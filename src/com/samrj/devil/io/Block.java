package com.samrj.devil.io;

/**
 * Class representing a block of memory within a ByteBuffer.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Block
{
    public static enum Type
    {
        FREE(true), PARENT(false), ALLOCATED(false), WASTED(true);
        
        public final boolean mergable;
        
        private Type(boolean mergable)
        {
            this.mergable = mergable;
        }
    }
    
    public final Block parent;
    public final int offset, size;
    Type type;
    Block left, right;
    
    Block(Block parent, int offset, int size, Type type)
    {
        this.parent = parent;
        this.offset = offset;
        this.size = size;
        this.type = type;
    }
    
    Block(Block parent, int offset, int size)
    {
        this(parent, offset, size, Type.FREE);
    }
    
    Block(int size)
    {
        this(null, 0, size, Type.FREE);
    }
    
    public Type type()
    {
        return type;
    }
    
    public Block left()
    {
        return left;
    }
    
    public Block right()
    {
        return right;
    }
}
