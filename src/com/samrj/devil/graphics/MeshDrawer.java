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
import com.samrj.devil.gl.Profiler;
import com.samrj.devil.gl.VertexData;
import com.samrj.devil.model.Mesh;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 * Utility OpenGL wrapper for DevilModel meshes.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class MeshDrawer implements VertexData
{
    private final Mesh mesh;
    
    private final Attribute position;
    private final Attribute normal;
    private final Attribute uv;
    private final Attribute tangent;
    private final Map<String, Attribute> colors;
    private final Attribute groups;
    private final Attribute weights;
    private final Attribute material;
    
    private final Map<String, Attribute> attributes;
    private int vbo, ibo;
    
    public MeshDrawer(Mesh mesh)
    {
        this.mesh = mesh;
        
        //Set up attributes.
        position = new Attribute(VEC3, mesh.positionOffset, true);
        normal = new Attribute(VEC3, mesh.normalOffset, mesh.hasNormals);
        uv = new Attribute(VEC2, mesh.uvOffset, mesh.uvLayers.length > 0);
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
            default: throw new IllegalArgumentException("Vertex group count over four.");
        }
        
        groups = new Attribute(groupsType, mesh.groupIndexOffset, mesh.numGroups > 0);
        weights = new Attribute(weightType, mesh.groupWeightOffset, mesh.numGroups > 0);
        material = new Attribute(INT, mesh.materialOffset, mesh.hasMaterials);
        
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
        
        attributes = new HashMap<>();
        
        Profiler.addUsedVRAM(mesh.vertexBlock.size*8L);
        Profiler.addUsedVRAM(mesh.indexBlock.size*8L);
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
        setName(uv, name);
    }
    
    public void setColorName(String name)
    {
        if (colors.isEmpty()) return;
        if (colors.size() > 1) throw new IllegalStateException("More then one color layer. Must specify layer name.");
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
    
    public void destroy()
    {
        Profiler.removeUsedVRAM(mesh.vertexBlock.size*8L);
        Profiler.removeUsedVRAM(mesh.indexBlock.size*8L);
        
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
