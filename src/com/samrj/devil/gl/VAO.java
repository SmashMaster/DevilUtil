package com.samrj.devil.gl;

/**
 * OpenGL VAO wrapper/emulator superclass.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
interface VAO
{
    void enableVertexAttribArray(int index);
    void disableVertexAttribArray(int index);
    void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset);
    void bindElementArrayBuffer(int buffer);
    void bind();
    void unbind();
    void delete();
}
