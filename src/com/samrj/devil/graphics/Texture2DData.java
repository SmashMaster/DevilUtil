package com.samrj.devil.graphics;

import com.samrj.devil.buffer.Buffer;
import com.samrj.devil.buffer.ByteBuffer;
import com.samrj.devil.res.FileRes;
import com.samrj.devil.res.Resource;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Class used by DevilUtil for loading images into an OGL-usable format.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Texture2DData
{
    public final int width, height, format, baseFormat, bands;
    private ByteBuffer buffer;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Texture2DData(int width, int height, int format, Buffer b)
    {
        this.width = width;
        this.height = height;
        this.format = format;
        baseFormat = TexUtil.getBaseFormat(format);
        bands = TexUtil.getBands(baseFormat);
        if (bands == -1) throw new IllegalArgumentException("Illegal format specified.");
        
        int length = width*height*bands*b.getType().size;
        if (b.size() != length) throw new IllegalArgumentException("Illegal input buffer size " + b.size() + ", expected size " + length);
        buffer = new ByteBuffer(length);
        b.get();
        buffer.put(b.byteBuffer());
    }
    
    public Texture2DData(int width, int height, int format)
    {
        this.width = width;
        this.height = height;
        this.format = format;
        baseFormat = TexUtil.getBaseFormat(format);
        bands = TexUtil.getBands(baseFormat);
        if (bands == -1) throw new IllegalArgumentException("Illegal format specified.");
        
        int length = width*height*bands;
        buffer = new ByteBuffer(length);
        buffer.put(new byte[length]);
    }
    
    public Texture2DData(int format, Raster raster)
    {
        width = raster.getWidth();
        height = raster.getHeight();
        this.format = format;
        baseFormat = TexUtil.getBaseFormat(format);
        bands = TexUtil.getBands(baseFormat);
        if (bands == -1) throw new IllegalArgumentException("Illegal format specified.");
        
        
        int imageBands = raster.getNumBands();
        if (bands != imageBands) throw new TextureLoadException("Cannot load "
                + bands + " band image as " + imageBands + " band texture.");
        
        load(raster);
    }
    
    public Texture2DData(Raster raster)
    {
        width = raster.getWidth();
        height = raster.getHeight();
        bands = raster.getNumBands();
        format = TexUtil.getDefaultFormat(bands);
        if (format == -1) throw new IllegalArgumentException("No default format for " + bands + " band image.");
        baseFormat = TexUtil.getBaseFormat(format);
        
        load(raster);
    }
    
    public Texture2DData(int format, BufferedImage image)
    {
        this(image.getRaster());
    }
    
    public Texture2DData(BufferedImage image)
    {
        this(image.getRaster());
    }
    
    public Texture2DData(int format, InputStream in) throws IOException
    {
        this(format, ImageIO.read(in));
        in.close();
    }
    
    public Texture2DData(InputStream in) throws IOException
    {
        this(ImageIO.read(in));
        in.close();
    }
    
    public Texture2DData(int format, Resource path) throws IOException
    {
        this(format, path.open());
    }
    
    public Texture2DData(Resource path) throws IOException
    {
        this(path.open());
    }
    
    public Texture2DData(int format, File f) throws IOException
    {
        this(format, FileRes.find(f));
    }
    
    public Texture2DData(File f) throws IOException
    {
        this(FileRes.find(f));
    }
    
    public Texture2DData(int format, String path) throws IOException
    {
        this(format, Resource.find(path));
    }
    
    public Texture2DData(String path) throws IOException
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
    
    public GLTexture2D makeTexture2D(Texture2DParams params)
    {
        return new GLTexture2D(this, params);
    }
    
    public GLTexture2D makeTexture2D()
    {
        return new GLTexture2D(this);
    }
}