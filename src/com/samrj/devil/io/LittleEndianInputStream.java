package com.samrj.devil.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream which can read little-endian data. Intentionally violates the
 * specification of DataInput. Can also read null-terminated ASCII strings.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class LittleEndianInputStream extends DataInputStream
{
    public LittleEndianInputStream(InputStream in)
    {
        super(in);
    }
    
    public final short readLittleShort() throws IOException
    {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)(ch1 + (ch2 << 8));
    }

    public final int readLittleUnsignedShort() throws IOException
    {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return ch1 + (ch2 << 8);
    }
    
    public final char readLittleChar() throws IOException
    {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)(ch1 + (ch2 << 8));
    }
    
    public final int readLittleInt() throws IOException
    {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return (ch1 + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    private final byte readBuffer[] = new byte[8];

    public final long readLittleLong() throws IOException
    {
        readFully(readBuffer, 0, 8);
        return ((readBuffer[0] & 0xFF) +
                ((readBuffer[1] & 0xFF) << 8) +
                ((readBuffer[2] & 0xFF) << 16) +
                ((long)(readBuffer[3] & 0xFF) << 24) +
                ((long)(readBuffer[4] & 0xFF) << 32) +
                ((long)(readBuffer[5] & 0xFF) << 40) +
                ((long)(readBuffer[6] & 0xFF) <<  48) +
                ((long)readBuffer[7] <<  56));
    }
    
    public final float readLittleFloat() throws IOException
    {
        return Float.intBitsToFloat(readLittleInt());
    }

    public final double readLittleDouble() throws IOException
    {
        return Double.longBitsToDouble(readLittleLong());
    }
    
    public final String readNullTermStr() throws IOException
    {
        StringBuilder builder = new StringBuilder();
        
        while (true)
        {
            char c = (char)in.read();
            if (c == '\0') break;
            builder.append(c);
        }
        
        return builder.toString();
    }
}
