package com.samrj.devil.gl.texture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

public enum TexWrap
{
    CLAMP_TO_EDGE(GL12.GL_CLAMP_TO_EDGE),
    MIRRORED_REPEAT(GL14.GL_MIRRORED_REPEAT),
    REPEAT(GL11.GL_REPEAT);
    
    public final int glEnum;
    
    private TexWrap(int glEnum)
    {
        this.glEnum = glEnum;
    }
}
