package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * OpenGL VAO wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class VAO extends DGLObj
{
    private final int id;
    private boolean deleted;
    
    VAO()
    {
        id = GL30.glGenVertexArrays();
    }
    
    private void ensureBound()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        if (DGL.currentVAO() != this) throw new IllegalStateException("VAO not bound.");
    }
    
    public void enableVertexAttribArray(int index)
    {
        ensureBound();
        GL20.glEnableVertexAttribArray(index);
    }

    public void disableVertexAttribArray(int index)
    {
        ensureBound();
        GL20.glDisableVertexAttribArray(index);
    }
    
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset)
    {
        ensureBound();
        GL20.nglVertexAttribPointer(index, size, type, normalized, stride, pointerOffset);
    }

    public void bindElementArrayBuffer(int buffer)
    {
        ensureBound();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer);
    }
    
    /**
     * Links this the given vertex data to the given shader program, enabling
     * common attributes between the two. This vertex array may then be used to
     * draw the given data with the given program. Will have undefined behavior
     * if called for multiple programs, but may be safely used to link different
     * data to the same programs.
     * 
     * @param data The vertex data to link.
     * @param shader The shader to link.
     */
    public void link(VertexData data, ShaderProgram shader)
    {
        ensureBound();
        
        int prevBinding = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.vbo());
        bindElementArrayBuffer(data.ibo());
        
        for (ShaderProgram.Attribute satt : shader.getAttributes())
        {
            AttributeType type  = satt.type;
            VertexData.Attribute att = data.getAttribute(satt.name);
            
            if (att != null && att.type == type) for (int layer=0; layer<type.layers; layer++)
            {
                int location = satt.location + layer;
                enableVertexAttribArray(location);
                vertexAttribPointer(location,
                                    type.components,
                                    type.glComponent,
                                    false,
                                    data.vertexSize(),
                                    att.offset + layer*type.size);
            }
            else disableVertexAttribArray(satt.location);
        }
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevBinding);
    }

    void bind()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        GL30.glBindVertexArray(id);
    }

    void unbind()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        GL30.glBindVertexArray(0);
    }

    @Override
    void delete()
    {
        if (deleted) return;
        GL30.glDeleteVertexArrays(id);
        deleted = true;
    }
}
