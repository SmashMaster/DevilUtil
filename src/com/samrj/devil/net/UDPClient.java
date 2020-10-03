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
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Arrays;
import org.lwjgl.system.MemoryStack;

/**
 * UDP game client. Establishes a connection with a UDP game server through
 * basic handshaking. Provides no guarantees of reliability once the handshake
 * is complete: datagrams can be dropped, duplicated, or delivered out of order.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class UDPClient implements Peer
{
    static final int CONNECTION_REQUEST = 1;
    static final int CHALLENGE_RESPONSE = 2;
    static final int KEEPALIVE = 3;
    static final int DISCONNECT = 4;
    static final int MESSAGE = 5;
    
    private static final float PENDING_CHECK_UP = 0.5f;
    private static final float PENDING_TIME_OUT = 5.0f;
    private static final float CONNECTED_CHECK_UP = 10.0f;
    private static final float CONNECTED_TIME_OUT = 60.0f;
    private static final int STATE_CONNECTION_REQUESTED = 0;
    private static final int STATE_CHALLENGED = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTED = 3;
    
    private final DatagramChannel channel;
    private final byte[] password;
    
    private PrintStream log;
    private LogVerbosity verbosity = LogVerbosity.OFF;
    
    private int state = STATE_CONNECTION_REQUESTED;
    private float lastHeardFromServer = 0.0f;
    private float lastSpokenToServer = Float.POSITIVE_INFINITY;
    
    private byte[] nonce;
    private byte[] challengeResponse;
    private byte[] identifier;
    
    private final ArrayDeque<byte[]> inbox = new ArrayDeque<>();
    
    /**
     * Attempts to connect with the given hostname and port, using the given
     * password.
     */
    public UDPClient(String hostname, int port, String password) throws IOException
    {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(InetAddress.getByName(hostname), port));
        this.password = NetUtil.bytes(password);
        nonce = new byte[16];
        NetUtil.getCSPRNG().nextBytes(nonce);
    }
    
    public void setLog(PrintStream log, LogVerbosity verbosity)
    {
        if (log == null) verbosity = LogVerbosity.OFF;
        this.log = log;
        this.verbosity = verbosity;
    }
    
    @Override
    public boolean isConnectionPending()
    {
        return state == STATE_CONNECTION_REQUESTED || state == STATE_CHALLENGED;
    }

    @Override
    public boolean isConnected()
    {
        return state == STATE_CONNECTED;
    }

    @Override
    public boolean isDisconnected()
    {
        return state == STATE_DISCONNECTED;
    }

    private void sendDisconnect() throws IOException
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.malloc(13);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(4);
            buffer.put((byte)DISCONNECT);
            buffer.put(identifier);
            NetUtil.flipAndBufferChecksum(buffer);
            channel.write(buffer);
        }
    }
    
    private void incomingPacket(ByteBuffer buffer) throws ClientException, IOException
    {
        switch (state)
        {
            case STATE_CONNECTION_REQUESTED:
                if (buffer.limit() < 21) break;
                if (NetUtil.failedChecksum(buffer)) break;
                
                switch (Byte.toUnsignedInt(buffer.get()))
                {
                    case UDPServer.SERVER_FULL:
                        if (buffer.limit() != 21) break;
                        byte[] pNonce = new byte[16];
                        buffer.get(pNonce);
                        if (!Arrays.equals(pNonce, nonce)) break;
                        close();
                        verbosity.low(log, () -> "CLIENT: Server full.");
                        throw new ServerFullException();
                    case UDPServer.CHALLENGE:
                        if (buffer.limit() != 38) break;
                        
                        pNonce = new byte[16];
                        buffer.get(pNonce);
                        if (!Arrays.equals(pNonce, nonce)) break;

                        byte[] serverNonce = new byte[16];
                        buffer.get(serverNonce);

                        boolean hasPassword = buffer.get() != 0;

                        MessageDigest digest = NetUtil.getDigest();
                        digest.update(nonce);
                        digest.update(serverNonce);
                        if (hasPassword) digest.update(password);
                        challengeResponse = digest.digest();
                        identifier = new byte[8];
                        System.arraycopy(challengeResponse, 0, identifier, 0, 8);

                        state = STATE_CHALLENGED;
                        lastHeardFromServer = 0.0f;
                        lastSpokenToServer = Float.POSITIVE_INFINITY;

                        verbosity.medium(log, () -> "CLIENT: Challenge received.");
                        break;
                }
                break;
            case STATE_CHALLENGED:
                if (buffer.limit() < 13) break;
                if (NetUtil.failedChecksum(buffer)) break;
                switch (Byte.toUnsignedInt(buffer.get()))
                {
                    case UDPServer.SERVER_FULL:
                        if (buffer.limit() != 21) break;
                        byte[] pNonce = new byte[16];
                        buffer.get(pNonce);
                        if (!Arrays.equals(pNonce, nonce)) break;
                        close();
                        verbosity.low(log, () -> "CLIENT: Server full.");
                        throw new ServerFullException();
                    case UDPServer.PASSWORD_INCORRECT:
                        if (buffer.limit() != 21) break;
                        pNonce = new byte[16];
                        buffer.get(pNonce);
                        if (!Arrays.equals(pNonce, nonce)) break;
                        close();
                        verbosity.low(log, () -> "CLIENT: Password rejected.");
                        throw new IncorrectPasswordException();
                    case UDPServer.KEEPALIVE:
                        byte[] pIdentifier = new byte[8];
                        buffer.get(pIdentifier);
                        if (!Arrays.equals(pIdentifier, identifier)) break;

                        state = STATE_CONNECTED;
                        lastHeardFromServer = 0.0f;
                        nonce = null;
                        challengeResponse = null;

                        verbosity.medium(log, () -> "CLIENT: Connection completed.");
                        break;
                }
                break;
            case STATE_CONNECTED:
                if (buffer.limit() < 13) break;
                if (NetUtil.failedChecksum(buffer)) break;
                int type = Byte.toUnsignedInt(buffer.get());
                
                byte[] pIdentifier = new byte[8];
                buffer.get(pIdentifier);
                if (!Arrays.equals(pIdentifier, identifier)) break;
                
                switch (type)
                {
                    case UDPServer.KEEPALIVE:
                        lastHeardFromServer = 0.0f;
                        verbosity.high(log, () -> "Client: Keepalive received.");
                        break;
                    case UDPServer.DISCONNECT:
                        lastHeardFromServer = 0.0f;
                        verbosity.low(log, () -> "Client: Connection terminated by server.");
                        close();
                        throw new ServerDisconnectedException();
                    case UDPServer.MESSAGE:
                        byte[] message = new byte[buffer.remaining()];
                        buffer.get(message);
                        inbox.addLast(message);
                        lastHeardFromServer = 0.0f;
                        verbosity.high(log, () -> "Client: Message received.");
                        break;
                }
                break;
        }
    }
    
    private void outgoingPacket(ByteBuffer buffer) throws IOException
    {
        switch (state)
        {
            case STATE_CONNECTION_REQUESTED:
                buffer.clear();
                buffer.position(4);
                buffer.put((byte)CONNECTION_REQUEST);
                buffer.put(nonce);
                while (buffer.position() < 1000) buffer.put((byte)0);
                NetUtil.flipAndBufferChecksum(buffer);
                channel.write(buffer);
                
                verbosity.medium(log, () -> "CLIENT: Sent connection request.");
                break;
            case STATE_CHALLENGED:
                buffer.clear();
                buffer.position(4);
                buffer.put((byte)CHALLENGE_RESPONSE);
                buffer.put(challengeResponse);
                NetUtil.flipAndBufferChecksum(buffer);
                channel.write(buffer);
                
                verbosity.medium(log, () -> "CLIENT: Sent challenge response.");
                break;
            case STATE_CONNECTED:
                buffer.clear();
                buffer.position(4);
                buffer.put((byte)KEEPALIVE);
                buffer.put(identifier);
                NetUtil.flipAndBufferChecksum(buffer);
                channel.write(buffer);
                
                verbosity.high(log, () -> "CLIENT: Sent keepalive.");
                break;
        }
    }
    
    /**
     * Updates this client. Call this often, at least once per second. Receives
     * any incoming messages, and sends any outgoing messages.
     * 
     * If the client's connection fails for any reason, a ClientException will
     * be thrown.
     */
    public void update(float dt) throws ClientException, IOException
    {
        if (state == STATE_DISCONNECTED) return;
        
        float timeOut = state == STATE_CONNECTED ? CONNECTED_TIME_OUT : PENDING_TIME_OUT;
        if (lastHeardFromServer > timeOut)
        {
            close();
            verbosity.low(log, () -> "CLIENT: Timed out.");
            throw new TimedOutException();
        }
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.malloc(MAX_PACKET_SIZE);
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
                catch (ClientException t)
                {
                    throw t;
                }
                catch (Exception t)
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
    public boolean hasDatagrams()
    {
        return !inbox.isEmpty();
    }
    
    @Override
    public byte[] receive()
    {
        return inbox.pollFirst();
    }
    
    /**
     * Sends the given datagram. If this client is not connected, this
     * method will do nothing. If the datagram exceeds 1187 bytes, throws
     * IOException.
     */
    @Override
    public void send(byte[] datagram) throws IOException
    {
        if (state != STATE_CONNECTED) return;
        if (datagram.length > MAX_PAYLOAD_SIZE) throw new IOException("Datagram length must not exceed " + MAX_PAYLOAD_SIZE);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.malloc(HEADER_SIZE + datagram.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(4);
            buffer.put((byte)MESSAGE);
            buffer.put(identifier);
            buffer.put(datagram);
            NetUtil.flipAndBufferChecksum(buffer);
            channel.write(buffer);
        }
    }

    @Override
    public void close() throws IOException
    {
        if (state == STATE_CONNECTED && channel.isConnected()) sendDisconnect();
        channel.close();
        state = STATE_DISCONNECTED;
        inbox.clear();
    }
}
