/*
 * Copyright (c) 2020 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.net;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.lwjgl.system.MemoryStack;

/**
 * Secure-ish UDP client. Performs handshaking, password authentication, and
 * encrypted communication with servers. Not very secure. see warnings in the
 * Server class for more details.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Client implements AutoCloseable
{
    static final int HANDSHAKE_CONNECTION_REQUEST = 1;
    static final int HANDSHAKE_CHALLENGE_RESPONSE = 2;
    static final int HANDSHAKE_KEY_EXCHANGE = 3;
    
    private static final float PENDING_CHECK_UP = 0.5f;
    private static final float PENDING_TIME_OUT = 5.0f;
    private static final float CONNECTED_CHECK_UP = 3.0f;
    private static final float CONNECTED_TIME_OUT = 30.0f;
    private static final int STATE_CONNECTION_REQUESTED = 0;
    private static final int STATE_CHALLENGED = 1;
    private static final int STATE_KEY_EXCHANGE = 2;
    private static final int STATE_FINALIZE = 3;
    private static final int STATE_CONNECTED = 4;
    private static final int STATE_CLOSED = -1;
    
    private final DatagramChannel channel;
    private final byte[] password;
    private final SecureRandom csprng;
    private final MessageDigest digest;
    private final Cipher cipher;
    
    private PrintStream log;
    private LogVerbosity verbosity = LogVerbosity.OFF;
    
    private int state = STATE_CONNECTION_REQUESTED;
    private float lastHeardFromServer = 0.0f;
    private float lastSpokenToServer = Float.POSITIVE_INFINITY;
    
    private byte[] nonce;
    
    private byte[] challengeResponse;
    
    private KeyPair dhKeyPair;
    
    private SecretKey encryptionKey;
    private LongSequence sequence;
    
    public Client(String hostname, int port, String password) throws Exception
    {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(InetAddress.getByName(hostname), port));
        this.password = NetUtil.bytes(password);
        csprng = SecureRandom.getInstance(NetUtil.CSPRNG_NAME);
        digest = MessageDigest.getInstance(NetUtil.DIGEST_NAME);
        cipher = Cipher.getInstance(NetUtil.CIPHER_NAME);
        
        nonce = new byte[16];
        csprng.nextBytes(nonce);
    }
    
    public void setLog(PrintStream log, LogVerbosity verbosity)
    {
        if (log == null) verbosity = LogVerbosity.OFF;
        this.log = log;
        this.verbosity = verbosity;
    }
    
    private void incomingPacket(ByteBuffer buffer) throws Exception
    {
        switch (state)
        {
            case STATE_CONNECTION_REQUESTED:
                NetUtil.verifyChecksumAndType(buffer, Server.HANDSHAKE_CONNECTION_CHALLENGE);

                //Note. Always process packet before changing state, in case packet is malformed.
                byte[] challengeNonce = new byte[buffer.remaining()];
                buffer.get(challengeNonce);
                digest.update(nonce);
                digest.update(password);
                digest.update(challengeNonce);
                challengeResponse = digest.digest();

                state = STATE_CHALLENGED;
                lastHeardFromServer = 0.0f;
                lastSpokenToServer = Float.POSITIVE_INFINITY;
                nonce = null;

                verbosity.medium(log, () -> "CLIENT: Received connection challenge.");
                break;
            case STATE_CHALLENGED:
                NetUtil.verifyChecksumAndType(buffer, Server.HANDSHAKE_KEY_EXCHANGE);

                byte[] params = new byte[Byte.toUnsignedInt(buffer.get())];
                buffer.get(params);
                AlgorithmParameters dhParams = AlgorithmParameters.getInstance("DiffieHellman");
                dhParams.init(params);

                KeyPairGenerator dhKeyGen = KeyPairGenerator.getInstance("DiffieHellman");
                dhKeyGen.initialize(dhParams.getParameterSpec(DHParameterSpec.class));
                dhKeyPair = dhKeyGen.genKeyPair();

                KeyAgreement dh = KeyAgreement.getInstance("DiffieHellman");
                dh.init(dhKeyPair.getPrivate());

                byte[] sKey = new byte[Byte.toUnsignedInt(buffer.get())];
                buffer.get(sKey);

                KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
                PublicKey serverPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(sKey));
                dh.doPhase(serverPublicKey, true);
                byte[] sharedSecret = dh.generateSecret();

                digest.update(challengeResponse);
                digest.update(sharedSecret);
                encryptionKey = new SecretKeySpec(digest.digest(), "AES");
                sequence = new LongSequence(256, 0L);

                state = STATE_KEY_EXCHANGE;
                lastHeardFromServer = 0.0f;
                lastSpokenToServer = Float.POSITIVE_INFINITY;
                challengeResponse = null;

                verbosity.medium(log, () -> "CLIENT: Received public key.");
                break;
            case STATE_KEY_EXCHANGE:
                Packet packet = NetUtil.decrypt(buffer, cipher, encryptionKey, sequence);
                if (packet.type == Packet.TYPE_FINALIZE)
                {
                    state = STATE_FINALIZE;
                    lastHeardFromServer = 0.0f;
                    lastSpokenToServer = Float.POSITIVE_INFINITY;
                    
                    verbosity.medium(log, () -> "CLIENT: Received finalize.");
                }
                break;
            case STATE_FINALIZE:
                packet = NetUtil.decrypt(buffer, cipher, encryptionKey, sequence);
                if (packet.type == Packet.TYPE_KEEPALIVE)
                {
                    state = STATE_CONNECTED;
                    lastHeardFromServer = 0.0f;
                    verbosity.low(log, () -> "CLIENT: Connection complete.");
                }
                break;
            case STATE_CONNECTED:
                packet = NetUtil.decrypt(buffer, cipher, encryptionKey, sequence);
                switch (packet.type)
                {
                    case Packet.TYPE_KEEPALIVE:
                        lastHeardFromServer = 0.0f;
                        verbosity.high(log, () -> "CLIENT: Received keepalive.");
                        break;
                    case Packet.TYPE_MESSAGE:
                    case Packet.TYPE_ACK:
                        //Pass to Peer.
                        lastHeardFromServer = 0.0f;
                        verbosity.high(log, () -> "CLIENT: Received packet.");
                        break;
                }
                break;
        }
    }
    
    private void outgoingPacket(ByteBuffer buffer) throws Exception
    {
        buffer.clear();

        switch (state)
        {
            case STATE_CONNECTION_REQUESTED:
                buffer.position(4);
                buffer.put((byte)HANDSHAKE_CONNECTION_REQUEST);
                buffer.put(nonce);
                buffer.position(1000);
                NetUtil.flipAndBufferChecksum(buffer);
                channel.write(buffer);

                verbosity.medium(log, () -> "CLIENT: Sent connection request.");
                break;
            case STATE_CHALLENGED:
                buffer.position(4);
                buffer.put((byte)HANDSHAKE_CHALLENGE_RESPONSE);
                buffer.put(challengeResponse);
                NetUtil.flipAndBufferChecksum(buffer);
                channel.write(buffer);

                verbosity.medium(log, () -> "CLIENT: Sent challenge response.");
                break;
            case STATE_KEY_EXCHANGE:
                buffer.position(4);
                buffer.put((byte)HANDSHAKE_KEY_EXCHANGE);
                buffer.put(dhKeyPair.getPublic().getEncoded());
                NetUtil.flipAndBufferChecksum(buffer);
                channel.write(buffer);

                verbosity.medium(log, () -> "CLIENT: Sent public key.");
                break;
            case STATE_FINALIZE:
                Packet packet = new Packet();
                packet.type = Packet.TYPE_FINALIZE;
                NetUtil.encrypt(packet, buffer, cipher, encryptionKey, sequence);
                buffer.flip();
                channel.write(buffer);
                
                verbosity.medium(log, () -> "CLIENT: Sent finalize.");
                break;
            case STATE_CONNECTED:
                packet = new Packet();
                packet.type = Packet.TYPE_KEEPALIVE;
                NetUtil.encrypt(packet, buffer, cipher, encryptionKey, sequence);
                buffer.flip();
                channel.write(buffer);
                
                verbosity.high(log, () -> "CLIENT: Sent keepalive.");
                break;
        }
    }
    
    public void update(float dt) throws Exception
    {
        if (state == STATE_CLOSED) return;
        
        float timeOut = (state == STATE_CONNECTED || state == STATE_FINALIZE) ? CONNECTED_TIME_OUT : PENDING_TIME_OUT;
        if (lastHeardFromServer > timeOut)
        {
            close();
            verbosity.low(log, () -> "CLIENT: Timed out.");
            return;
        }
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.malloc(NetUtil.MAX_PACKET_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            //INCOMING
            while (true)
            {
                buffer.clear();
                if (channel.read(buffer) <= 0) break;
                buffer.flip();
                
                try
                {
                    incomingPacket(buffer);
                }
                catch (Throwable t)
                {
                    if (verbosity == LogVerbosity.HIGH)
                    {
                        log.print("CLIENT: ");
                        t.printStackTrace(log);
                    }
                    else verbosity.medium(log, () -> "CLIENT: Packet threw " + t);
                }
            }
            
            //OUTGOING
            float checkUp = state == STATE_CONNECTED ? CONNECTED_CHECK_UP : PENDING_CHECK_UP;
            if (lastSpokenToServer > checkUp)
            {
                outgoingPacket(buffer);
                lastSpokenToServer = 0.0f;
            }
        }
        
        lastSpokenToServer += dt;
        lastHeardFromServer += dt;
    }

    @Override
    public void close() throws IOException
    {
        channel.close();
        state = STATE_CLOSED;
    }
}
