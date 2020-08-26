package com.samrj.devil.gl;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;

/**
 * OpenGL Vertex Array Object wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
final class VAO extends DGLObj
{
    private static Map<Binding, VAO> vaos;
    private static Map<VAOBindable, Binding> bindables;
    private static VertexAttribDivisorMethod vertexAttribDivisorMethod;
    
    static void init()
    {
        vaos = new HashMap<>();
        bindables = new IdentityHashMap<>();
        
        GLCapabilities caps = GL.getCapabilities();

        if (caps.OpenGL33) vertexAttribDivisorMethod = GL33C::glVertexAttribDivisor;
        else if (caps.GL_ARB_instanced_arrays) vertexAttribDivisorMethod = ARBInstancedArrays::glVertexAttribDivisorARB;
    }
    
    static void bindFor(InstanceData iData, VertexData vData, ShaderProgram shader, Runnable r)
    {
        Binding binding = new Binding(iData, vData, shader);
        VAO vao = vaos.get(binding);
        if (vao == null)
        {
            vao = new VAO(binding);
            vaos.put(binding, vao);
            if (iData != null) bindables.put(iData, binding);
            bindables.put(vData, binding);
            bindables.put(shader, binding);
        }
        else vao.bind();
        r.run();
        vao.unbind();
    }
    
    static void delete(VAOBindable bindable)
    {
        Binding binding = bindables.get(bindable);
        if (binding != null)
        {
            VAO vao = vaos.remove(binding);
            vao.delete();
            bindables.remove(binding.iData);
            bindables.remove(binding.vData);
            bindables.remove(binding.shader);
        }
    }
    
    static void terminate()
    {
        if (DGL.getDebugLeakTracking()) for (VAO vao : vaos.values()) vao.debugLeakTrace();
        vaos = null;
    }
    
    private final int id;
    
    private VAO(Binding binding)
    {
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL30)
            throw new UnsupportedOperationException("Vertex arrays unsupported in OpenGL < 3.0");
        id = glGenVertexArrays();
        
        bind();
        
        glBindBuffer(GL_ARRAY_BUFFER, binding.vData.vbo());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, binding.vData.ibo());
        
        for (ShaderProgram.Attribute satt : binding.shader.getAttributes())
        {
            InstanceData.Attribute iAtt = binding.iData != null ? binding.iData.getAttribute(satt.name) : null;
            
            if (iAtt != null)
            {
                AttributeType type = iAtt.getType();
                for (int layer=0; layer<type.layers; layer++)
                {
                    int location = satt.location + layer;
                    glEnableVertexAttribArray(location);
                    vertexAttribPointer(location, type, iAtt.getStride(), iAtt.getOffset() + layer*type.size);
                    vertexAttribDivisorMethod.accept(location, iAtt.getDivisor());
                }
                continue;
            }
            
            VertexData.Attribute vAtt = binding.vData.getAttribute(satt.name);
            
            if (vAtt != null)
            {
                AttributeType type = vAtt.getType();
                for (int layer=0; layer<type.layers; layer++)
                {
                    int location = satt.location + layer;
                    glEnableVertexAttribArray(location);
                    vertexAttribPointer(location, type, vAtt.getStride(), vAtt.getOffset() + layer*type.size);
                }
            }
        }
    }
    
    private void vertexAttribPointer(int index, AttributeType type, int stride, long pointerOffset)
    {
        if (type.isInteger) nglVertexAttribIPointer(index, type.components, type.glComponent, stride, pointerOffset);
        else nglVertexAttribPointer(index, type.components, type.glComponent, false, stride, pointerOffset);
    }
    
    private void bind()
    {
        glBindVertexArray(id);
    }
    
    private void unbind()
    {
        glBindVertexArray(0);
    }

    @Override
    void delete()
    {
        glDeleteVertexArrays(id);
    }
    
    @FunctionalInterface
    private static interface VertexAttribDivisorMethod
    {
        public void accept(int index, int divisor);
    }
    
    private static class Binding
    {
        public final InstanceData iData;
        public final VertexData vData;
        public final ShaderProgram shader;

        public Binding(InstanceData iData, VertexData vData, ShaderProgram shader)
        {
            this.iData = iData;
            this.vData = vData;
            this.shader = shader;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 43 * hash + Objects.hashCode(this.iData);
            hash = 43 * hash + Objects.hashCode(this.vData);
            hash = 43 * hash + Objects.hashCode(this.shader);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final Binding other = (Binding)obj;
            if (!Objects.equals(this.iData, other.iData)) return false;
            if (!Objects.equals(this.vData, other.vData)) return false;
            if (!Objects.equals(this.shader, other.shader)) return false;
            return true;
        }
    }
}
