package com.samrj.devil.gl;

import com.samrj.devil.util.IOUtil;

import java.util.List;

/**
 * Interface for all vertex data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface VertexData extends VAOBindable
{
    /**
     * @return The OpenGL vertex buffer object for this vertex data.
     */
    int vbo();
    
    /**
     * @return The OpenGL index buffer object for this vertex data, or 0 if
     *         indexing is not enabled for this vertex data.
     */
    int ibo();

    /**
     * @return A list of every attribute in this vertex data.
     */
    List<Attribute> attributes();

    /**
     *@return A list of every attribute name in this vertex data.
     */
    default List<String> getAttributeNames()
    {
        return IOUtil.mapList(attributes(), Attribute::getName);
    };

    /**
     * Returns the attribute with the give name, or null if none exists.
     * 
     * @param name The name of the attribute to find.
     * @return The attribute with the give name, or null if none exists.
     */
    Attribute getAttribute(String name);
    
    /**
     * @return The number of currently uploaded vertices.
     */
    int numVertices();
    
    /**
     * @return The number of currently uploaded indices, or -1 if indexing is
     *         not enabled for this vertex data.
     */
    int numIndices();

    default boolean isIndexed()
    {
        return numIndices() >= 0;
    }
    
    interface Attribute
    {
        String getName();
        AttributeType getType();
        int getStride();
        int getOffset();
    }
}
