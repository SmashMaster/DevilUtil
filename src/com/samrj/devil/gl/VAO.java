package com.samrj.devil.gl;

import com.samrj.devil.util.IdentitySet;
import com.samrj.devil.util.Pair;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
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
final class VAO
{
    private static Map<Pair<VertexData, ShaderProgram>, VAO> vaos;
    private static Map<VertexData, Set<VAO>> datas;
    private static Map<ShaderProgram, Set<VAO>> shaders;
    
    static void init()
    {
        vaos = new HashMap<>();
        datas = new IdentityHashMap<>();
        shaders = new IdentityHashMap<>();
    }
    
    private static <T> void addVAO(Map<T, Set<VAO>> map, T obj, VAO vao)
    {
        Set<VAO> set = map.get(obj);
        if (set == null)
        {
            set = new IdentitySet<>();
            map.put(obj, set);
        }
        set.add(vao);
    }
    
    static void bindFor(VertexData data, ShaderProgram shader, Runnable r)
    {
        Pair<VertexData, ShaderProgram> pair = new Pair<>(data, shader);
        VAO vao = vaos.get(pair);
        if (vao == null)
        {
            vao = new VAO(pair);
            vaos.put(pair, vao);
            addVAO(datas, data, vao);
            addVAO(shaders, shader, vao);
        }
        else vao.bind();
        r.run();
        vao.unbind();
    }
    
    static void delete(DGLObj object)
    {
        if (object instanceof VertexData)
        {
            Set<VAO> dataSet = datas.remove((VertexData)object);
            if (dataSet != null) for (VAO vao : dataSet)
            {
                vaos.remove(vao.pair);
                Set<VAO> set = shaders.get(vao.pair.b);
                set.remove(vao);
                if (set.isEmpty()) shaders.remove(vao.pair.b);
                vao.delete();
            }
        }
        else if (object instanceof ShaderProgram)
        {
            Set<VAO> shaderSet = shaders.remove((ShaderProgram)object);
            if (shaderSet != null) for (VAO vao : shaderSet)
            {
                vaos.remove(vao.pair);
                Set<VAO> set = datas.get(vao.pair.a);
                set.remove(vao);
                if (set.isEmpty()) datas.remove(vao.pair.a);
                vao.delete();
            }
        }
    }
    
    static void terminate()
    {
        vaos = null;
        datas = null;
        shaders = null;
    }
    
    private final int id;
    private final Pair<VertexData, ShaderProgram> pair;
    
    private VAO(Pair<VertexData, ShaderProgram> pair)
    {
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL30)
            throw new UnsupportedOperationException("Vertex arrays unsupported in OpenGL < 3.0");
        id = GL30.glGenVertexArrays();
        this.pair = pair;
        
        bind();
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, pair.a.vbo());
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, pair.a.ibo());
        
        for (ShaderProgram.Attribute satt : pair.b.getAttributes())
        {
            VertexData.Attribute att = pair.a.getAttribute(satt.name);
            
            if (att != null)
            {
                AttributeType type = att.getType();
                for (int layer=0; layer<type.layers; layer++)
                {
                    int location = satt.location + layer;
                    GL20.glEnableVertexAttribArray(location);
                    vertexAttribPointer(location, type, att.getStride(),
                                        att.getOffset() + layer*type.size);
                }
            }
        }
    }
    
    private void vertexAttribPointer(int index, AttributeType type, int stride, long pointerOffset)
    {
        if (type.isInteger) GL30.nglVertexAttribIPointer(index, type.components, type.glComponent, stride, pointerOffset);
        else GL20.nglVertexAttribPointer(index, type.components, type.glComponent, false, stride, pointerOffset);
    }
    
    private void bind()
    {
        GL30.glBindVertexArray(id);
    }
    
    private void unbind()
    {
        GL30.glBindVertexArray(0);
    }

    private void delete()
    {
        GL30.glDeleteVertexArrays(id);
    }
}
