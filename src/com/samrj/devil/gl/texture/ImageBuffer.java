package com.samrj.devil.gl.texture;

import com.samrj.devil.gl.util.Primitive;
import static com.samrj.devil.gl.util.Primitive.*;
import java.awt.image.Raster;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.ImageIO;

/**
 * Loads and stores 2D image data in a OpenGL-readable format, ready to be sent
 * to the GPU.
 * 
 * @author samjohns
 */
public final class ImageBuffer
{
    private static InputStream open(String path) throws IOException
    {
        InputStream stream = ClassLoader.getSystemResourceAsStream(path);
        if (stream == null) throw new FileNotFoundException(path);
        return stream;
    }
    
    public final int width, height;
    public final TexFormat format;
    private final int bytes;
    private ByteBuffer buffer = null;
    
    public ImageBuffer(int width, int height, TexFormat format)
    {
        this.width = width;
        this.height = height;
        this.format = format;
        bytes = width*height*format.bands*format.type.size;
        buffer = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
    }
    
    public ImageBuffer(int width, int height, TexFormat format, byte[] data)
    {
        this(width, height, format);
        ensureDataType(BYTE);
        ensureCapacity(sizeof(data));
        
        buffer.put(data);
    }
    
    public ImageBuffer(int width, int height, TexFormat format, short[] data)
    {
        this(width, height, format);
        ensureDataType(SHORT);
        ensureCapacity(sizeof(data));
        
        buffer.asShortBuffer().put(data);
    }
    
    public ImageBuffer(int width, int height, TexFormat format, int[] data)
    {
        this(width, height, format);
        ensureDataType(INT);
        ensureCapacity(sizeof(data));
        
        buffer.asIntBuffer().put(data);
    }
    
    public ImageBuffer(int width, int height, TexFormat format, float[] data)
    {
        this(width, height, format);
        ensureDataType(FLOAT);
        ensureCapacity(sizeof(data));
        
        buffer.asFloatBuffer().put(data);
    }
    
    public ImageBuffer(Raster raster)
    {
        this(raster.getWidth(), raster.getHeight(),
             TexFormat.getFormat(raster.getNumBands(), BYTE));
        
        for (int y = height - 1; y >= 0; y--)
            for (int x = 0; x < width; x++)
                for (int b = 0; b < format.bands; b++)
                    buffer.put((byte)raster.getSample(x, y, b));
    }
    
    public ImageBuffer(String path) throws IOException
    {
        this(ImageIO.read(open(path)).getRaster());
    }
    
    private void ensureCapacity(int size)
    {
        if (bytes != size) throw new IllegalArgumentException(
            "Expected " + buffer.capacity() + " bytes of image data, got " + size + "!");
    }
    
    private void ensureDataType(Primitive type)
    {
        if (format.type != type) throw new IllegalArgumentException(
            "Expected image data type " + format.type + ", got " + type + "!");
    }
    
    ByteBuffer getBuffer()
    {
        buffer.rewind();
        return buffer;
    }
}
