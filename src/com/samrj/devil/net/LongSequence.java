package com.samrj.devil.net;

import java.util.TreeSet;

/**
 * Keeps track of the sequence numbers of packets, such that duplicate packets
 * are rejected. It is assumed that packets will not deviate too far from their
 * original order, or else they are rejected.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
class LongSequence
{
    private final TreeSet<Long> values = new TreeSet<>();
    private final int capacity;
    
    private long outgoing;
    
    LongSequence(int capacity, long initialOutgoing)
    {
        this.capacity = capacity;
        outgoing = initialOutgoing;
    }
    
    /**
     * Returns true if the given sequence number has not been received. May
     * incorrectly reject packets if their order deviates by more than this
     * sequence's capacity. Those packets would need to be re-sent.
     */
    boolean addIncoming(long value)
    {
        if (values.isEmpty())
        {
            values.add(value);
            return true;
        }
        
        if (values.contains(value)) return false;
        
        if (value + capacity < values.last()) return false; //Underflow safe.
        
        values.add(value);
        if (values.size() > capacity) values.pollFirst();
        
        return true;
    }
    
    long incrementOutgoing()
    {
        return outgoing++;
    }
}
