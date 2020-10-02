package com.samrj.devil.net;

import java.io.IOException;
import java.util.ArrayDeque;

/**
 * Provides time-sensitive reliability and datagram fragmentation. Used by both
 * client and server.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
class Peer
{
    static final int HEADER_SIZE = 13;
    static final int MAX_PAYLOAD = NetUtil.MAX_DATAGRAM_SIZE - HEADER_SIZE;
    
    private static final int UNRELIABLE = 0;
    
    private final PayloadConsumer consumer;
    private final ArrayDeque<byte[]> inbox = new ArrayDeque<>();
    private float bytesPerSecond, byteAccumLimit;
    private float availableBandwidth;
    
    Peer(PayloadConsumer consumer, float kilobitsPerSecond, float secondsToAccumulate)
    {
        this.consumer = consumer;
        setBandwidth(kilobitsPerSecond, secondsToAccumulate);
    }
    
    void setBandwidth(float kilobitsPerSecond, float secondsToAccumulate)
    {
        bytesPerSecond = kilobitsPerSecond*125.0f;
        byteAccumLimit = bytesPerSecond*secondsToAccumulate;
    }
    
    void incoming(int type, byte[] data)
    {
        switch (type)
        {
            case UNRELIABLE:
                inbox.addLast(data);
                break;
        }
    }
    
    void update(float dt) throws IOException
    {
        availableBandwidth += dt*bytesPerSecond;
        if (availableBandwidth > byteAccumLimit) availableBandwidth = byteAccumLimit;
    }
    
    boolean isInboxNonempty()
    {
        return !inbox.isEmpty();
    }
    
    byte[] receive()
    {
        return inbox.pollFirst();
    }
    
    void send(byte[] message, float expiry) throws IOException
    {
        if (expiry <= 0.0f) //Unreliable message.
        {
            if (message.length > MAX_PAYLOAD)
                throw new IOException("Unreliable messages cannot exceed " + MAX_PAYLOAD + " bytes.");
            
            //Immediately drop unreliable messages if we don't have enough bandwidth.
            int bandwidthRequirement = HEADER_SIZE + message.length;
            if (bandwidthRequirement <= availableBandwidth)
            {
                consumer.accept(UNRELIABLE, message);
                availableBandwidth -= bandwidthRequirement;
            }
        }
    }
    
    void destroy()
    {
        inbox.clear();
    }
}
