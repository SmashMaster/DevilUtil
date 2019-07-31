package com.samrj.devil.gl;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 * Data container for compressed images.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ImageCompressed extends DGLObj
{
    public final int width, height;
    public final int format;
    
    private int size = -1;
    private ByteBuffer buffer;
    private boolean deleted;
    
    /**
     * Creates a new compressed image container, but does not allocate any
     * space yet.
     * 
     * @param width The width of this image.
     * @param height The height of this image.
     * @param format The OpenGL compressed texture format to use, ex:
     *        EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT
     */
    ImageCompressed(int width, int height, int format)
    {
        DGL.checkState();
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Illegal dimensions specified.");
        
        this.width = width;
        this.height = height;
        this.format = format;
    }
    
    /**
     * Allocates storage for this image.
     * 
     * @param size The number of bytes to allocate.
     * @return This image.
     */
    public ImageCompressed allocate(int size)
    {
        if (deleted) throw new IllegalStateException("Image deleted.");
        if (buffer != null) throw new IllegalStateException("Buffer already allocated.");
        
        this.size = size;
        buffer = MemoryUtil.memAlloc(size);
        return this;
    }
    
    /**
     * @return Creates a view buffer for this image's underlying data.
     */
    public ByteBuffer buffer()
    {
        return buffer;
    }
    
    /**
     * @return The native memory location for this image buffer.
     */
    public long address()
    {
        return MemoryUtil.memAddressSafe(buffer);
    }
    
    /**
     * @return The size of this image data in bytes, or -1 if unallocated.
     */
    public int size()
    {
        return size;
    }
    
    /**
     * @return Whether this image has been deleted.
     */
    public boolean deleted()
    {
        return deleted;
    }
    
    @Override
    void delete()
    {
        size = -1;
        MemoryUtil.memFree(buffer);
        buffer = null;
        deleted = true;
    }
}
