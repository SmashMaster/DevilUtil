package com.samrj.devil.net;

import java.io.IOException;

/**
 * Symmetric peer interface, for clients and servers.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Peer extends AutoCloseable
{
    /**
     * Returns true if this peer is in the process of connecting.
     */
    public boolean isConnectionPending();

    /**
     * Returns true if this peer is connected.
     */
    public boolean isConnected();
    /**
     * Returns true if this peer is disconnected.
     */
    public boolean isDisconnected();
        
    /**
     * Returns true if one or more datagrams are waiting in this peer's inbox.
     */
    public boolean hasDatagrams();
    
    /**
     * Returns the next datagram received by this peer, or null if the inbox
     * is empty. This must be called repeatedly until the inbox is empty, or
     * else it might grow until no more memory is available.
     */
    public byte[] receive();
    
    /**
     * Sends the given datagram. If this peer is not connected, this method will
     * do nothing.
     */
    public void send(byte[] datagram) throws IOException;

    /**
     * Disconnects this peer.
     */
    @Override
    public void close() throws IOException;
}
