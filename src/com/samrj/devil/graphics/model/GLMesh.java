package com.samrj.devil.graphics.model;

import com.samrj.devil.gl.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Utility OpenGL wrapper for DevilModel meshes.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLMesh
{
    private final ShaderProgram shader;
    private final Mesh mesh;
    private final int posOffset, normalOffset, tangentOffset, uvOffset, colorOffset, groupsOffset, weightsOffset;
    
    private int vertexArray;
    private int vertexBuffer, elementBuffer;
    
    public GLMesh(ShaderProgram shader, Mesh mesh)
    {
        this.shader = shader;
        this.mesh = mesh;
        
        //Set up offsets based on previous offset.
        posOffset = 0;
        normalOffset = posOffset + mesh.numVertices*3*4; //3 components for position X 4 bytes per component
        tangentOffset = normalOffset + mesh.numVertices*3*4;
        uvOffset = tangentOffset + mesh.numVertices*(mesh.hasTangents ? 3 : 0)*4;
        colorOffset = uvOffset + mesh.numVertices*(mesh.hasUVs ? 2 : 0)*4;
        groupsOffset = colorOffset + mesh.numVertices*(mesh.hasVertexColors ? 3 : 0)*4;
        weightsOffset = groupsOffset + mesh.numVertices*mesh.numVertexGroups*4;
        
        //Set up OpenGL stuff.
        vertexArray = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArray);
        
        mesh.rewindBuffers();

        vertexBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh.vertexData, GL15.GL_STATIC_DRAW);

        elementBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.triangleIndexData, GL15.GL_STATIC_DRAW);
    }
    
    private int getEnableLocation(String name)
    {
        int location = shader.getAttributeLocation(name);
        if (location < 0) throw new IllegalArgumentException("No attribute with name " + name + " found.");
        GL20.glEnableVertexAttribArray(location);
        return location;
    }
    
    /**
     * These tell the shader how to find the mesh data. The name argument
     * specifies a shader variable name.
     * 
     * These may throw a LocationNotFoundException if the shader doesn't
     * actually ever use the referenced attribute--the attribute can be entirely
     * optimized out of the shader by the system's GLSL compiler.
     * 
     * Vertex array must still be bound for these to work. So call these right
     * after making the object.
     */
    
    public void setPositionName(String name)
    {
        int location = getEnableLocation(name);
        GL20.glVertexAttribPointer(location, 3, GL11.GL_FLOAT, false, 0, posOffset);
    }
    
    public void setNormalName(String name)
    {
        int location = getEnableLocation(name);
        GL20.glVertexAttribPointer(location, 3, GL11.GL_FLOAT, false, 0, normalOffset);
    }
    
    public void setTangentName(String name)
    {
        int location = getEnableLocation(name);
        GL20.glVertexAttribPointer(location, 3, GL11.GL_FLOAT, false, 0, tangentOffset);
    }
    
    public void setUVName(String name)
    {
        if (!mesh.hasUVs) throw new IllegalStateException();
        int location = getEnableLocation(name);
        GL20.glVertexAttribPointer(location, 2, GL11.GL_FLOAT, false, 0, uvOffset);
    }
    
    public void setColorName(String name)
    {
        if (!mesh.hasVertexColors) throw new IllegalStateException();
        int location = getEnableLocation(name);
        GL20.glVertexAttribPointer(location, 3, GL11.GL_FLOAT, false, 0, colorOffset);
    }
    
    public void setGroupsName(String name)
    {
        if (mesh.numVertexGroups <= 0) throw new IllegalStateException();
        int location = getEnableLocation(name);
        //GL_FLOAT might look like a bug right here, but it's not. Don't touch it.
        GL20.glVertexAttribPointer(location, mesh.numVertexGroups, GL11.GL_FLOAT, false, 0, groupsOffset);
    }
    
    public void setWeightsName(String name)
    {
        if (mesh.numVertexGroups <= 0) throw new IllegalStateException();
        int location = getEnableLocation(name);
        GL20.glVertexAttribPointer(location, mesh.numVertexGroups, GL11.GL_FLOAT, false, 0, weightsOffset);
    }
    
    public void draw()
    {
        GL30.glBindVertexArray(vertexArray);
        GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.numTriangles*3, GL11.GL_UNSIGNED_INT, 0);
    }
    
    public void delete()
    {
        GL30.glDeleteVertexArrays(vertexArray);
        GL15.glDeleteBuffers(vertexBuffer);
        GL15.glDeleteBuffers(elementBuffer);
        
        vertexArray = -1;
    }
}
