package com.samrj.devil.util;

import java.util.Arrays;
import java.util.Comparator;

public class SortedArray<T>
{
    private final T[] array;
    private final Comparator<? super T> comparator;
    
    public SortedArray(int size, Comparator<? super T> comparator)
    {
        array = (T[])new Object[size];
        this.comparator = comparator;
    }
    
    public void insert(T entry)
    {
        int index = Arrays.binarySearch(array, entry, comparator);
        if (index < 0) index = -index - 1;
        
        if (index >= array.length) return;
        
        System.arraycopy(array, index, array, index + 1, array.length - index - 1);
        array[index] = entry;
    }
}
