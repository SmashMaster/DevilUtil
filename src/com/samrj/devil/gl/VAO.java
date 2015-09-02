package com.samrj.devil.gl;

/**
 * OpenGL VAO wrapper/emulator interface.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class VAO
{
    VAO()
    {
    }
    
    public abstract void enableVertexAttribArray(int index);
    public abstract void disableVertexAttribArray(int index);
    public abstract void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset);
    public abstract void bindElementArrayBuffer(int buffer);
    abstract void bind();
    abstract void unbind();
    abstract void delete();
}
