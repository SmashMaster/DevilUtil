package com.samrj.devil.util.alloc;

/**
 *
 * @author angle
 */
public interface Allocation
{
    public int getOffset();
    public int getLength();
    public boolean isAllocated();
    public void deallocate();
}