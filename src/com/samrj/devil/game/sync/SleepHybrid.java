package com.samrj.devil.game.sync;

/**
 * Sleep method combining thread sleeping with a busy wait. Should have fairly
 * good accuracy without hogging a CPU core too much. If the sleep time is lower
 * than the measured granularity of thread sleeping, this class defaults over to
 * busy waiting.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SleepHybrid extends SleepMethod
{
    private final MovingLongAvg sleepAvg;
    private long busyError;
    
    /**
     * Creates a new hybrid sleep object.
     * 
     * @param windowSize The size of the moving average of sleep times.
     * @param deviationTolerance Number of standard deviations for thread
     *        sleeping. Higher is stricter.
     */
    public SleepHybrid(int windowSize, float deviationTolerance)
    {
        sleepAvg = new MovingLongAvg(windowSize);
    }
    
    /**
     * Creates a new hybrid sleep object with default values.
     */
    public SleepHybrid()
    {
        this(60, 2.0f);
    }
    
    /**
     * Calibrates this sleep object's by 'warming' it up, populating the running
     * average for sleep times and getting the JVM to inline System.nanoTime().
     * 
     * The calibration time should be at least 5 milliseconds to get a good
     * sample size.
     * 
     * @param time The amount of time to calibrate for, in milliseconds.
     * @throws java.lang.InterruptedException If this thread is interrupted
     *         while this method is trying to run.
     */
    public void calibrate(float time) throws InterruptedException
    {
        long now = System.nanoTime();
        sleep(now, now + (long)(time*1_000_000.0));
    }
    
    @Override
    public long sleep(long now, long end) throws InterruptedException
    {
        //Sleep until it's likely that we'll pass our target if we sleep again.
        while (now + sleepAvg.mean() + sleepAvg.deviation() < end)
        {
            Thread.sleep(1);
            long t1 = System.nanoTime();
            sleepAvg.push(t1 - now);
            now = t1;
        }
        
        //Busy wait until we pass our target.
        while (now + busyError < end) now = System.nanoTime();
        
        return now;
    }
    
    public void printDebug()
    {
        System.out.println("Average sleep time: " + sleepAvg.mean());
        System.out.println("Sleep time standard deviation: " + sleepAvg.deviation());
    }
}
