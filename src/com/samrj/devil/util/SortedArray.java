package com.samrj.devil.util;

import java.util.Arrays;
import java.util.Comparator;

public class SortedArray<T>
{
    private Object[] array;
    private int size;
    private final Comparator<? super T> comparator;
    
    public SortedArray(int capacity, Comparator<? super T> comparator)
    {
        array = new Object[capacity];
        this.comparator = comparator;
    }
    
    /**
     * Finds the given entry in the array.
     * 
     * @param entry The entry to search for.
     * @return index of the search entry, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
    public int index(T entry)
    {
        return Arrays.binarySearch((T[])array, 0, size, entry, comparator);
    }
    
    /**
     * Returns the entry at the given index.
     * 
     * @param index The index at which to retrieve an entry.
     * @return The entry at th given index.
     */
    public T get(int index)
    {
        if (index >= size) throw new ArrayIndexOutOfBoundsException();
        
        return (T)array[index];
    }
    
    /**
     * Inserts the given entry into the array.
     * 
     * @param entry The entry to insert into the array.
     * @return The index the entry was inserted at.
     */
    public int insert(T entry)
    {
        if (entry == null) throw new NullPointerException();
        
        int index = index(entry);
        if (index < 0) index = -index - 1;
        
        if (size == array.length)
        {
            int newCapacity = array.length << 1;
            if (newCapacity < array.length) throw new ArrayIndexOutOfBoundsException();
            Object[] newArray = new Object[newCapacity];
            
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
        return index;
    }
    
    /**
     * Removes the entry from the given index.
     * 
     * @param index The index at which to remove an entry.
     */
    public void remove(int index)
    {
        if (index >= size) throw new ArrayIndexOutOfBoundsException();
        System.arraycopy(array, index + 1, array, index, size - index - 1);
        array[--size] = null;
    }
    
    /**
     * Removes the given entry from the array.
     * 
     * @param entry The entry to remove from the array.
     * @return index of the removed entry, if it was contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.
     */
    public int remove(T entry)
    {
        if (entry == null) throw new NullPointerException();
        
        int index = index(entry);
        if (index >= 0) remove(index);
        return index;
    }
    
    /**
     * Resorts this array, in case it has somehow gotten out of order.
     */
    public void resort()
    {
        Arrays.sort((T[])array, 0, size, comparator);
    }
    
    /**
     * @return The number of entries in this array.
     */
    public int size()
    {
        return size;
    }
    
    /**
     * @return The current capacity of this array.
     */
    public int capacity()
    {
        return array.length;
    }
}
