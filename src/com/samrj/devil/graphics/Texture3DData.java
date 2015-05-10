package com.samrj.devil.graphics;

import com.samrj.devil.buffer.BufferUtil;
import java.nio.ByteBuffer;

/**
 * 3D texture data class for OpenGL.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Texture3DData
{
    public final int width, height, depth, format, baseFormat, bands;
    private ByteBuffer buffer;
    
//    public Texture3DData(int width, int height, int depth, int format, Buffer b)
//    {
//        this.width = width;
//        this.height = height;
//        this.depth = depth;
//        this.format = format;
//        baseFormat = TexUtil.getBaseFormat(format);
//        bands = TexUtil.getBands(baseFormat);
//        if (bands == -1) throw new IllegalArgumentException("Illegal format specified.");
//        
//        int length = width*height*depth*bands*b.getType().size;
//        if (b.size() != length) throw new IllegalArgumentException("Illegal input buffer size " + b.size() + ", expected size " + length);
//        buffer = new ByteBuffer(length);
//        b.get();
//        buffer.put(b.byteBuffer());
//    }
    
    public Texture3DData(int width, int height, int depth, int format)
    {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.format = format;
        baseFormat = TexUtil.getBaseFormat(format);
        bands = TexUtil.getBands(baseFormat);
        if (bands == -1) throw new IllegalArgumentException("Illegal format specified.");
        
        int length = width*height*depth*bands;
        buffer = BufferUtil.createByteBuffer(length);
        buffer.put(new byte[length]);
    }
    
    /**
     * Prepares the underlying buffer to be read entirely.
     * 
     * @return the java.nio buffer associated with this RasterBuffer.
     */
    public ByteBuffer read()
    {
        buffer.rewind();
        return buffer;
    }
    
    public GLTexture3D makeTexture3D(Texture3DParams params)
    {
        return new GLTexture3D(this, params);
    }
    
    public GLTexture3D makeTexture3D()
    {
        return new GLTexture3D(this);
    }
}
