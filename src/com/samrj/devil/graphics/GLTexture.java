package com.samrj.devil.graphics;

import com.samrj.devil.buffer.ByteBuffer;
import com.samrj.devil.math.Util.PrimType;
import com.samrj.devil.math.Vector2f;
import com.samrj.devil.res.FileRes;
import com.samrj.devil.res.Resource;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

/**
 * Handles the allocation, loading, manipulation, and deletion of OpenGL texture
 * objects. Provides an object-oriented view of OpenGL's state-based textures.
 * 
 * @author Samuel Johnson (SmashMaster)
 */

public class GLTexture
{
    // <editor-fold defaultstate="collapsed" desc="Static Methods">
    /**
     * Returns the number of components in a particular texture format. Supports
     * only the core profile formats (OpenGL 3.2+).
     * 
     * @param baseFormat a base internal format, as in the internalFormat
     *                   argument of the {@code glTexImage2D()} method.
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
     * Returns the base internal format that corresponds with the given texture
     * format.
     * 
     * @param format a texture format, as in the format argument of the
     *               {@code glTexImage2D()} method.
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
                
            default: throw new IllegalArgumentException();
        }
    }
    
    public static int getDefaultFormat(int bands)
    {
        switch (bands)
        {
            case 1: return GL11.GL_RED;
            case 2: return GL30.GL_RG;
            case 3: return GL11.GL_RGB;
            case 4: return GL11.GL_RGBA;
                
            default: throw new IllegalArgumentException();
        }
    }
    
    public static PrimType getPrimType(int format)
    {
        switch (format)
        {
            //Valid formats with no corresponding primitive type.
            case GL11.GL_DEPTH_COMPONENT:
            case GL14.GL_DEPTH_COMPONENT24:
            case GL14.GL_DEPTH_COMPONENT32:
            case GL11.GL_RED:
            case GL30.GL_R16F:
            case GL30.GL_DEPTH_STENCIL:
            case GL30.GL_DEPTH24_STENCIL8:
            case GL30.GL_DEPTH32F_STENCIL8:
            case GL30.GL_RG:
            case GL30.GL_RG16F:
            case GL11.GL_RGB:
            case GL30.GL_RGB16F:
            case GL11.GL_RGBA:
            case GL30.GL_RGBA16F: return null;
            
            case GL30.GL_R8:
            case GL30.GL_RG8:
            case GL11.GL_RGB8:
            case GL11.GL_RGBA8: return PrimType.BYTE;
            
            case GL14.GL_DEPTH_COMPONENT16:
            case GL30.GL_R16:
            case GL30.GL_RG16:
            case GL11.GL_RGB16:
            case GL11.GL_RGBA16: return PrimType.CHAR;
            
            case GL30.GL_R16I:
            case GL30.GL_RG16I:
            case GL30.GL_RGB16I:
            case GL30.GL_RGBA16I: return PrimType.SHORT;
            
            case GL30.GL_DEPTH_COMPONENT32F:
            case GL30.GL_R32F:
            case GL30.GL_RG32F:
            case GL30.GL_RGB32F:
            case GL30.GL_RGBA32F: return PrimType.FLOAT;
            
            case GL30.GL_R32I:
            case GL30.GL_RG32I:
            case GL30.GL_RGB32I:
            case GL30.GL_RGBA32I: return PrimType.INT;
                
            default: throw new IllegalArgumentException();
        }
    }
    // </editor-fold>
    
    private Resource path;
    private int width, height;
    private int id = -1;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public GLTexture(int format, ByteBuffer b, int width, int height)
    {
        this.width = width; this.height = height;
        load(format, b);
    }
    
    public GLTexture(int format, int width, int height)
    {
        this.width = width; this.height = height;
        int length = width*height*getBands(getBaseFormat(format));
        
        ByteBuffer b = new ByteBuffer(length);
        b.put(new byte[length]);
        load(format, b);
    }
    
    public GLTexture(int format, Raster raster)
    {
        width = raster.getWidth(); height = raster.getHeight();
        
        int bands = getBands(getBaseFormat(format));
        if (bands == -1) throw new IllegalArgumentException("Illegal format specified.");
        
        int imageBands = raster.getNumBands();
        if (bands != imageBands) throw new TextureLoadException("Cannot load "
                + bands + " band image as " + imageBands + " band texture.");
        
        load(format, bands, raster);
    }
    
    public GLTexture(Raster raster)
    {
        width = raster.getWidth(); height = raster.getHeight();
        
        int bands = raster.getNumBands();
        
        int format = getDefaultFormat(bands);
        if (format == -1) throw new IllegalArgumentException("No default format for " + bands + " band image.");
        
        load(format, bands, raster);
    }
    
    public GLTexture(int format, BufferedImage image)
    {
        this(format, image.getRaster());
    }
    
    public GLTexture(BufferedImage image)
    {
        this(image.getRaster());
    }
    
    public GLTexture(int format, InputStream in) throws IOException
    {
        this(format, ImageIO.read(in));
        in.close();
    }
    
    public GLTexture(InputStream in) throws IOException
    {
        this(ImageIO.read(in));
        in.close();
    }
    
    public GLTexture(int format, Resource path) throws IOException
    {
        this(format, path.open());
        this.path = path;
    }
    
    public GLTexture(Resource path) throws IOException
    {
        this(path.open());
        this.path = path;
    }
    
    public GLTexture(int format, File f) throws IOException
    {
        this(format, FileRes.find(f));
    }
    
    public GLTexture(File f) throws IOException
    {
        this(FileRes.find(f));
    }
    
    public GLTexture(int format, String path) throws IOException
    {
        this(format, Resource.find(path));
    }
    
    public GLTexture(String path) throws IOException
    {
        this(Resource.find(path));
    }
    // </editor-fold>
    
    private void load(int format, int bands, Raster raster)
    {
        ByteBuffer buffer = new ByteBuffer(width*height*bands);
        
        for (int y=height-1; y>=0; y--)
            for (int x=0; x<width; x++)
                for (int b=0; b<bands; b++)
                    buffer.put((byte)raster.getSample(x, y, b));
        
        load(format, buffer);
    }
    
    private void load(int format, ByteBuffer b)
    {
        int baseFormat = getBaseFormat(format);
        int length = width*height*getBands(baseFormat);
        
        if (b.size() != length) throw new IllegalArgumentException("Illegal buffer size " + b.size() + ", expected size " + length);
        
        id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        
        glParam(GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        glParam(GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        glParam(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        glParam(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format, width, height,
                0, baseFormat, GL11.GL_UNSIGNED_BYTE, b.get());
    }
    
    public Resource getPath()
    {
        return path;
    }
    
    public void glBind(int target)
    {
        if (isDeleted()) throw new IllegalStateException("Cannot bind deleted texture.");
        GL11.glBindTexture(target, id);
    }
    
    public void glBind()
    {
        glBind(GL11.GL_TEXTURE_2D);
    }
    
    public void glMultiBind(int target, int i)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
        glBind(target);
    }
    
    public void glMultiBind(int i)
    {
        glMultiBind(GL11.GL_TEXTURE_2D, i);
    }
    
    public void glParam(int name, int value)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, name, value);
    }
    
    public void glParam(int name, float value)
    {
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, name, value);
    }
    
    public void glDelete()
    {
        id = -1;
        GL11.glDeleteTextures(id);
    }
    
    public boolean isDeleted()
    {
        return id < 0;
    }
    
    public boolean glIsBound()
    {
        return GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D) == id;
    }
    
    public int id()
    {
        return id;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public int getHeight()
    {
        return height;
    }
    
    public Vector2f size()
    {
        return new Vector2f(width, height);
    }
}