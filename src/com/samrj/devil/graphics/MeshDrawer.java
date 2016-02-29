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
import com.samrj.devil.gl.DGL;
import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.gl.VAO;
import com.samrj.devil.model.Mesh;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 * Utility OpenGL wrapper for DevilModel meshes.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class MeshDrawer
{
    private static final int POSITION = 0, NORMAL = 1, TANGENT = 2, UV = 3,
                             COLOR = 4, GROUPS = 5, WEIGHTS = 6;
    
    private final Mesh mesh;
    private final Attribute[] attributes;
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
        
        attributes[GROUPS] = new Attribute(
                VEC4I,
                (offset += verts*mesh.colorLayers.length*3*4),
                mesh.numGroups > 0,
                mesh.numGroups,
                GL11.GL_FLOAT);
        
        attributes[WEIGHTS] = new Attribute(
                VEC4,
                (offset += verts*mesh.numGroups*4),
                mesh.numGroups > 0,
                mesh.numGroups,
                GL11.GL_FLOAT);
        
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
    }
    
    public void setPositionName(String name)
    {
        attributes[POSITION].name = name;
    }
    
    public void setNormalName(String name)
    {
        attributes[NORMAL].name = name;
    }
    
    public void setTangentName(String name)
    {
        attributes[TANGENT].name = name;
    }
    
    public void setUVName(String name)
    {
        attributes[UV].name = name;
    }
    
    public void setColorName(String name)
    {
        attributes[COLOR].name = name;
    }
    
    public void setGroupsName(String name)
    {
        attributes[GROUPS].name = name;
    }
    
    public void setWeightsName(String name)
    {
        attributes[WEIGHTS].name = name;
    }
    
    public VAO link(ShaderProgram program)
    {
        VAO oldVAO = DGL.currentVAO();
        
        VAO vao = DGL.genVAO();
        DGL.bindVAO(vao);
        
        int prevBinding = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        vao.bindElementArrayBuffer(ibo);
        
        for (int i=0; i<7; i++)
        {
            Attribute att = attributes[i];
            if (!att.enabled || att.name == null) continue;
            
            ShaderProgram.Attribute satt = program.getAttribute(att.name);
            if (satt == null || satt.type != att.type) continue;
            
            att.enable(vao, satt.location);
        }
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevBinding);
        
        DGL.bindVAO(oldVAO);
        return vao;
    }
    
    public void draw()
    {
        GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.numTriangles*3, GL11.GL_UNSIGNED_INT, 0);
    }
    
    public void delete()
    {
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(ibo);
        
        vbo = -1;
        ibo = -1;
    }
    
    private class Attribute
    {
        private String name;
        private final AttributeType type;
        private final int offset;
        private final boolean enabled;
        private final int components;
        private final int componentType;
        
        private Attribute(AttributeType type, int offset, boolean enabled, int components, int componentType)
        {
            this.type = type;
            this.offset = offset;
            this.enabled = enabled;
            this.components = components;
            this.componentType = componentType;
        }
        
        private Attribute(AttributeType type, int offset, boolean enabled)
        {
            this(type, offset, enabled, type.components, type.glComponent);
        }
        
        private void enable(VAO vao, int location)
        {
            vao.enableVertexAttribArray(location);
            vao.vertexAttribPointer(location,
                                    components,
                                    componentType,
                                    false,
                                    0,
                                    offset);
        }
    }
}
