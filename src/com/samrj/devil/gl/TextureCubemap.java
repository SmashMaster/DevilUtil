package com.samrj.devil.gl;

import com.samrj.devil.graphics.TexUtil;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Util.PrimType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;

public final class TextureCubemap extends Texture<TextureCubemap>
{
    private int size;
    
    TextureCubemap()
    {
        super(GL13.GL_TEXTURE_CUBE_MAP, GL13.GL_TEXTURE_BINDING_CUBE_MAP);
        size = 1;
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
        
        Util.PrimType primType = TexUtil.getPrimType(format);
        int glPrimType = primType != null ? TexUtil.getGLPrim(primType) : GL11.GL_UNSIGNED_BYTE;
        
        this.size = size;
        
        int oldID = tempBind();
        for (int i=0; i<6; i++) GL11.nglTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                0, format, size, size, 0, baseFormat, glPrimType, MemoryUtil.NULL);
        tempUnbind(oldID);
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
        PrimType primType = images[0].type;
        int glPrimType = TexUtil.getGLPrim(primType);
        int dataFormat = TexUtil.getBaseFormat(format);
        int bands = TexUtil.getBands(dataFormat);
        
        for (Image image : images)
            if (image.width != size || image.height != size ||
                    image.bands != bands || image.type != primType)
                throw new IllegalArgumentException();
        
        int oldID = tempBind();
        for (int i=0; i<6; i++) GL11.nglTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                0, format, size, size, 0, dataFormat, glPrimType, images[i].address());
        tempUnbind(oldID);
        return getThis();
    }
}
