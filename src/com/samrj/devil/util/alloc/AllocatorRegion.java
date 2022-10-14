package com.samrj.devil.util.alloc;

import java.util.Objects;

/**
 *
 * @author angle
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class AllocatorRegion {
    private final FirstFitFreeListAllocator parent;
    private final int offset;
    private int length;
    private AllocatorRegion prev, next;
    private boolean allocated = false;

    AllocatorRegion(FirstFitFreeListAllocator parent, int offset, int length, AllocatorRegion prev, AllocatorRegion next) {
        this.parent = parent;
        this.offset = offset;
        this.length = length;
        this.prev = prev;
        this.next = next;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @return the length
     */
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
    AllocatorRegion getPrev() {
        return prev;
    }

    /**
     * @return the next
     */
    AllocatorRegion getNext() {
        return next;
    }

    /**
     * @return if this region is allocated
     */
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
            AllocatorRegion newRegion = new AllocatorRegion(parent, getOffset() + size, getLength() - size, this, getNext());
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