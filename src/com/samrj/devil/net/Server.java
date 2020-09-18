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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.lwjgl.system.MemoryStack;

/**
 * Secure-ish UDP server. Performs handshaking, password authentication, and
 * encrypted communication with clients. Not very secure, so don't use this for
 * anything really important.
 * 
 * Since this class doesn't use public-key infrastructure, it is certainly
 * vulnerable to man-in-the-middle attacks. It probably has more issues.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Server implements AutoCloseable
{
    static final int HANDSHAKE_CONNECTION_CHALLENGE = 2;
    static final int HANDSHAKE_KEY_EXCHANGE = 3;
    
    private static final int CLIENT_STATE_CONNECTION_REQUESTED = 0;
    private static final int CLIENT_STATE_RESPONDED_TO_CHALLENGE = 1;
    private static final int CLIENT_STATE_KEY_EXCHANGED = 2;
    private static final int CLIENT_STATE_CONNECTED = 3;
    private static final float PENDING_CHECK_UP = 0.5f;
    private static final float PENDING_TIME_OUT = 5.0f;
    private static final float CONNECTED_CHECK_UP = 3.0f;
    private static final float CONNECTED_TIME_OUT = 30.0f;
    
    private final DatagramChannel channel;
    private final byte[] password;
    private final SecureRandom csprng;
    private final MessageDigest digest;
    private final AlgorithmParameterGenerator dhParamGen;
    private final KeyPairGenerator dhKeyGen;
    private final Cipher cipher;
    private final Map<SocketAddress, ServerClient> clients = new HashMap<>();
    
    private PrintStream log;
    private LogVerbosity verbosity = LogVerbosity.OFF;
    
    public Server(int port, String password) throws Exception
    {
        channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
        this.password = NetUtil.bytes(password);
        
        csprng = SecureRandom.getInstance(NetUtil.CSPRNG_NAME);
        digest = MessageDigest.getInstance(NetUtil.DIGEST_NAME);
        dhParamGen = AlgorithmParameterGenerator.getInstance("DiffieHellman");
        dhParamGen.init(512);
        dhKeyGen = KeyPairGenerator.getInstance("DiffieHellman");
        cipher = Cipher.getInstance(NetUtil.CIPHER_NAME);
    }
    
    public void setLog(PrintStream log, LogVerbosity verbosity)
    {
        if (log == null) verbosity = LogVerbosity.OFF;
        this.log = log;
        this.verbosity = verbosity;
    }
    
    public Collection<ServerClient> getClients()
    {
        return Collections.unmodifiableCollection(clients.values());
    }
    
    private void incomingPacket(ByteBuffer buffer, SocketAddress address) throws Exception
    {
        ServerClient client = clients.get(address);
        
        if (client == null)
        {
            //Connection requests must be padded to 1000 bytes. This prevents
            //this server from being used for a DoS amplification attack.
            if (buffer.limit() != 1000) return;
            
            NetUtil.verifyChecksumAndType(buffer, Client.HANDSHAKE_CONNECTION_REQUEST);
            byte[] nonce = new byte[16];
            buffer.get(nonce);
            
            clients.put(address, new ServerClient(address, nonce));

            verbosity.medium(log, () -> "SERVER: Connection requested by client " + address);
        }
        else switch (client.state)
        {
            case CLIENT_STATE_CONNECTION_REQUESTED:
                NetUtil.verifyChecksumAndType(buffer, Client.HANDSHAKE_CHALLENGE_RESPONSE);

                byte[] challengeResponse = new byte[buffer.remaining()];
                buffer.get(challengeResponse);

                if (Arrays.equals(challengeResponse, client.expectedChallengeResponse))
                {
                    client.dh = KeyAgreement.getInstance("DiffieHellman");
                    client.dhParams = dhParamGen.generateParameters();
                    dhKeyGen.initialize(client.dhParams.getParameterSpec(DHParameterSpec.class));
                    client.keyPair = dhKeyGen.genKeyPair();
                    client.dh.init(client.keyPair.getPrivate());

                    client.state = CLIENT_STATE_RESPONDED_TO_CHALLENGE;
                    client.lastHeardFrom = 0.0f;
                    client.lastSpokenTo = Float.POSITIVE_INFINITY;
                    client.challengeNonce = null;
                    
                    verbosity.medium(log, () -> "SERVER: Good challenge response received from client " + client.address);
                }
                break;
            case CLIENT_STATE_RESPONDED_TO_CHALLENGE:
                NetUtil.verifyChecksumAndType(buffer, Client.HANDSHAKE_KEY_EXCHANGE);

                byte[] clientPublicKey = new byte[buffer.remaining()];
                buffer.get(clientPublicKey);

                KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
                client.dh.doPhase(keyFactory.generatePublic(new X509EncodedKeySpec(clientPublicKey)), true);
                byte[] sharedSecret = client.dh.generateSecret();
                
                digest.update(client.expectedChallengeResponse);
                digest.update(sharedSecret);
                client.encryptionKey = new SecretKeySpec(digest.digest(), "AES");
                client.sequence = new LongSequence(256, Long.MIN_VALUE);

                client.state = CLIENT_STATE_KEY_EXCHANGED;
                client.lastHeardFrom = 0.0f;
                client.lastSpokenTo = Float.POSITIVE_INFINITY;
                client.expectedChallengeResponse = null;
                client.dh = null;
                client.dhParams = null;
                client.keyPair = null;
                
                verbosity.medium(log, () -> "SERVER: Received public key from client " + client.address);
                break;
            case CLIENT_STATE_KEY_EXCHANGED:
                Packet packet = NetUtil.decrypt(buffer, cipher, client.encryptionKey, client.sequence);
                if (packet.type == Packet.TYPE_FINALIZE)
                {
                    //Set up message channel.
                    
                    client.state = CLIENT_STATE_CONNECTED;
                    client.lastHeardFrom = 0.0f;
                    client.lastSpokenTo = Float.POSITIVE_INFINITY;
                    
                    verbosity.low(log, () -> "SERVER: Connection completed by client " + client.address);
                }
                break;
            case CLIENT_STATE_CONNECTED:
                packet = NetUtil.decrypt(buffer, cipher, client.encryptionKey, client.sequence);
                switch (packet.type)
                {
                    case Packet.TYPE_KEEPALIVE:
                        client.lastHeardFrom = 0.0f;
                        verbosity.high(log, () -> "SERVER: Keepalive recieved from client " + client.address);
                        break;
                    case Packet.TYPE_MESSAGE:
                    case Packet.TYPE_ACK:
                        //Pass to Peer.
                        client.lastHeardFrom = 0.0f;
                        verbosity.high(log, () -> "SERVER: Packet recieved from client " + client.address);
                        break;
                }
                break;
        }
    }
    
    private void outgoingPacket(ByteBuffer buffer, ServerClient client) throws Exception
    {
        buffer.clear();

        switch (client.state)
        {
            case CLIENT_STATE_CONNECTION_REQUESTED:
                buffer.position(4);
                buffer.put((byte)HANDSHAKE_CONNECTION_CHALLENGE);
                buffer.put(client.challengeNonce);
                NetUtil.flipAndBufferChecksum(buffer);
                channel.send(buffer, client.address);

                verbosity.medium(log, () -> "SERVER: Sent challenge to client " + client.address);
                break;
            case CLIENT_STATE_RESPONDED_TO_CHALLENGE:
                buffer.position(4);
                buffer.put((byte)HANDSHAKE_KEY_EXCHANGE);

                byte[] params = client.dhParams.getEncoded();
                if (params.length > 255) throw new IllegalArgumentException();
                buffer.put((byte)params.length);
                buffer.put(params);

                byte[] serverPublicKey = client.keyPair.getPublic().getEncoded();
                if (serverPublicKey.length > 255) throw new IllegalArgumentException();
                buffer.put((byte)serverPublicKey.length);
                buffer.put(serverPublicKey);

                NetUtil.flipAndBufferChecksum(buffer);
                channel.send(buffer, client.address);

                verbosity.medium(log, () -> "SERVER: Sent public key to client " + client.address);
                break;
            case CLIENT_STATE_KEY_EXCHANGED:
                Packet packet = new Packet();
                packet.type = Packet.TYPE_FINALIZE;
                NetUtil.encrypt(packet, buffer, cipher, client.encryptionKey, client.sequence);
                buffer.flip();
                channel.send(buffer, client.address);

                verbosity.medium(log, () -> "SERVER: Sent finalize to client " + client.address);
                break;
            case CLIENT_STATE_CONNECTED:
                packet = new Packet();
                packet.type = Packet.TYPE_KEEPALIVE;
                NetUtil.encrypt(packet, buffer, cipher, client.encryptionKey, client.sequence);
                buffer.flip();
                channel.send(buffer, client.address);

                verbosity.high(log, () -> "SERVER: Sent keepalive to client " + client.address);
                break;
        }
    }
    
    public void update(float dt) throws Exception
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.malloc(NetUtil.MAX_PACKET_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            //INCOMING
            while (true)
            {
                buffer.clear();
                SocketAddress address = channel.receive(buffer);
                if (address == null) break;
                buffer.flip();
                
                try
                {
                    incomingPacket(buffer, address);
                }
                catch (Throwable t)
                {
                    if (verbosity == LogVerbosity.HIGH)
                    {
                        log.print("SERVER: ");
                        t.printStackTrace(log);
                    }
                    else verbosity.medium(log, () -> "SERVER: Packet threw " + t);
                }
            }
            
            //OUTGOING
            for (Iterator<ServerClient> it = clients.values().iterator(); it.hasNext();)
            {
                ServerClient client = it.next();
                float timeOut = client.state == CLIENT_STATE_CONNECTED ? CONNECTED_TIME_OUT : PENDING_TIME_OUT;
                if (client.lastHeardFrom >= timeOut)
                {
                    it.remove();
                    
                    verbosity.low(log, () -> "SERVER: Timed out client " + client.address);
                    continue;
                }
                
                float checkUp = client.state == CLIENT_STATE_CONNECTED ? CONNECTED_CHECK_UP : PENDING_CHECK_UP;
                if (client.lastSpokenTo >= checkUp)
                {
                    outgoingPacket(buffer, client);
                    client.lastSpokenTo = 0.0f;
                }

                client.lastHeardFrom += dt;
                client.lastSpokenTo += dt;
            }
        }
    }
    
    @Override
    public void close() throws IOException
    {
        channel.close();
    }
    
    public class ServerClient
    {
        private final SocketAddress address;
        private int state = CLIENT_STATE_CONNECTION_REQUESTED;
        private float lastHeardFrom = 0.0f;
        private float lastSpokenTo = Float.POSITIVE_INFINITY;
        
        private byte[] challengeNonce;
        private byte[] expectedChallengeResponse;
        
        private KeyAgreement dh;
        private AlgorithmParameters dhParams;
        private KeyPair keyPair;
        
        private SecretKey encryptionKey;
        private LongSequence sequence;
    
        private ServerClient(SocketAddress address, byte[] nonce)
        {
            this.address = address;
            
            challengeNonce = new byte[16];
            csprng.nextBytes(challengeNonce);
            
            digest.update(nonce);
            digest.update(password);
            digest.update(challengeNonce);
            expectedChallengeResponse = digest.digest();
        }
        
        public SocketAddress getAddress()
        {
            return address;
        }
    }
}
