/*
 * Copyright (c) 2015 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.graphics;

import com.samrj.devil.gl.AttributeType;
import static com.samrj.devil.gl.AttributeType.*;
import com.samrj.devil.gl.VAO;
import com.samrj.devil.gl.VertexData;
import com.samrj.devil.model.Mesh;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

/**
 * Utility OpenGL wrapper for DevilModel meshes.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class MeshDrawer implements VertexData
{
    private static final int POSITION = 0, NORMAL = 1, TANGENT = 2, UV = 3,
                             COLOR = 4, GROUPS = 5, WEIGHTS = 6;
    
    private final Mesh mesh;
    private final Attribute[] attributes;
    private final Map<String, Attribute> nameMap;
    private int vbo, ibo;
    
    public MeshDrawer(Mesh mesh)
    {
        this.mesh = mesh;
        attributes = new Attribute[7];
        
        //Set up attributes.
        int verts = mesh.numVertices;
        int offset = 0;
        
        attributes[POSITION] = new Attribute(
                VEC3,
                (offset += 0),
                true);
        
        attributes[NORMAL] = new Attribute(
                VEC3,
                (offset += verts*3*4),
                mesh.hasNormals);
        
        attributes[TANGENT] = new Attribute(
                VEC3,
                (offset += mesh.hasNormals ? verts*3*4 : 0),
                mesh.hasTangents);
        
        attributes[UV] = new Attribute(
                VEC2,
                (offset += mesh.hasTangents ? verts*3*4 : 0),
                mesh.uvLayers.length > 0);
        
        attributes[COLOR] = new Attribute(
                VEC3,
                (offset += verts*mesh.uvLayers.length*2*4),
                mesh.colorLayers.length > 0);
        
        AttributeType groupsType, weightType;
        switch (mesh.numGroups)
        {
            case 1: groupsType = INT; weightType = FLOAT; break;
            case 2: groupsType = VEC2I; weightType = VEC2; break;
            case 3: groupsType = VEC3I; weightType = VEC3; break;
            case 4: groupsType = VEC4I; weightType = VEC4; break;
            default: groupsType = null; weightType = null; break;
        }
        
        attributes[GROUPS] = new Attribute(
                groupsType,
                (offset += verts*mesh.colorLayers.length*3*4),
                mesh.numGroups > 0);
        
        attributes[WEIGHTS] = new Attribute(
                weightType,
                (offset += verts*mesh.numGroups*4),
                mesh.numGroups > 0);
        
        vbo = GL15.glGenBuffers();
        int prevBinding = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.nglBufferData(GL15.GL_ARRAY_BUFFER, mesh.vertexBlock.size, mesh.vertexBlock.address, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevBinding);
        
        ibo = GL15.glGenBuffers();
        prevBinding = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL15.nglBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.indexBlock.size, mesh.indexBlock.address, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevBinding);
        
        nameMap = new HashMap<>();
    }
    
    public void setPositionName(String name)
    {
        attributes[POSITION].name = name;
        nameMap.put(name, attributes[POSITION]);
    }
    
    public void setNormalName(String name)
    {
        attributes[NORMAL].name = name;
        nameMap.put(name, attributes[NORMAL]);
    }
    
    public void setTangentName(String name)
    {
        attributes[TANGENT].name = name;
        nameMap.put(name, attributes[TANGENT]);
    }
    
    public void setUVName(String name)
    {
        attributes[UV].name = name;
        nameMap.put(name, attributes[UV]);
    }
    
    public void setColorName(String name)
    {
        attributes[COLOR].name = name;
        nameMap.put(name, attributes[COLOR]);
    }
    
    public void setGroupsName(String name)
    {
        attributes[GROUPS].name = name;
        nameMap.put(name, attributes[GROUPS]);
    }
    
    public void setWeightsName(String name)
    {
        attributes[WEIGHTS].name = name;
        nameMap.put(name, attributes[WEIGHTS]);
    }
    
    public void draw()
    {
        GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.numTriangles*3, GL11.GL_UNSIGNED_INT, 0);
    }
    
    public void drawInstanced(int primcount)
    {
        GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, mesh.numTriangles*3, GL11.GL_UNSIGNED_INT, 0, primcount);
    }
    
    public void destroy()
    {
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(ibo);
        
        vbo = -1;
        ibo = -1;
    }

    @Override
    public int vbo()
    {
        return vbo;
    }

    @Override
    public int ibo()
    {
        return ibo;
    }

    @Override
    public Iterable<VertexData.Attribute> attributes()
    {
        List<VertexData.Attribute> out = new ArrayList<>(attributes.length);
        for (Attribute att : attributes) if (att.enabled) out.add(att);
        return out;
    }

    @Override
    public VertexData.Attribute getAttribute(String name)
    {
        return nameMap.get(name);
    }
    
    @Override
    public int numVertices()
    {
        return mesh.numVertices;
    }

    @Override
    public int numIndices()
    {
        return mesh.numTriangles*3;
    }
    
    private class Attribute implements VertexData.Attribute
    {
        private String name;
        private final AttributeType type;
        private final int offset;
        private final boolean enabled;
        
        private Attribute(AttributeType type, int offset, boolean enabled)
        {
            this.type = type;
            this.offset = offset;
            this.enabled = enabled;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public AttributeType getType()
        {
            return type;
        }

        @Override
        public int getStride()
        {
            return 0;
        }
        
        @Override
        public int getOffset()
        {
            return offset;
        }
    }
}
