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
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL30)
            throw new UnsupportedOperationException("Vertex arrays unsupported in OpenGL < 3.0");
        id = GL30.glGenVertexArrays();
    }
    
    private void ensureBound()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        if (DGL.currentVAO() != this) throw new IllegalStateException("VAO not bound.");
    }
    
    /**
     * Enables a generic vertex attribute array.
     * 
     * @param index The vertex attribute array to enable.
     * @return This vertex array.
     */
    public VAO enableVertexAttribArray(int index)
    {
        ensureBound();
        GL20.glEnableVertexAttribArray(index);
        return this;
    }

    /**
     * Disables a generic vertex attribute array.
     * 
     * @param index The vertex attribute array to disable.
     * @return This vertex array.
     */
    public VAO disableVertexAttribArray(int index)
    {
        ensureBound();
        GL20.glDisableVertexAttribArray(index);
        return this;
    }
    
    /**
     * Specifies the location and organization of a vertex attribute array.
     * 
     * @param index The index of the generic vertex attribute to be modified.
     * @param size The number of values per vertex that are stored in the array.
     * @param type The data type of each component in the array.
     * @param normalized Whether fixed-point data values should be normalized or
     *        converted directly as fixed-point values when they are accessed.
     * @param stride the byte offset between consecutive generic vertex
     *        attributes. If stride is 0, the generic vertex attributes are
     *        understood to be tightly packed in the array.
     * @param pointerOffset the vertex attribute data or the offset of the first
     *        component of the first generic vertex attribute in the array in
     *        the data store of the buffer currently bound to the
     *        {@link GL15#GL_ARRAY_BUFFER ARRAY_BUFFER} target.
     * @return This vertex array.
     */
    public VAO vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset)
    {
        ensureBound();
        GL20.nglVertexAttribPointer(index, size, type, normalized, stride, pointerOffset);
        return this;
    }

    /**
     * Binds the given buffer to the {@link GL15#GL_ELEMENT_ARRAY_BUFFER
     * GL_ELEMENT_ARRAY_BUFFER} target.
     * 
     * @param buffer The name of the buffer object to bind.
     * @return This vertex array.
     */
    public VAO bindElementArrayBuffer(int buffer)
    {
        ensureBound();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer);
        return this;
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
     * @return This vertex array.
     */
    public VAO link(VertexData data, ShaderProgram shader)
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
        return this;
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
        if (DGL.currentVAO() == this) DGL.bindVAO(null);
        GL30.glDeleteVertexArrays(id);
        deleted = true;
    }
}
