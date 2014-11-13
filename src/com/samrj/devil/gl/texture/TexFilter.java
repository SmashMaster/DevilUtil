package com.samrj.devil.gl.texture;

import org.lwjgl.opengl.GL11;

public enum TexFilter
{
    NEAREST               (GL11.GL_NEAREST, false),
    LINEAR                (GL11.GL_LINEAR, false),
    NEAREST_MIPMAP_NEAREST(GL11.GL_NEAREST_MIPMAP_NEAREST, true),
    LINEAR_MIPMAP_NEAREST (GL11.GL_LINEAR_MIPMAP_NEAREST, true),
    NEAREST_MIPMAP_LINEAR (GL11.GL_NEAREST_MIPMAP_LINEAR, true),
    LINEAR_MIPMAP_LINEAR  (GL11.GL_LINEAR_MIPMAP_LINEAR, true);

    final int glEnum;
    final boolean mipmapped;

    private TexFilter(int glEnum, boolean mipmapped)
    {
        this.glEnum = glEnum;
        this.mipmapped = mipmapped;
    }
}
