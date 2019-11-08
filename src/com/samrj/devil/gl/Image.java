/*
 * Copyright (c) 2019 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.gl;

import com.samrj.devil.math.Util.PrimType;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.*;

/**
 * Raster image buffer. Used to load or generate images to main memory. Does not
 * rely on OpenGL in any way, but does store image data in an OpenGL-readable
 * format.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class Image extends DGLObj
{
    /**
     * Returns the primitive type of the given raster, or null if the raster is
     * not bufferable.
     * 
     * @param raster The raster to get the primitive type of.
     * @return The primitive type of the given raster.
     */
    public static PrimType getType(Raster raster)
    {
        switch (raster.getSampleModel().getDataType())
        {
            case DataBuffer.TYPE_BYTE:   return PrimType.BYTE;
            case DataBuffer.TYPE_USHORT: return PrimType.CHAR;
            case DataBuffer.TYPE_SHORT:  return PrimType.SHORT;
            case DataBuffer.TYPE_INT:    return PrimType.INT;
            case DataBuffer.TYPE_FLOAT:  return PrimType.FLOAT;
            default: return null;
        }
    }
    
    /**
     * Returns true if the given primitive type is bufferable as an image, or
     * false if it is not.
     * 
     * @param type Any primitive type.
     * @return Whether the given type is bufferable as an image.
     */
    private static boolean typeSupported(PrimType type)
    {
        switch (type)
        {
            case BYTE: case CHAR: case SHORT: case INT: case FLOAT: return true;
            default: return false;
        }
    }
    
    public final int width, height, bands;
    public final PrimType type;
    public final int size;
    public final ByteBuffer buffer;
    
    private boolean deleted;
    
    Image(int width, int height, int bands, PrimType type)
    {
        DGL.checkState();
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Illegal dimensions specified.");
        if (bands <= 0 || bands > 4)
            throw new IllegalArgumentException("Illegal number of image bands specified.");
        if (!typeSupported(type))
            throw new IllegalArgumentException("Illegal primitive type " + type + " specified.");
        
        this.width = width;
        this.height = height;
        this.bands = bands;
        this.type = type;
        size = width*height*bands*type.size;
        
        buffer = memAlloc(size);
    }
    
    /**
     * Returns the buffer offset, in bytes, of the given band and pixel.
     * 
     * @param x The x coordinate of the pixel.
     * @param y The y coordinate of the pixel.
     * @param b The color band.
     * @return The index of the given band and pixel.
     */
    public int index(int x, int y, int b)
    {
        y = height - 1 - y;
        return (b + (x + y*width)*bands)*type.size;
    }
    
    /**
     * Performs the given function for each pixel and band in this image, in the
     * order they should be buffered by OpenGL.
     * 
     * @param s The sample function to use per pixel/band.
     * @return This image.
     */
    public Image sample(Sampler s)
    {
        for (int y=height-1; y>=0; y--)
            for (int x=0; x<width; x++)
                for (int b=0; b<bands; b++) s.sample(x, y, b);
        return this;
    }
    
    /**
     * Returns the value for the given pixel coordinates and band. The range of
     * the returned value depends on the format of the image. Byte values are
     * considered to be unsigned.
     * 
     * @param x The x coordinate of the pixel.
     * @param y The y coordinate of the pixel.
     * @param b The color band.
     * @return The value at the specified pixel and band.
     */
    public double get(int x, int y, int b)
    {
        int index = index(x, y, b);
        switch (type)
        {
            case BYTE: return Byte.toUnsignedInt(buffer.get(index));
            case CHAR: return buffer.getChar(index);
            case SHORT: return buffer.getShort(index);
            case INT: return buffer.getInt(index);
            case FLOAT: return buffer.getFloat(index);
            default: throw new IllegalArgumentException();
        }
    }
    
    /**
     * Buffers the given image raster into this. Throws an exception if the
     * format of the raster is not compatible with this image buffer, or if the
     * raster's size is not equal to this buffer's size.
     * 
     * @param raster The image raster to buffer.
     * @return This image.
     */
    public Image buffer(Raster raster)
    {
        if (deleted) throw new IllegalStateException("Image buffer deleted.");
        if (raster.getWidth() != width || raster.getHeight() != height)
            throw new IllegalArgumentException("Raster size must equal image buffer size.");
        if (raster.getNumBands() < bands)
            throw new IllegalArgumentException("Not enough bands supplied.");
        if (getType(raster) != type) throw new IllegalArgumentException("Illegal raster format supplied.");
        
        Sampler s;
        switch (type)
        {
            case BYTE: s = (x, y, b) -> buffer.put((byte)raster.getSample(x, y, b)); break;
            case CHAR: s = (x, y, b) -> buffer.putChar((char)raster.getSample(x, y, b)); break;
            case SHORT: s = (x, y, b) -> buffer.putShort((short)raster.getSample(x, y, b)); break;
            case INT: s = (x, y, b) -> buffer.putInt(raster.getSample(x, y, b)); break;
            case FLOAT: s = (x, y, b) -> buffer.putFloat(raster.getSampleFloat(x, y, b)); break;
            default: throw new IllegalArgumentException();
        }
        
        buffer.clear();
        sample(s);
        buffer.flip();
        return this;
    }
    
    /**
     * Writes data into this image based on the given function.
     * 
     * @param shader The function to use.
     * @return This image.
     */
    public Image shade(Shader shader)
    {
        if (deleted) throw new IllegalStateException("Image buffer deleted.");
        
        Sampler s;
        switch (type)
        {
            case BYTE: s = (x, y, b) -> buffer.put((byte)shader.shade(x, y, b)); break;
            case CHAR: s = (x, y, b) -> buffer.putChar((char)shader.shade(x, y, b)); break;
            case SHORT: s = (x, y, b) -> buffer.putShort((short)shader.shade(x, y, b)); break;
            case INT: s = (x, y, b) -> buffer.putInt((int)shader.shade(x, y, b)); break;
            case FLOAT: s = (x, y, b) -> buffer.putFloat((float)shader.shade(x, y, b)); break;
            default: throw new IllegalArgumentException();
        }
        
        buffer.clear();
        sample(s);
        buffer.flip();
        return this;
    }
    
    /**
     * Writes data from this image into the given writable raster.
     * 
     * @param raster A writable raster.
     * @return This image.
     */
    public Image write(WritableRaster raster)
    {
        if (deleted) throw new IllegalStateException("Image buffer deleted.");
        if (raster.getWidth() != width || raster.getHeight() != height)
            throw new IllegalArgumentException("Raster size must equal image buffer size.");
        if (raster.getNumBands() < bands)
            throw new IllegalArgumentException("Not enough bands supplied.");
//        if (getType(raster) != type) throw new IllegalArgumentException("Illegal raster format supplied.");
        
        Sampler s;
        switch (type)
        {
            case BYTE: s = (x, y, b) -> raster.setSample(x, y, b, buffer.get()); break;
            case CHAR: s = (x, y, b) -> raster.setSample(x, y, b, buffer.getChar()); break;
            case SHORT: s = (x, y, b) -> raster.setSample(x, y, b, buffer.getShort()); break;
            case INT: s = (x, y, b) -> raster.setSample(x, y, b, buffer.getInt()); break;
            case FLOAT: s = (x, y, b) -> raster.setSample(x, y, b, buffer.getFloat()); break;
            default: throw new IllegalArgumentException();
        }
        
        buffer.rewind();
        sample(s);
        buffer.rewind();
        return this;
    }
    
    /**
     * Creates a new buffered image for this image data. This image data must be
     * in a byte format, and have 3 or 4 bands.
     * 
     * @return A new BufferedImage.
     */
    public BufferedImage toBufferedImage()
    {
        if (deleted) throw new IllegalStateException("Image buffer deleted.");
        if (type != PrimType.BYTE) throw new IllegalStateException("Image buffer must be in byte format.");
        if (bands != 3 && bands != 4) throw new IllegalStateException("Image must have 4 or fewer bands.");
        
        int biType = bands == 3 ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage out = new BufferedImage(width, height, biType);
        write(out.getRaster());
        return out;
    }
    
    /**
     * @return The native memory location for this image buffer. Unsafe!
     */
    public long address()
    {
        return memAddress(buffer);
    }
    
    /**
     * @return Whether or not this image buffer has been deleted.
     */
    public boolean deleted()
    {
        return deleted;
    }
    
    @Override
    void delete()
    {
        memFree(buffer);
        deleted = true;
    }
    
    @Override
    public String toString()
    {
        return "[w:" + width + ", h: " + height + ", b:" + bands + " type: " + type + "]";
    }
    
    /**
     * Functional interface for any per-pixel-per-channel operation.
     */
    @FunctionalInterface
    public static interface Sampler
    {
        void sample(int x, int y, int b);
    }
    
    /**
     * Functional interface for any per-pixel-per-channel write operation.
     */
    @FunctionalInterface
    public static interface Shader
    {
        double shade(int x, int y, int b);
    }
}
