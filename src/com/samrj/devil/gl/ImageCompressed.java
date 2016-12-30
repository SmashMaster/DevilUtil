package com.samrj.devil.gl;

import com.samrj.devil.io.Memory;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 * Data container for compressed images.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ImageCompressed extends DGLObj
{
    public final int width, height;
    public final int format;
    
    private int size = -1;
    private Memory memory;
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
        if (memory != null) throw new IllegalStateException("Buffer already allocated.");
        
        this.size = size;
        memory = new Memory(size);
        return this;
    }
    
    /**
     * @return Creates a view buffer for this image's underlying data.
     */
    public ByteBuffer buffer()
    {
        return memory != null ? memory.buffer : null;
    }
    
    /**
     * @return The native memory location for this image buffer. Unsafe!
     */
    public long address()
    {
        return memory != null ? memory.address : MemoryUtil.NULL;
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
        memory.free();
        memory = null;
        deleted = true;
    }
}
