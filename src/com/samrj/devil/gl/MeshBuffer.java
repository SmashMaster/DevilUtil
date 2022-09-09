/*
 * Copyright (c) 2022 Sam Johnson
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

package com.samrj.devil.gl;

import com.samrj.devil.model.Mesh;

import java.nio.ByteBuffer;
import java.util.*;

import static com.samrj.devil.gl.AttributeType.*;
import static org.lwjgl.opengl.GL11C.glGetInteger;
import static org.lwjgl.opengl.GL15C.*;

/**
 * Utility OpenGL wrapper for DevilModel meshes.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class MeshBuffer extends DGLObj implements VertexData
{
    private final Mesh mesh;
    private final boolean edges;
    
    private final Attribute position;
    private final Attribute normal;
    private final Map<String, Attribute> uvs;
    private final Attribute tangent;
    private final Map<String, Attribute> colors;
    private final Attribute groups;
    private final Attribute weights;
    private final Attribute material;
    
    private final Map<String, Attribute> attributes;
    private int vbo, ibo;
    
    MeshBuffer(Mesh mesh, boolean edges)
    {
        this.mesh = mesh;
        this.edges = edges;
        
        //Set up attributes.
        position = new Attribute(VEC3, mesh.positionOffset, true);
        normal = new Attribute(VEC3, mesh.normalOffset, true);
        uvs = new HashMap<>();
        for (int i=0; i<mesh.uvLayers.length; i++)
        {
            Attribute color =  new Attribute(VEC2, mesh.uvOffsets[i], true);
            uvs.put(mesh.uvLayers[i], color);
        }
        tangent = new Attribute(VEC3, mesh.tangentOffset, mesh.hasTangents);
        colors = new HashMap<>();
        for (int i=0; i<mesh.colorLayers.length; i++)
        {
            Attribute color =  new Attribute(VEC3, mesh.colorOffsets[i], true);
            colors.put(mesh.colorLayers[i], color);
        }
        
        AttributeType groupsType, weightType;
        switch (mesh.numGroups)
        {
            case 0: groupsType = NONE; weightType = NONE; break;
            case 1: groupsType = INT; weightType = FLOAT; break;
            case 2: groupsType = VEC2I; weightType = VEC2; break;
            case 3: groupsType = VEC3I; weightType = VEC3; break;
            case 4: groupsType = VEC4I; weightType = VEC4; break;
            default: throw new IllegalArgumentException("Vertex group count of " + mesh.numGroups + ", limited to four.");
        }
        
        groups = new Attribute(groupsType, mesh.groupIndexOffset, mesh.numGroups > 0);
        weights = new Attribute(weightType, mesh.groupWeightOffset, mesh.numGroups > 0);
        material = new Attribute(INT, mesh.materialOffset, mesh.hasMaterials);
        
        vbo = glGenBuffers();
        int prevBinding = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, mesh.vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, prevBinding);
        
        ByteBuffer indexData = edges ? mesh.edgeIndexData : mesh.indexData;
        
        ibo = glGenBuffers();
        prevBinding = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, prevBinding);
        
        attributes = new HashMap<>();
        
        Profiler.addUsedVRAM(mesh.vertexData.remaining()*8L);
        Profiler.addUsedVRAM(indexData.remaining()*8L);
    }
    
    private void setName(Attribute att, String name)
    {
        att.name = name;
        attributes.put(name, att);
    }
    
    public void setPositionName(String name)
    {
        setName(position, name);
    }
    
    public void setNormalName(String name)
    {
        setName(normal, name);
    }
    
    public void setTangentName(String name)
    {
        setName(tangent, name);
    }
    
    public void setUVName(String name)
    {
        if (uvs.isEmpty()) return;
        if (uvs.size() > 1) throw new IllegalStateException("More than one UV layer. Must specify layer name.");
        Attribute att = uvs.values().iterator().next();
        setName(att, name);
    }
    
    public void setUVName(String layer, String name)
    {
        Attribute att = uvs.get(layer);
        if (att == null) return;
        setName(att, name);
    }
    
    public void setColorName(String name)
    {
        if (colors.isEmpty()) return;
        if (colors.size() > 1) throw new IllegalStateException("More than one color layer. Must specify layer name.");
        Attribute att = colors.values().iterator().next();
        setName(att, name);
    }
    
    public void setColorName(String layer, String name)
    {
        Attribute att = colors.get(layer);
        if (att == null) return;
        setName(att, name);
    }
    
    public void setGroupsName(String name)
    {
        setName(groups, name);
    }
    
    public void setWeightsName(String name)
    {
        setName(weights, name);
    }
    
    public void setMaterialName(String name)
    {
        setName(material, name);
    }
    
    @Override
    void delete()
    {
        Profiler.removeUsedVRAM(mesh.vertexData.remaining()*8L);
        Profiler.removeUsedVRAM((edges ? mesh.edgeIndexData : mesh.indexData).remaining()*8L);
        
        glDeleteBuffers(vbo);
        glDeleteBuffers(ibo);
        
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
    public List<VertexData.Attribute> attributes()
    {
        List<VertexData.Attribute> out = new ArrayList<>(attributes.size());
        for (Attribute att : attributes.values()) if (att.enabled) out.add(att);
        return out;
    }

    @Override
    public VertexData.Attribute getAttribute(String name)
    {
        return attributes.get(name);
    }
    
    @Override
    public int numVertices()
    {
        return mesh.numVertices;
    }

    @Override
    public int numIndices()
    {
        return edges ? mesh.numEdges*2 : mesh.numTriangles*3;
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
