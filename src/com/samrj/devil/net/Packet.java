package com.samrj.devil.net;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A single packet, whether received or sent.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
class Packet
{
    static final int TYPE_FINALIZE = 0;
    static final int TYPE_KEEPALIVE = 1;
    static final int TYPE_MESSAGE = 2;
    static final int TYPE_ACK = 3;
    
    private static final int       TYPE_BITS = 0b11000000;
    private static final int  SHOULD_ACK_BIT = 0b00100000;
    private static final int IS_FRAGMENT_BIT = 0b00010000;
    private static final int    PADDING_BITS = 0b00001111;
    
    /**
     * The time remaining until the information in this packet is useless to the
     * recipient. May be set to Float.POSITIVE_INFINITY for reliable packets.
     */
    float expiry;
    
    /**
     * The sequence number of a packet identifies the order in which packets
     * were sent for a particular stream.
     */
    short sequence;
    
    /**
     * What type of packet this is.
     */
    int type;
    
    
    /**
     * Whether the recipient should acknowledge receipt of this packet.
     * Implicitly false for any type other than TYPE_MESSAGE.
     */
    boolean shouldAck;
    
    /**
     * Whether this is a fragment packet. Fragments are used to split up packets
     * which exceed the maximum transmission unit.
     */
    boolean isFragment;
    short fragmentIndex;
    short fragmentCount;
    
    /**
     * The payload, or data to be delivered, of this packet.
     */
    byte[] payload;
    int payloadOffset;
    int payloadLength;
    
    void fragment(boolean isFragment, int index, int count)
    {
        this.isFragment = isFragment;
        fragmentIndex = (short)index;
        fragmentCount = (short)count;
    }
            
    void payload(byte[] payload)
    {
        this.payload = payload;
        payloadOffset = 0;
        payloadLength = payload.length;
    }
    
    void payload(byte[] payload, int offset, int length)
    {
        this.payload = payload;
        payloadOffset = offset;
        payloadLength = length;
    }
    
    /**
     * Returns the size, in bytes, of this packet's header.
     * @return 
     */
    int getHeaderSize()
    {
        return isFragment ? 7 : 3;
    }
    
    /**
     * Returns the size, in bytes, that this packet will be once buffered.
     */
    int getSize()
    {
        return Math.max(getHeaderSize() + payloadLength, NetUtil.MIN_ENCRYPTED_SIZE);
    }
    
    /**
     * Writes this packet into the provided buffer.
     */
    void write(ByteBuffer buffer)
    {
        buffer.putShort(sequence);
        
        int bits = (type << 6) & TYPE_BITS;
        if (shouldAck) bits |= SHOULD_ACK_BIT;
        if (isFragment) bits |= IS_FRAGMENT_BIT;
        int padding = getSize() - (getHeaderSize() + payloadLength);
        bits |= padding & PADDING_BITS;
        buffer.put((byte)bits);
        
        if (isFragment)
        {
            buffer.putShort(fragmentIndex);
            buffer.putShort(fragmentCount);
        }
        
        for (int i=0; i<padding; i++) buffer.put((byte)0);
        
        if (payloadLength > 0) buffer.put(payload, payloadOffset, payloadLength);
    }
    
    /**
     * Writes this packet into a new byte array.
     */
    byte[] toArray()
    {
        byte[] result = new byte[getSize()];
        write(ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN));
        return result;
    }
    
    /**
     * Reads a packet from the given byte buffer.
     */
    void read(ByteBuffer buffer)
    {
        sequence = buffer.getShort();
        
        int bits = Byte.toUnsignedInt(buffer.get());
        type = (bits & TYPE_BITS) >> 6;
        shouldAck = (bits & SHOULD_ACK_BIT) != 0;
        isFragment = (bits & IS_FRAGMENT_BIT) != 0;
        int padding = bits & PADDING_BITS;
        
        if (isFragment)
        {
            fragmentIndex = buffer.getShort();
            fragmentCount = buffer.getShort();
        }
        
        if (buffer.remaining() > padding)
        {
            buffer.position(buffer.position() + padding);
            
            payloadOffset = 0;
            payloadLength = buffer.remaining();
            payload = new byte[payloadLength];
            buffer.get(payload);
        }
    }
    
    /**
     * Reads a packet from the given byte array.
     */
    void read(byte[] array)
    {
        read(ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN));
    }
    
    /**
     * Returns a new acknowledgment packet that matches this packet's signature.
     */
    Packet makeAck()
    {
        Packet ack = new Packet();
        ack.sequence = sequence;
        ack.type = TYPE_ACK;
        if (isFragment)
        {
            ack.isFragment = true;
            ack.fragmentIndex = fragmentIndex;
            ack.fragmentCount = fragmentCount;
        }
        return ack;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Packet: {");
        builder.append("    Sequence number: ").append(sequence).append('\n');
        builder.append("    Type: ").append(type).append('\n');
        builder.append("    Should ack: ").append(shouldAck).append('\n');
        if (isFragment)
            builder.append("    Fragment ").append(fragmentIndex + 1).append(" of ").append(fragmentCount).append('\n');
        if (payload != null)
            builder.append("    Payload: ").append(payloadLength).append(" bytes\n");
        builder.append("}\n");
        return builder.toString();
    }
    
    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 79 * hash + sequence;
        hash = 79 * hash + (isFragment ? 1 : 0);
        if (isFragment)
        {
            hash = 79 * hash + fragmentIndex;
            hash = 79 * hash + fragmentCount;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Packet other = (Packet)obj;
        if (sequence != other.sequence) return false;
        if (isFragment != other.isFragment) return false;
        if (isFragment)
        {
            if (fragmentIndex != other.fragmentIndex) return false;
            if (fragmentCount != other.fragmentCount) return false;
        }
        return true;
    }
}
