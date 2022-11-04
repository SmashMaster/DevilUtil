package com.samrj.devil.gl;

import org.lwjgl.opengl.GL15C;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.glGetInteger;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindBufferBase;
import static org.lwjgl.opengl.GL30C.glGenBuffers;
import static org.lwjgl.opengl.GL42C.*;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.system.MemoryUtil.memAddress0;

/**
 * Class for miscellaneous buffer types.
 *
 * @author angle
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class BufferObject extends DGLObj
{
    private static final Map<Integer, Integer> BINDINGS = new HashMap<>(); //Target -> Binding

    static
    {
        BINDINGS.put(GL_ARRAY_BUFFER, GL_ARRAY_BUFFER_BINDING);
        BINDINGS.put(GL_ATOMIC_COUNTER_BUFFER, GL_ATOMIC_COUNTER_BUFFER_BINDING);
        BINDINGS.put(GL_COPY_READ_BUFFER, GL_COPY_READ_BUFFER_BINDING);
        BINDINGS.put(GL_COPY_WRITE_BUFFER, GL_COPY_WRITE_BUFFER_BINDING);
        BINDINGS.put(GL_DRAW_INDIRECT_BUFFER, GL_DRAW_INDIRECT_BUFFER_BINDING);
        BINDINGS.put(GL_DISPATCH_INDIRECT_BUFFER, GL_DISPATCH_INDIRECT_BUFFER_BINDING);
        BINDINGS.put(GL_ELEMENT_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER_BINDING);
        BINDINGS.put(GL_PIXEL_PACK_BUFFER, GL_PIXEL_PACK_BUFFER_BINDING);
        BINDINGS.put(GL_PIXEL_UNPACK_BUFFER, GL_PIXEL_UNPACK_BUFFER_BINDING);
        BINDINGS.put(GL_SHADER_STORAGE_BUFFER, GL_SHADER_STORAGE_BUFFER_BINDING);
        BINDINGS.put(GL_TRANSFORM_FEEDBACK_BUFFER, GL_TRANSFORM_FEEDBACK_BUFFER_BINDING);
        BINDINGS.put(GL_UNIFORM_BUFFER, GL_UNIFORM_BUFFER_BINDING);
    }

    public final int id, target;
    private boolean deleted;

    BufferObject(int target)
    {
        DGL.checkState();
        this.target = target;
        id = glGenBuffers();
    }

    public final boolean isBound()
    {
        return !deleted && glGetInteger(BINDINGS.get(target)) == id;
    }

    final int tempBind()
    {
        int oldID = glGetInteger(BINDINGS.get(target));
        if (oldID != id) glBindBuffer(target, id);
        return oldID;
    }

    final void tempUnbind(int oldID)
    {
        if (oldID == id) return;
        glBindBuffer(target, oldID);
    }

    public void bindBufferBase(int binding)
    {
        if (deleted) throw new IllegalStateException("Cannot bind deleted buffer.");
        glBindBufferBase(target, binding, id);
    }

    public void bindBufferBase(int target, int binding)
    {
        if (deleted) throw new IllegalStateException("Cannot bind deleted buffer.");
        glBindBufferBase(target, binding, id);
    }

    public void bindBuffer()
    {
        if (deleted) throw new IllegalStateException("Cannot bind deleted buffer.");
        glBindBuffer(target, id);
    }

    @Deprecated
    public void unbindBuffer()
    {
        glBindBuffer(target, 0);
    }

    public void bufferData(long bytes, int usage)
    {
        int oldID = tempBind();
        glBufferData(target, bytes, usage);
        tempUnbind(oldID);
    }

    public void bufferData(ByteBuffer data, int usage)
    {
        int oldID = tempBind();
        glBufferData(target, data, usage);
        tempUnbind(oldID);
    }

    public void bufferSubData(long offset, ByteBuffer data)
    {
        int oldID = tempBind();
        glBufferSubData(target, offset, data);
        tempUnbind(oldID);
    }

    public void bufferSubData(long offset, int length, ByteBuffer data)
    {
        int oldID = tempBind();
        nglBufferSubData(target, offset, length, memAddress0(data));
        tempUnbind(oldID);
    }

    public void bufferData(int[] data, int usage)
    {
        int oldID = tempBind();
        glBufferData(target, data, usage);
        tempUnbind(oldID);
    }

    @Override
    void delete()
    {
        if (deleted) return;
        GL15C.glDeleteBuffers(id);
        deleted = true;
    }
}
