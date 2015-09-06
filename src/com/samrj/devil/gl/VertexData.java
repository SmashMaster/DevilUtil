package com.samrj.devil.gl;

/**
 * Interface for all vertex data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface VertexData
{
    /**
     * @return The OpenGL vertex buffer object for this vertex data.
     */
    public int vbo();
    
    /**
     * @return The OpenGL index buffer object for this vertex data, or 0 if
     *         indexing is not enabled for this vertex data.
     */
    public int ibo();
    
    /**
     * @return An iterable of every attribute in this vertex data.
     */
    public Iterable<Attribute> attributes();
    
    /**
     * Returns the attribute with the give name, or null if none exists.
     * 
     * @param name The name of the attribute to find.
     * @return The attribute with the give name, or null if none exists.
     */
    public Attribute getAttribute(String name);
    
    /**
     * @return The size of each vertex, in bytes.
     */
    public int vertexSize();
    
    /**
     * @return The number of currently uploaded vertices.
     */
    public int numVertices();
    
    /**
     * @return The number of currently uploaded indices, or -1 if indexing is
     *         not enabled for this vertex data.
     */
    public int numIndices();
    
    public class Attribute
    {
        public final String name;
        public final AttributeType type;
        public final int offset;
        
        protected Attribute(String name, AttributeType type, int offset)
        {
            this.name = name;
            this.type = type;
            this.offset = offset;
        }
    }
}
