package com.samrj.devil.util.alloc;

/**
 *
 * @author angle
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Allocator {
    @FunctionalInterface
    public interface IncreaseCapacityCallback<T extends Allocator> {
        int increaseCapacity(T allocator, int increaseNeeded);
    }
    
    public int getCapacity();
    public int getAlignment();
    public default int sizeToAlignment(int size) {
        return size + getAlignment() - 1 - (size + getAlignment() - 1) % getAlignment();
    }
    
    public void increaseCapacity(int increaseNeeded);
    
    public Allocation allocateRegion(int size);
}