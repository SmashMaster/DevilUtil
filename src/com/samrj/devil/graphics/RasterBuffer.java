package com.samrj.devil.graphics;

import com.samrj.devil.buffer.ByteBuffer;
import com.samrj.devil.math.Util;
import com.samrj.devil.res.FileRes;
import com.samrj.devil.res.Resource;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.*;

/**
 * Class used by DevilUtil for loading images into an OGL-usable format.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class RasterBuffer
{
    // <editor-fold defaultstate="collapsed" desc="Static Methods">
    /**
     * @param baseFormat a base internal format, as in the internalFormat
     *                   argument of the {@code glTexImage2D()} method.
     * @return the number of components in a particular texture format. Supports
     *         only the core profile formats (OpenGL 3.2+).
     */
    public static int getBands(int baseFormat)
    {
        switch (baseFormat)
        {
            case GL11.GL_DEPTH_COMPONENT:
            case GL11.GL_RED: return 1;
                
            case GL30.GL_DEPTH_STENCIL:
            case GL30.GL_RG: return 2;
                
            case GL11.GL_RGB: return 3;
            case GL11.GL_RGBA: return 4;
            
            default: return -1;
        }
    }
    
    /**
     * @param format an OpenGL texture format.
     * @return the base OpenGL internal texture format corresponding with the
     *         given format.
     */
    public static int getBaseFormat(int format)
    {
        switch (format)
        {
            //Forward-compatible formats.
            case GL11.GL_DEPTH_COMPONENT:
            case GL14.GL_DEPTH_COMPONENT16:
            case GL14.GL_DEPTH_COMPONENT24:
            case GL14.GL_DEPTH_COMPONENT32:
            case GL30.GL_DEPTH_COMPONENT32F: return GL11.GL_DEPTH_COMPONENT;
                
            case GL11.GL_RED:
            case GL30.GL_R8:
            case GL30.GL_R16:
            case GL30.GL_R16F:
            case GL30.GL_R16I:
            case GL30.GL_R32F:
            case GL30.GL_R32I: return GL11.GL_RED;
                
            case GL30.GL_DEPTH_STENCIL:
            case GL30.GL_DEPTH24_STENCIL8:
            case GL30.GL_DEPTH32F_STENCIL8: return GL30.GL_DEPTH_STENCIL;
                
            case GL30.GL_RG:
            case GL30.GL_RG8:
            case GL30.GL_RG16:
            case GL30.GL_RG16F:
            case GL30.GL_RG16I:
            case GL30.GL_RG32F:
            case GL30.GL_RG32I: return GL30.GL_RG;
                
            case GL11.GL_RGB:
            case GL11.GL_RGB8:
            case GL11.GL_RGB16:
            case GL30.GL_RGB16F:
            case GL30.GL_RGB16I:
            case GL30.GL_RGB32F:
            case GL30.GL_RGB32I: return GL11.GL_RGB;
                
            case GL11.GL_RGBA:
            case GL11.GL_RGBA8:
            case GL11.GL_RGBA16:
            case GL30.GL_RGBA16F:
            case GL30.GL_RGBA16I:
            case GL30.GL_RGBA32F:
            case GL30.GL_RGBA32I: return GL11.GL_RGBA;
                
            default: return -1;
        }
    }
    /**
     * @param bands the number of color channels for a format.
     * @return the 'best guess' format compatible with the given number of
     *         bands.
     */
    public static int getDefaultFormat(int bands)
    {
        switch (bands)
        {
            case 1: return GL11.GL_RED;
            case 2: return GL30.GL_RG;
            case 3: return GL11.GL_RGB;
            case 4: return GL11.GL_RGBA;
                
            default: return -1;
        }
    }
    
    /**
     * @param format an OpenGL texture format.
     * @return the primitive data type associated with the given OpenGL format.
     */
    public static Util.PrimType getPrimType(int format)
    {
        switch (format)
        {
            case GL30.GL_R8:
            case GL30.GL_RG8:
            case GL11.GL_RGB8:
            case GL11.GL_RGBA8: return Util.PrimType.BYTE;
            
            case GL14.GL_DEPTH_COMPONENT16:
            case GL30.GL_R16:
            case GL30.GL_RG16:
            case GL11.GL_RGB16:
            case GL11.GL_RGBA16: return Util.PrimType.CHAR;
            
            case GL30.GL_R16I:
            case GL30.GL_RG16I:
            case GL30.GL_RGB16I:
            case GL30.GL_RGBA16I: return Util.PrimType.SHORT;
            
            case GL30.GL_DEPTH_COMPONENT32F:
            case GL30.GL_R32F:
            case GL30.GL_RG32F:
            case GL30.GL_RGB32F:
            case GL30.GL_RGBA32F: return Util.PrimType.FLOAT;
            
            case GL30.GL_R32I:
            case GL30.GL_RG32I:
            case GL30.GL_RGB32I:
            case GL30.GL_RGBA32I: return Util.PrimType.INT;
                
            default: return null;
        }
    }
    // </editor-fold>
    
    public final int width, height, format, baseFormat, bands;
    private ByteBuffer buffer;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public RasterBuffer(int width, int height, int format, ByteBuffer b)
    {
        this.width = width;
        this.height = height;
        this.format = format;
        baseFormat = getBaseFormat(format);
        bands = getBands(baseFormat);
        if (bands == -1) throw new IllegalArgumentException("Illegal format specified.");
        
        int length = width*height*bands;
        if (b.size() != length) throw new IllegalArgumentException("Illegal input buffer size " + b.size() + ", expected size " + length);
        buffer = new ByteBuffer(length);
        buffer.put(b);
    }
    
    public RasterBuffer(int width, int height, int format)
    {
        this.width = width;
        this.height = height;
        this.format = format;
        baseFormat = getBaseFormat(format);
        bands = getBands(baseFormat);
        if (bands == -1) throw new IllegalArgumentException("Illegal format specified.");
        
        int length = width*height*bands;
        buffer = new ByteBuffer(length);
        buffer.put(new byte[length]);
    }
    
    public RasterBuffer(int format, Raster raster)
    {
        width = raster.getWidth();
        height = raster.getHeight();
        this.format = format;
        baseFormat = getBaseFormat(format);
        bands = getBands(baseFormat);
        if (bands == -1) throw new IllegalArgumentException("Illegal format specified.");
        
        
        int imageBands = raster.getNumBands();
        if (bands != imageBands) throw new TextureLoadException("Cannot load "
                + bands + " band image as " + imageBands + " band texture.");
        
        load(raster);
    }
    
    public RasterBuffer(Raster raster)
    {
        width = raster.getWidth();
        height = raster.getHeight();
        bands = raster.getNumBands();
        format = getDefaultFormat(bands);
        if (format == -1) throw new IllegalArgumentException("No default format for " + bands + " band image.");
        baseFormat = getBaseFormat(format);
        
        load(raster);
    }
    
    public RasterBuffer(int format, BufferedImage image)
    {
        this(image.getRaster());
    }
    
    public RasterBuffer(BufferedImage image)
    {
        this(image.getRaster());
    }
    
    public RasterBuffer(int format, InputStream in) throws IOException
    {
        this(format, ImageIO.read(in));
        in.close();
    }
    
    public RasterBuffer(InputStream in) throws IOException
    {
        this(ImageIO.read(in));
        in.close();
    }
    
    public RasterBuffer(int format, Resource path) throws IOException
    {
        this(format, path.open());
    }
    
    public RasterBuffer(Resource path) throws IOException
    {
        this(path.open());
    }
    
    public RasterBuffer(int format, File f) throws IOException
    {
        this(format, FileRes.find(f));
    }
    
    public RasterBuffer(File f) throws IOException
    {
        this(FileRes.find(f));
    }
    
    public RasterBuffer(int format, String path) throws IOException
    {
        this(format, Resource.find(path));
    }
    
    public RasterBuffer(String path) throws IOException
    {
        this(Resource.find(path));
    }
    // </editor-fold>
    
    private void load(Raster raster)
    {
        buffer = new ByteBuffer(width*height*bands);
        
        for (int y=height-1; y>=0; y--)
            for (int x=0; x<width; x++)
                for (int b=0; b<bands; b++)
                    buffer.put((byte)raster.getSample(x, y, b));
    }
    
    /**
     * Prepares the underlying buffer to be read entirely.
     * 
     * @return the java.nio buffer associated with this RasterBuffer.
     */
    public java.nio.ByteBuffer read()
    {
        return buffer.get();
    }
}