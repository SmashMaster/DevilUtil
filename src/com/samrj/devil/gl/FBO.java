package com.samrj.devil.gl;

import com.samrj.devil.graphics.GLTexture2D;

/**
 * OpenGL framebuffer object wrapper/emulator interface.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface FBO
{
    public void attachTexture(GLTexture2D texture);
    void bind();
    void unbind();
    void delete();
}
