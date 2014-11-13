package com.samrj.devil.gl.texture;

import com.samrj.devil.gl.util.Primitive;
import static com.samrj.devil.gl.util.Primitive.*;
import java.awt.image.Raster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Loads and stores 2D image data in a OpenGL-readable format, ready to be sent
 * to the GPU.
 * 
 * @author samjohns
 */
public class ImageBuffer
{
    public static enum Format
    {
        RED (GL11.GL_RED,  1),
        RG  (GL30.GL_RG,   2),
        RGB (GL11.GL_RGB,  3),
        RGBA(GL11.GL_RGBA, 4);
        
        public final int glEnum, bands;
        
        private Format(int glEnum, int bands)
        {
            this.glEnum = glEnum;
            this.bands = bands;
        }
    }
    
    private static Format formatFromBands(int bands)
    {
        switch (bands)
        {
            case 1: return Format.RED;
            case 2: return Format.RG;
            case 3: return Format.RGB;
            case 4: return Format.RGBA;
            default: throw new IllegalArgumentException(
                    "Illegal number of bands: " + bands);
        }
    }
    
    public final int width, height;
    public final Format format;
    public final Primitive type;
    private final ByteBuffer buffer;
    
    public ImageBuffer(int width, int height, Format format, Primitive type)
    {
        this.width = width;
        this.height = height;
        this.format = format;
        this.type = type;
        int bytes = width*height*format.bands*type.size;
        buffer = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
    }
    
    public ImageBuffer(int width, int height, Format format, byte[] data)
    {
        this(width, height, format, BYTE);
        ensureCapacity(sizeof(data));
        buffer.put(data);
        buffer.rewind();
    }
    
    public ImageBuffer(int width, int height, Format format, short[] data)
    {
        this(width, height, format, SHORT);
        ensureCapacity(sizeof(data));
        buffer.asShortBuffer().put(data);
        buffer.rewind();
    }
    
    public ImageBuffer(int width, int height, Format format, int[] data)
    {
        this(width, height, format, INT);
        ensureCapacity(sizeof(data));
        buffer.asIntBuffer().put(data);
        buffer.rewind();
    }
    
    public ImageBuffer(int width, int height, Format format, float[] data)
    {
        this(width, height, format, FLOAT);
        ensureCapacity(sizeof(data));
        buffer.asFloatBuffer().put(data);
        buffer.rewind();
    }
    
    public ImageBuffer(Raster raster)
    {
        this(raster.getWidth(), raster.getHeight(),
             formatFromBands(raster.getNumBands()), BYTE);
        
        for (int y = height - 1; y >= 0; y--)
            for (int x = 0; x < width; x++)
                for (int b = 0; b < format.bands; b++)
                    buffer.put((byte)raster.getSample(x, y, b));
        buffer.rewind();
    }
    
    private void ensureCapacity(int size)
    {
        if (buffer.capacity() != size) throw new IllegalArgumentException(
            "Expected " + buffer.capacity() + " bytes of image data, got " + size);
    }
    
    private void ensureType(Primitive type)
    {
        if (this.type != type) throw new IllegalArgumentException(
            "Expected primitive type " + this.type + ", got " + type);
    }
}
