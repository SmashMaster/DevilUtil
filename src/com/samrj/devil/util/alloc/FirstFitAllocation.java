package com.samrj.devil.util.alloc;

import java.util.Objects;

/**
 *
 * @author angle
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FirstFitAllocation implements Allocation
{
    private final FirstFitAllocator parent;
    private final int offset;
    private int length;
    private FirstFitAllocation prev, next;
    private boolean allocated = false;

    FirstFitAllocation(FirstFitAllocator parent, int offset, int length, FirstFitAllocation prev, FirstFitAllocation next) {
        this.parent = parent;
        this.offset = offset;
        this.length = length;
        this.prev = prev;
        this.next = next;
    }

    /**
     * @return the offset
     */
    @Override
    public int getOffset() {
        return offset;
    }

    /**
     * @return the length
     */
    @Override
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the prev
     */
    FirstFitAllocation getPrev() {
        return prev;
    }

    /**
     * @return the next
     */
    FirstFitAllocation getNext() {
        return next;
    }

    /**
     * @return if this region is allocated
     */
    @Override
    public boolean isAllocated() {
        return allocated;
    }

    @Override
    public String toString() {
        return "AllocatorRegion: (offset:" + getOffset() + ", length:" + getLength() + ")";
    }
    
    void allocate(int size) {
        if (isAllocated())
            throw new IllegalStateException("Cannot allocate already allocated region: " + this);
        
        if (size == getLength()) {
            allocated = true;
            return;
        }
        
        if (size < getLength()) {
            FirstFitAllocation newRegion = new FirstFitAllocation(parent, getOffset() + size, getLength() - size, this, getNext());
            if (Objects.nonNull(getNext()))
                next.prev = newRegion;
            else 
                parent.setLast(newRegion);
            next = newRegion;
            setLength(size);
            allocated = true;
            return;
        }
        
        throw new IllegalArgumentException("Cannot allocate " + size + " length in: " + this);
    }
    
    private void absorbNext() {
        setLength(length + getNext().getLength());
        next = getNext().getNext();
        next.prev = this;
        if (getNext() != null && !next.isAllocated())
            absorbNext();
    }
    
    @Override
    public void deallocate() {
        synchronized (parent) {
            if (!isAllocated())
                throw new IllegalStateException("Cannot deallocate unallocated region: " + this);
            
            if (getPrev() != null && !prev.isAllocated())
                getPrev().absorbNext();
            else {
                if (getNext() != null && !next.isAllocated())
                    absorbNext();
                allocated = false;
            }
        }
    }
}