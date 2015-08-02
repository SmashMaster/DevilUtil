package com.samrj.devil.game.sync;

/**
 * Interface for any method used to wait until a given time, with the best
 * possible precision.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class SleepMethod
{
    /**
     * Sleeps until the given time, in nanoseconds, as measured by the
     * System.nanoTime() method.
     * 
     * @param now The current time. Should be measured right before calling.
     * @param end The time to sleep until.
     * @return The actual time when this method ended.
     * @throws java.lang.InterruptedException If this thread is interrupted
     *         while this method is trying to sleep.
     */
    public abstract long sleep(long now, long end) throws InterruptedException;
    
    /**
     * Sleeps until the given time, in nanoseconds, as measured by the
     * System.nanoTime() method.
     * 
     * @param end The time to sleep until.
     * @return The actual time when this method ended.
     * @throws java.lang.InterruptedException If this thread is interrupted
     *         while this method is trying to sleep.
     */
    public final long sleep(long end) throws InterruptedException
    {
        return sleep(System.nanoTime(), end);
    }
}
