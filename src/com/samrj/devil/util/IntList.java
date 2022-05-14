package com.samrj.devil.util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Automatically resizing integer array.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class IntList implements Iterable<Integer>
{
    private static final int DEFAULT_CAPACITY = 8;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    
    private static int hugeCapacity(int minCapacity)
    {
        if (minCapacity < 0) throw new OutOfMemoryError(); //Overflow.
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
    
    private int[] array;
    private int size;
    
    /**
     * Creates a new IntList with the given initial capacity.
     * 
     * @param initialCapacity The initial size of the backing array.
     */
    public IntList(int initialCapacity)
    {
        array = new int[initialCapacity];
    }
    
    /**
     * Creates a new IntList with the default capacity, ten.
     */
    public IntList()
    {
        this(DEFAULT_CAPACITY);
    }
    
    /**
     * Returns the value at the given index.
     * 
     * @param index The index to poll.
     * @return The value at the index.
     */
    public int get(int index)
    {
        if (index >= size) throw new ArrayIndexOutOfBoundsException();
        return array[index];
    }
    
    /**
     * Sets the value at the given index to the given value.
     * 
     * @param index The index of the entry to update.
     * @param value The value to set the entry to.
     */
    public void set(int index, int value)
    {
        if (index >= size) throw new ArrayIndexOutOfBoundsException();
        array[index] = value;
    }
    
    private void grow(int minCapacity)
    {
        if (array.length - minCapacity > 0) return;
        
        int oldCapacity = array.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity);
        
        array = Arrays.copyOf(array, newCapacity);
    }
    
    /**
     * Appends the given value to the end of the list.
     * 
     * @param value The value to append.
     */
    public void add(int value)
    {
        grow(size + 1);
        array[size++] = value;
    }
    
    /**
     * Appends each entry in the given array to the end of this list, in order.
     * 
     * @param values The array of values to append.
     */
    public void add(int... values)
    {
        int newSize = size + values.length;
        grow(newSize);
        System.arraycopy(values, 0, array, size, values.length);
        size = newSize;
    }
    
    /**
     * Adds each entry in the given list to the end of this list, in order.
     * 
     * @param list The list of values to append.
     */
    public void add(IntList list)
    {
        add(list.array);
    }
    
    /**
     * Returns a new array representing the contents of this int list.
     */
    public int[] toArray()
    {
        int[] out = new int[size];
        System.arraycopy(array, 0, out, 0, size);
        return out;
    }
    
    /**
     * Sets the size of this IntList to zero, making it effectively empty.
     */
    public void clear()
    {
        size = 0;
    }
    
    /**
     * Returns the number of entries in this list.
     * 
     * @return The number of entries in this list.
     */
    public int size()
    {
        return size;
    }
    
    /**
     * Returns the current capacity of this list.
     * 
     * @return The capacity of this list.
     */
    public int capacity()
    {
        return array.length;
    }
    
    @Override
    public Iterator<Integer> iterator()
    {
        return new IntListIterator();
    }
    
    private final class IntListIterator implements Iterator<Integer>
    {
        private int i = 0;
        
        @Override
        public boolean hasNext()
        {
            return i < size;
        }

        @Override
        public Integer next()
        {
            return array[i++];
        }
    }
    
    @Override
    public String toString()
    {
        return Arrays.toString(toArray());
    }
}
