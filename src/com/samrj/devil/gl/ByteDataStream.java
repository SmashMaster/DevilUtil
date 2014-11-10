package com.samrj.devil.gl;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteDataStream extends ByteArrayOutputStream
{
    public ByteDataStream()
    {
        super();
    }
    
    public ByteDataStream(int size)
    {
        super(size);
    }
    
    private final byte writeBuffer[] = new byte[8];
    
    public final void writeInt(int v)
    {
        writeBuffer[3] = (byte)(v >>> 24);
        writeBuffer[2] = (byte)(v >>> 16);
        writeBuffer[1] = (byte)(v >>>  8);
        writeBuffer[0] = (byte)v;
        
        write(writeBuffer, 0, 4);
    }
    
    public final void writeLong(long v)
    {
        writeBuffer[7] = (byte)(v >>> 56);
        writeBuffer[6] = (byte)(v >>> 48);
        writeBuffer[5] = (byte)(v >>> 40);
        writeBuffer[4] = (byte)(v >>> 32);
        writeBuffer[3] = (byte)(v >>> 24);
        writeBuffer[2] = (byte)(v >>> 16);
        writeBuffer[1] = (byte)(v >>>  8);
        writeBuffer[0] = (byte)v;
        
        write(writeBuffer, 0, 8);
    }

    public final void writeFloat(float v)
    {
        writeInt(Float.floatToIntBits(v));
    }

    public final void writeDouble(double v)
    {
        writeLong(Double.doubleToLongBits(v));
    }
    
    public final void writeTo(ByteDataStream stream)
    {
        stream.write(buf, 0, count);
    }
    
    public ByteBuffer toBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(count);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(buf, 0, count);
        buffer.rewind();
        return buffer;
    }
}
