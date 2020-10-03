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
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import org.lwjgl.system.MemoryStack;

/**
 * UDP game server.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Server implements AutoCloseable
{
    static final int CHALLENGE = 1;
    static final int SERVER_FULL = 2;
    static final int PASSWORD_INCORRECT = 3;
    static final int KEEPALIVE = 4;
    static final int DISCONNECT = 5;
    static final int MESSAGE = 6;
    
    private static final int CLIENT_STATE_CONNECTION_PENDING = 0;
    private static final int CLIENT_STATE_CONNECTED = 1;
    private static final int CLIENT_STATE_DISCONNECTED = 2;
    private static final float PENDING_CHECK_UP = 0.5f;
    private static final float PENDING_TIME_OUT = 5.0f;
    private static final float CONNECTED_CHECK_UP = 10.0f;
    private static final float CONNECTED_TIME_OUT = 60.0f;
    
    private final DatagramChannel channel;
    private final byte[] password;
    private final SecureRandom csprng;
    private final MessageDigest digest;
    private final Map<SocketAddress, ServerClient> clients = new HashMap<>();
    private final Set<ServerClient> connectedClients = Collections.newSetFromMap(new IdentityHashMap<>());
    private final int capacity;
    
    private PrintStream log;
    private LogVerbosity verbosity = LogVerbosity.OFF;
    
    /**
     * Binds this server to the given port, and sets its password and capacity.
     * It will immediately start handling connections from clients.
     */
    public Server(int port, String password, int capacity) throws IOException
    {
        channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
        this.password = NetUtil.bytes(password);
        csprng = NetUtil.getCSPRNG();
        digest = NetUtil.getDigest();
        this.capacity = capacity;
    }
    
    public void setLog(PrintStream log, LogVerbosity verbosity)
    {
        if (log == null) verbosity = LogVerbosity.OFF;
        this.log = log;
        this.verbosity = verbosity;
    }
    
    /**
     * Returns a set of all connected clients.
     */
    public Set<ServerClient> getClients()
    {
        return Collections.unmodifiableSet(connectedClients);
    }
    
    private void sendServerFull(SocketAddress address, byte[] nonce) throws IOException
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.malloc(21);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(4);
            buffer.put((byte)SERVER_FULL);
            buffer.put(nonce);
            NetUtil.flipAndBufferChecksum(buffer);
            channel.send(buffer, address);
            verbosity.high(log, () -> "SERVER: Server full, turned away client " + address);
        }
    }
    
    private void sendPasswordIncorrect(ServerClient client) throws IOException
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.malloc(21);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(4);
            buffer.put((byte)PASSWORD_INCORRECT);
            buffer.put(client.nonce);
            NetUtil.flipAndBufferChecksum(buffer);
            channel.send(buffer, client.address);
            verbosity.high(log, () -> "SERVER: Incorrect password from client " + client.address);
        }
    }
    
    private void sendDisconnect(ServerClient client) throws IOException
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.malloc(13);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(4);
            buffer.put((byte)DISCONNECT);
            buffer.put(client.identifier);
            NetUtil.flipAndBufferChecksum(buffer);
            channel.send(buffer, client.address);
            verbosity.high(log, () -> "SERVER: Incorrect password from client " + client.address);
        }
    }
    
    private void incomingPacket(ByteBuffer buffer, SocketAddress address) throws IOException
    {
        ServerClient client = clients.get(address);
        
        if (client == null)
        {
            if (buffer.limit() != 1000) return;
            if (NetUtil.failedChecksum(buffer)) return;
            if (Byte.toUnsignedInt(buffer.get()) != Client.CONNECTION_REQUEST) return;
            
            byte[] nonce = new byte[16];
            buffer.get(nonce);
            
            if (connectedClients.size() >= capacity)
            {
                sendServerFull(address, nonce);
                return;
            }
            
            client = new ServerClient(address);
            clients.put(address, client);
            
            client.nonce = nonce;
            client.serverNonce = new byte[16];
            csprng.nextBytes(client.serverNonce);
            
            digest.update(client.nonce);
            digest.update(client.serverNonce);
            if (password.length != 0) digest.update(password);
            client.expectedChallengeResponse = digest.digest();
            
            verbosity.medium(log, () -> "SERVER: Connection requested by client " + address);
        }
        else switch (client.state)
        {
            case CLIENT_STATE_CONNECTION_PENDING:
                if (buffer.limit() != 37) break;
                if (NetUtil.failedChecksum(buffer)) break;
                if (Byte.toUnsignedInt(buffer.get()) != Client.CHALLENGE_RESPONSE) break;
                
                if (connectedClients.size() >= capacity)
                {
                    clients.remove(address);
                    client.state = CLIENT_STATE_DISCONNECTED;
                    sendServerFull(address, client.nonce);
                    return;
                }
                
                byte[] challengeResponse = new byte[32];
                buffer.get(challengeResponse);
                if (!Arrays.equals(challengeResponse, client.expectedChallengeResponse))
                {
                    clients.remove(address);
                    client.state = CLIENT_STATE_DISCONNECTED;
                    sendPasswordIncorrect(client);
                    break;
                }
                
                client.identifier = new byte[8];
                System.arraycopy(client.expectedChallengeResponse, 0, client.identifier, 0, 8);
                
                client.nonce = null;
                client.serverNonce = null;
                client.expectedChallengeResponse = null;
                client.state = CLIENT_STATE_CONNECTED;
                client.lastHeardFrom = 0.0f;
                client.lastSpokenTo = Float.POSITIVE_INFINITY;
                
                connectedClients.add(client);
                
                verbosity.low(log, () -> "SERVER: Connection completed by client " + address);
                break;
            case CLIENT_STATE_CONNECTED:
                if (buffer.limit() < 13) break;
                if (NetUtil.failedChecksum(buffer)) break;
                int type = Byte.toUnsignedInt(buffer.get());
                
                byte[] pIdentifier = new byte[8];
                buffer.get(pIdentifier);
                if (!Arrays.equals(pIdentifier, client.identifier)) break;
                
                switch (type)
                {
                    case Client.CHALLENGE_RESPONSE:
                        //Client already connected, but initial keepalive dropped.
                        client.lastSpokenTo = Float.POSITIVE_INFINITY;
                        client.lastHeardFrom = 0.0f;
                        verbosity.high(log, () -> "SERVER: Redundant challenge response from client " + address);
                        break;
                    case Client.KEEPALIVE:
                        client.lastHeardFrom = 0.0f;
                        verbosity.high(log, () -> "SERVER: Keepalive from client " + address);
                        break;
                    case Client.DISCONNECT:
                        client.disconnect();
                        verbosity.low(log, () -> "SERVER: Client " + address + " disconnected");
                        break;
                    case Client.MESSAGE:
                        byte[] message = new byte[buffer.remaining()];
                        buffer.get(message);
                        client.inbox.addLast(message);
                        client.lastHeardFrom = 0.0f;
                        verbosity.high(log, () -> "SERVER: Message from client " + address);
                        break;
                }
                break;
        }
    }
    
    private void outgoingPacket(ByteBuffer buffer, ServerClient client) throws IOException
    {
        switch (client.state)
        {
            case CLIENT_STATE_CONNECTION_PENDING:
                buffer.clear();
                buffer.position(4);
                buffer.put((byte)CHALLENGE);
                buffer.put(client.nonce);
                buffer.put(client.serverNonce);
                buffer.put(password.length != 0 ? (byte)1 : (byte)0);
                NetUtil.flipAndBufferChecksum(buffer);
                channel.send(buffer, client.address);
                
                verbosity.medium(log, () -> "SERVER: Sent challenge to client " + client.address);
                break;
            case CLIENT_STATE_CONNECTED:
                buffer.clear();
                buffer.position(4);
                buffer.put((byte)KEEPALIVE);
                buffer.put(client.identifier);
                NetUtil.flipAndBufferChecksum(buffer);
                channel.send(buffer, client.address);
                
                verbosity.high(log, () -> "SERVER: Sent keepalive to client " + client.address);
                break;
        }
    }
    
    /**
     * Sends and receives datagrams. This should be called frequently to ensure
     * messages are received in a timely manner, and that clients do not time
     * out.
     */
    public void update(float dt) throws IOException
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
                    client.state = CLIENT_STATE_DISCONNECTED;
                    connectedClients.remove(client);
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
    
    /**
     * Disconnects all clients from this server and stops any new connections.
     */
    @Override
    public void close() throws IOException
    {
        for (ServerClient client : clients.values())
        {
            if (client.state == CLIENT_STATE_CONNECTED && channel.isOpen()) sendDisconnect(client);
            client.state = CLIENT_STATE_DISCONNECTED;
        }
        clients.clear();
        connectedClients.clear();
        channel.close();
    }
    
    /**
     * Represents a client that is connected to this server.
     */
    public class ServerClient
    {
        private final SocketAddress address;
        private int state = CLIENT_STATE_CONNECTION_PENDING;
        private float lastHeardFrom = 0.0f;
        private float lastSpokenTo = Float.POSITIVE_INFINITY;
        
        private byte[] nonce;
        private byte[] serverNonce;
        private byte[] expectedChallengeResponse;
        private byte[] identifier;
        
        private final ArrayDeque<byte[]> inbox = new ArrayDeque<>();
        
        private ServerClient(SocketAddress address)
        {
            this.address = address;
        }
        
        /**
         * Returns the SocketAddress this client is connecting from.
         */
        public SocketAddress getAddress()
        {
            return address;
        }
        
        /**
         * Returns true if this client is in the process of connecting.
         */
        public boolean isConnectionPending()
        {
            return state == CLIENT_STATE_CONNECTION_PENDING;
        }
        
        /**
         * Returns true if this client is connected.
         */
        public boolean isConnected()
        {
            return state == CLIENT_STATE_CONNECTED;
        }
        
        /**
         * Returns true if this client is disconnected.
         */
        public boolean isDisconnected()
        {
            return state == CLIENT_STATE_DISCONNECTED;
        }
        
        /**
         * Disconnects this client from the server.
         */
        public void disconnect() throws IOException
        {
            if (state == CLIENT_STATE_DISCONNECTED) return;
            if (state == CLIENT_STATE_CONNECTED)
            {
                if (channel.isOpen()) sendDisconnect(this);
                connectedClients.remove(this);
            }
            state = CLIENT_STATE_DISCONNECTED;
            clients.remove(address);
            inbox.clear();
        }
        
        /**
         * Returns true if there is at least one datagram in the inbox.
         */
        public boolean isInboxNonempty()
        {
            return !inbox.isEmpty();
        }
        
        /**
         * Returns the next datagram received from this client, or null if the
         * inbox is empty. This must be called repeatedly until the inbox is
         * empty, or else it might grow until no more memory is available.
         */
        public byte[] receiveDatagram()
        {
            return inbox.pollFirst();
        }
        
        /**
         * Immediately sends the given datagram to the client. If this client
         * is not connected, this method will do nothing. Will throw a
         * BufferOverflowException if the datagram exceeds 1187 bytes.
         */
        public void sendDatagram(byte[] datagram) throws IOException
        {
            if (state != CLIENT_STATE_CONNECTED) return;

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                ByteBuffer buffer = stack.malloc(NetUtil.MAX_PACKET_SIZE);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.position(4);
                buffer.put((byte)MESSAGE);
                buffer.put(identifier);
                buffer.put(datagram);
                NetUtil.flipAndBufferChecksum(buffer);
                channel.send(buffer, address);
            }
        }
    }
}
