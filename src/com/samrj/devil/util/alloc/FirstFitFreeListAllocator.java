package com.samrj.devil.util.alloc;

import java.util.Objects;

/**
 *
 * @author angle
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FirstFitFreeListAllocator implements Allocator {
    private final AllocatorRegion first;
    AllocatorRegion last;
    private int capacity;
    private final int alignment;
    private IncreaseCapacityCallback<FirstFitFreeListAllocator> callback;
    
    public FirstFitFreeListAllocator(int capacity, int alignment, IncreaseCapacityCallback<FirstFitFreeListAllocator> callback) {
        this.first = new AllocatorRegion(this, 0, capacity, null, null);
        this.capacity = capacity;
        this.alignment = alignment;
        this.callback = callback;
    }

    /**
     * @return the capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @return the alignment
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * @param last the last to set
     */
    void setLast(AllocatorRegion last) {
        this.last = last;
    }
    
    @Override
    public void increaseCapacity(int increaseNeeded) {
        int newCapacity = callback.increaseCapacity(this, increaseNeeded);
        last.setLength(last.getLength() + newCapacity - getCapacity());
        capacity = newCapacity;
    }
    
    @Override
    public synchronized AllocatorRegion allocateRegion(int size) {
        size = sizeToAlignment(size);
        
        if (size > getCapacity()) increaseCapacity(size);
        
        AllocatorRegion currentRegion = first;
        
        while (true) {
            if (!currentRegion.isAllocated() && currentRegion.getLength() >= size) {
                currentRegion.allocate(size);
                return currentRegion;
            }
            AllocatorRegion nextRegion = currentRegion.getNext();
            if (Objects.isNull(nextRegion))
                increaseCapacity(size);
            else
                currentRegion = nextRegion;
        }
    }
}