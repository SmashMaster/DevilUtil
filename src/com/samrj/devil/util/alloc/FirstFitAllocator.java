package com.samrj.devil.util.alloc;

import java.util.Objects;

/**
 *
 * @author angle
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FirstFitAllocator implements Allocator {
    private final FirstFitAllocation first;
    FirstFitAllocation last;
    private int capacity;
    private final int alignment;
    private final IncreaseCapacityCallback<FirstFitAllocator> callback;
    
    public FirstFitAllocator(int capacity, int alignment, IncreaseCapacityCallback<FirstFitAllocator> callback) {
        this.first = new FirstFitAllocation(this, 0, capacity, null, null);
        this.capacity = capacity;
        this.alignment = alignment;
        this.callback = callback;
        last = first;
    }

    /**
     * @return the capacity
     */
    @Override
    public int getCapacity() {
        return capacity;
    }

    /**
     * @return the alignment
     */
    @Override
    public int getAlignment() {
        return alignment;
    }

    /**
     * @param last the last to set
     */
    void setLast(FirstFitAllocation last) {
        this.last = last;
    }
    
    @Override
    public void increaseCapacity(int increaseNeeded) {
        int newCapacity = callback.increaseCapacity(this, increaseNeeded);
        if (last.isAllocated()) {
            FirstFitAllocation newRegion = new FirstFitAllocation(this, capacity, increaseNeeded, last, null);
            last.setNext(newRegion);
            setLast(newRegion);
        } else
            last.setLength(last.getLength() + newCapacity - getCapacity());
        capacity = newCapacity;
    }
    
    @Override
    public synchronized Allocation allocateRegion(int size) {
        size = sizeToAlignment(size);
        
        if (size > getCapacity()) increaseCapacity(size);
        
        FirstFitAllocation currentRegion = first;
        
        while (true) {
            if (!currentRegion.isAllocated() && currentRegion.getLength() >= size) {
                currentRegion.allocate(size);
                return currentRegion;
            }
            FirstFitAllocation nextRegion = currentRegion.getNext();
            if (Objects.isNull(nextRegion))
                increaseCapacity(size);
            else
                currentRegion = nextRegion;
        }
    }
}