package com.samrj.devil.gl;

import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;

import java.util.*;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.nglVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;

/**
 * OpenGL Vertex Array Object wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
final class VAO extends DGLObj
{
    private static Map<ShaderProgram, Map<VertexData, Map<VertexData, VAO>>> shaderMap;
    private static Map<VAOBindable, Set<Binding>> bindables;
    private static VertexAttribDivisorMethod vertexAttribDivisorMethod;
    
    static void init()
    {
        shaderMap = new IdentityHashMap<>();
        bindables = new IdentityHashMap<>();
        
        GLCapabilities caps = GL.getCapabilities();

        if (caps.OpenGL33) vertexAttribDivisorMethod = GL33C::glVertexAttribDivisor;
        else if (caps.GL_ARB_instanced_arrays) vertexAttribDivisorMethod = ARBInstancedArrays::glVertexAttribDivisorARB;
    }
    
    private static void addBindable(VAOBindable bindable, Binding binding)
    {
        if (bindable == null) return;
        Set<Binding> set = bindables.get(bindable);
        if (set == null)
        {
            set = Collections.newSetFromMap(new HashMap<>());
            bindables.put(bindable, set);
        }
        set.add(binding);
    }
    
    static void bindFor(VertexData iData, VertexData vData, ShaderProgram shader, Runnable r)
    {
        Map<VertexData, Map<VertexData, VAO>> instanceMap = shaderMap.get(shader);
        if (instanceMap == null)
        {
            instanceMap = new IdentityHashMap<>();
            shaderMap.put(shader, instanceMap);
        }

        Map<VertexData, VAO> vertexMap = instanceMap.get(iData);
        if (vertexMap == null)
        {
            vertexMap = new IdentityHashMap<>();
            instanceMap.put(iData, vertexMap);
        }

        VAO vao = vertexMap.get(vData);
        if (vao == null)
        {
            Binding binding = new Binding(iData, vData, shader);
            vao = new VAO(binding);
            vertexMap.put(vData, vao);
            addBindable(iData, binding);
            addBindable(vData, binding);
            addBindable(shader, binding);
        }
        else vao.bind();
        r.run();
        vao.unbind();
    }
    
    private static void removeBindable(VAOBindable bindable, Binding binding)
    {
        if (bindable == null) return;
        Set<Binding> set = bindables.get(bindable);
        set.remove(binding);
        if (set.isEmpty()) bindables.remove(bindable);
    }
    
    static void delete(VAOBindable bindable)
    {
        Set<Binding> bindings = bindables.get(bindable);
        Set<Binding> removedBindings = new HashSet<>();
        
        if (bindings != null) for (Binding binding : bindings)
        {
            Map<VertexData, Map<VertexData, VAO>> instanceMap = shaderMap.get(binding.shader);
            Map<VertexData, VAO> vertexMap = instanceMap.get(binding.iData);
            VAO vao = vertexMap.remove(binding.vData);
            if (vertexMap.isEmpty()) instanceMap.remove(binding.iData);
            if (instanceMap.isEmpty()) shaderMap.remove(binding.shader);

            vao.delete();
            removedBindings.add(binding);
        }
        
        for (Binding binding : removedBindings)
        {
            removeBindable(binding.iData, binding);
            removeBindable(binding.vData, binding);
            removeBindable(binding.shader, binding);
        }
    }
    
    static void terminate()
    {
        if (DGL.getDebugLeakTracking())
            for (Map<VertexData, Map<VertexData, VAO>> instanceMap : shaderMap.values())
                for (Map<VertexData, VAO> vertexMap : instanceMap.values())
                    for (VAO vao : vertexMap.values())
                        vao.debugLeakTrace();
        shaderMap = null;
    }
    
    private final int id;
    
    private VAO(Binding binding)
    {
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL30)
            throw new UnsupportedOperationException("Vertex arrays unsupported in OpenGL < 3.0");
        id = glGenVertexArrays();
        
        bind();
        
        //Instance attributes
        if (binding.iData != null)
        {
            glBindBuffer(GL_ARRAY_BUFFER, binding.iData.vbo());
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

            for (ShaderProgram.Attribute satt : binding.shader.getAttributes())
            {
                VertexData.Attribute iAtt = binding.iData.getAttribute(satt.name);
                if (iAtt != null)
                {
                    AttributeType type = iAtt.getType();
                    for (int layer=0; layer<type.layers; layer++)
                    {
                        int location = satt.location + layer;
                        glEnableVertexAttribArray(location);
                        vertexAttribPointer(location, type, iAtt.getStride(), iAtt.getOffset() + layer*type.size);
                        vertexAttribDivisorMethod.accept(location, 1);
                    }
                }
            }
        }
        
        //Vertex attributes
        glBindBuffer(GL_ARRAY_BUFFER, binding.vData.vbo());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, binding.vData.ibo());
        
        for (ShaderProgram.Attribute satt : binding.shader.getAttributes())
        {
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
    private interface VertexAttribDivisorMethod
    {
        void accept(int index, int divisor);
    }

    private record Binding(VertexData iData, VertexData vData, ShaderProgram shader) {}
}
