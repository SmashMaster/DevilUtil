package com.samrj.devil.gl;


/**
 * Interface for instanced rendering data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
interface InstanceData extends VAOBindable
{
    /**
     * @return The OpenGL vertex buffer object for this instance data.
     */
    public int vbo();
    
    /**
     * @return An iterable of every attribute in this instance data.
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
     * Returns the number of instances in this instance data.
     */
    public int numInstances();
    
    public interface Attribute
    {
        public String getName();
        public AttributeType getType();
        public int getStride();
        public int getOffset();
        public int getDivisor();
    }
}
