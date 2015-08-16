package com.samrj.devil.util;

import java.util.Arrays;

/**
 * A set of primitive integers.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IntSet
{
    private static final int DEFAULT_CAPACITY = 16;
    
    private int[] array;
    private int size;
    
    /**
     * Creates a new integer set with the given initial capacity.
     * 
     * @param capacity The initial capacity of the set.
     */
    public IntSet(int capacity)
    {
        array = new int[capacity];
    }
    
    /**
     * Creates a new integer set with the default capacity, 16.
     */
    public IntSet()
    {
        this(DEFAULT_CAPACITY);
    }
    
    /**
     * Returns the index in this set's backing array at which the given entry
     * lies, or a negative number if this set does not contain the given entry.
     * 
     * @param entry The entry to search for.
     */
    public int index(int entry)
    {
        return Arrays.binarySearch(array, 0, size, entry);
    }
    
    /**
     * Returns the entry at the given index.
     * 
     * @param index The index to return the entry at.
     * @return The entry at the given index.
     */
    public int get(int index)
    {
        if (index >= size) throw new ArrayIndexOutOfBoundsException();
        return array[index];
    }
    
    /**
     * Returns whether the given integer is in this set.
     * 
     * @param entry The entry to check for.
     * @return Whether this set contains the given entry.
     */
    public boolean contains(int entry)
    {
        return index(entry) >= 0;
    }
    
    /**
     * Adds the given entry to this set.
     * 
     * @param entry The entry to add to the set.
     * @return True if the entry was not already contained, false otherwise.
     */
    public boolean add(int entry)
    {
        int index = index(entry);
        if (index >= 0) return false;
        index = -index - 1;
        
        if (size == array.length)
        {
            int newCapacity = array.length << 1;
            if (newCapacity < array.length) throw new ArrayIndexOutOfBoundsException();
            int[] newArray = new int[newCapacity];
            
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = entry;
            System.arraycopy(array, index, newArray, index + 1, size - index);
            array = newArray;
        }
        else
        {
            System.arraycopy(array, index, array, index + 1, size - index);
            array[index] = entry;
        }
        
        size++;
        return true;
    }
    
    /**
     * Removes the given entry from the set.
     * 
     * @param entry The entry to remove from the set.
     * @return Whether or not the entry was contained in the set.
     */
    public boolean remove(int entry)
    {
        int index = index(entry);
        if (index >= 0)
        {
            System.arraycopy(array, index + 1, array, index, size - index - 1);
            size--;
            return true;
        }
        return false;
    }
    
    /**
     * Clears this set.
     */
    public void clear()
    {
        size = 0;
    }
    
    /**
     * @return The number of entries in this set.
     */
    public int size()
    {
        return size;
    }
    
    /**
     * @return The current capacity of this set.
     */
    public int capacity()
    {
        return array.length;
    }
    
    /**
     * @return A sorted array of each integer in this set.
     */
    public int[] toArray()
    {
        return Arrays.copyOf(array, size);
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append("{");
        if (size > 0)
        {
            int end = size-1;
            for (int i=0; i<end; i++)
            {
                builder.append(array[i]);
                builder.append(", ");
            }
            builder.append(array[end]);
        }
        builder.append("}");
        return builder.toString();
    }
}
