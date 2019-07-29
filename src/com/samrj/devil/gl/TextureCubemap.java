package com.samrj.devil.gl;

import com.samrj.devil.graphics.TexUtil;
import com.samrj.devil.math.Util.PrimType;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL13C.*;

/**
 * OpenGL cubemap texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class TextureCubemap extends Texture<TextureCubemap>
{
    private int size;
    
    TextureCubemap()
    {
        super(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_BINDING_CUBE_MAP);
        size = 1;
        
        int oldID = tempBind();
        parami(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        parami(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        parami(GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        parami(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        parami(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        tempUnbind(oldID);
    }
    
    @Override
    TextureCubemap getThis()
    {
        return this;
    }
    
    /**
     * Allocates space on the GPU for a cubemap of the given size and format,
     * but does not upload any information.
     * 
     * @param size The size of each dimension of this cubemap.
     * @param format The format of the image.
     * @return This texture.
     */
    public TextureCubemap image(int size, int format)
    {
        if (size <= 0) throw new IllegalArgumentException("Illegal image dimensions.");
        
        int baseFormat = TexUtil.getBaseFormat(format);
        if (baseFormat == -1) throw new IllegalArgumentException("Illegal image format.");
        
        int primType = TexUtil.getPrimitiveType(format);
        this.size = size;
        
        int oldID = tempBind();
        for (int i=0; i<6; i++) nglTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                0, format, size, size, 0, baseFormat, primType, MemoryUtil.NULL);
        tempUnbind(oldID);
        
        setVRAMUsage(TexUtil.getBits(format)*size*size*6);
        
        return this;
    }
    
    /**
     * Uploads each of 6 images to the corresponding face on this cubemap. Each
     * image must have exactly the same width, height, and format.
     * 
     * @param images An array of 6 images to upload.
     * @param format The texture format to upload each image as.
     * @return This texture.
     */
    public TextureCubemap image(Image[] images, int format)
    {
        if (images.length != 6) throw new IllegalArgumentException();
        
        size = images[0].width;
        PrimType type = images[0].type;
        int primType = TexUtil.getPrimitiveType(format);
        int dataFormat = TexUtil.getBaseFormat(format);
        int bands = TexUtil.getBands(dataFormat);
        
        for (Image image : images)
            if (image.width != size || image.height != size ||
                    image.bands != bands || image.type != type)
                throw new IllegalArgumentException();
        
        int oldID = tempBind();
        for (int i=0; i<6; i++) nglTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                0, format, size, size, 0, dataFormat, primType, images[i].address());
        tempUnbind(oldID);
        
        setVRAMUsage(TexUtil.getBits(format)*size*size*6);
        
        return getThis();
    }
}
