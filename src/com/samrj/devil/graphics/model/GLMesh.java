package com.samrj.devil.graphics.model;

import com.samrj.devil.gl.AttributeType;
import static com.samrj.devil.gl.AttributeType.*;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.gl.VAO;
import com.samrj.devil.gl.VertexBuilder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 * Utility OpenGL wrapper for DevilModel meshes.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLMesh
{
    private static final int POSITION = 0, NORMAL = 1, TANGENT = 2, UV = 3,
                             COLOR = 4, GROUPS = 5, WEIGHTS = 6;
    
    private final Mesh mesh;
    private final Attribute[] attributes;
    private int vertexBuffer, elementBuffer;
    
    public GLMesh(Mesh mesh)
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
                true);
        
        attributes[TANGENT] = new Attribute(
                VEC3,
                (offset += verts*3*4),
                mesh.hasTangents);
        
        attributes[UV] = new Attribute(
                VEC2,
                (offset += verts*(mesh.hasTangents ? 3 : 0)*4),
                mesh.hasUVs);
        
        attributes[COLOR] = new Attribute(
                VEC3,
                (offset += verts*(mesh.hasUVs ? 2 : 0)*4),
                mesh.hasVertexColors);
        
        attributes[GROUPS] = new Attribute(
                VEC4I,
                (offset += verts*(mesh.hasVertexColors ? 3 : 0)*4),
                mesh.numVertexGroups <= 0,
                mesh.numVertexGroups,
                GL11.GL_FLOAT);
        
        attributes[WEIGHTS] = new Attribute(
                VEC4,
                (offset += verts*mesh.numVertexGroups*4),
                mesh.numVertexGroups <= 0,
                mesh.numVertexGroups,
                GL11.GL_FLOAT);
        
        //Set up OpenGL stuff.
        mesh.rewindBuffers();
        
        VertexBuilder oldData = DGL.currentData();
        VAO oldVAO = DGL.currentVAO();
        DGL.bindData(null);
        DGL.bindVAO(null);

        vertexBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.vertexData(), GL15.GL_STATIC_DRAW);
        elementBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.indexData(), GL15.GL_STATIC_DRAW);
        
        DGL.bindData(oldData);
        DGL.bindVAO(oldVAO);
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
        vao.bindElementArrayBuffer(elementBuffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
        
        for (int i=0; i<7; i++)
        {
            Attribute att = attributes[i];
            if (!att.enabled || att.name == null) continue;
            
            ShaderProgram.Attribute satt = program.getAttribute(att.name);
            if (satt == null || satt.type != att.type) continue;
            
            att.enable(vao, satt.location);
        }
        
        DGL.bindVAO(oldVAO);
        return vao;
    }
    
    public void draw()
    {
        VertexBuilder oldData = DGL.currentData();
        DGL.bindData(null);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
        GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.numTriangles*3, GL11.GL_UNSIGNED_INT, 0);
        
        DGL.bindData(oldData);
    }
    
    public void delete()
    {
        GL15.glDeleteBuffers(vertexBuffer);
        GL15.glDeleteBuffers(elementBuffer);
        
        vertexBuffer = -1;
        elementBuffer = -1;
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
