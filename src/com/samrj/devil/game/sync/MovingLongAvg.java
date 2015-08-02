package com.samrj.devil.game.sync;

/**
 * Class for keeping a moving average of a stream of data. Calculates the mean
 * and standard deviation of the last N values given to it, where N is the size
 * of the window.
 * 
 * The data type for this average is the 64 bit integer, so we can use this for
 * System.nanoTime().
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class MovingLongAvg
{
    private final long[] window;
    private boolean full;
    
    private int index;
    
    private long sum, sumOfSquares;
    private long mean;
    private long deviation;
    
    /**
     * Creates a new moving average with the given window capacity.
     * 
     * @param capacity The maximum size of the window.
     */
    public MovingLongAvg(int capacity)
    {
        window = new long[capacity];
        full = false;
    }
    
    /**
     * @return The number of samples in the current window.
     */
    public int size()
    {
        return full ? window.length : index;
    }
        
    
    /**
     * Pushes a new value onto the current window. If the window is full, the
     * new value will replace the oldest value.
     * 
     * @param value The value to push onto the window.
     */
    public void push(long value)
    {
        if (++index == window.length)
        {
            index = 0;
            full = true;
        }

        if (full)
        {
            long sample = window[index];
            sum -= sample;
            sumOfSquares -= sample*sample;
        }
        
        window[index] = value;
        sum += value;
        sumOfSquares += value*value;
        
        double size = (double)size();
        double dmean = sum/size;
        mean = Math.round(dmean);
        
        double variance = sumOfSquares/size - dmean*dmean;
        deviation = Math.round(Math.sqrt(variance));
    }
    
    /**
     * @return The mean of the data in the current window.
     */
    public long mean()
    {
        return mean;
    }
    
    /**
     * @return The sample standard deviation of the data in the current window.
     */
    public long deviation()
    {
        return deviation;
    }
}
