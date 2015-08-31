package com.samrj.devil.gl;

/**
 * OpenGL VAO wrapper/emulator interface.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface VAO
{
    public void enableVertexAttribArray(int index);
    public void disableVertexAttribArray(int index);
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset);
    public void bindElementArrayBuffer(int buffer);
    void bind();
    void unbind();
    void delete();
}
