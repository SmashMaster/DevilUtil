package com.samrj.devil.game.sync;

/**
 * Frame synchronization class, for game loops without v-sync.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Sync
{
    private final long dt;
    private final SleepMethod sleepMethod;
    private boolean initialized;
    private long frameStart; //AKA the end of the previous frame
    
    public Sync(int fps, SleepMethod sleepMethod)
    {
        if (fps <= 0) throw new IllegalArgumentException();
        if (sleepMethod == null) throw new NullPointerException();
        
        dt = Math.round(1_000_000_000.0/fps);
        
        this.sleepMethod = sleepMethod;
    }
    
    /**
     * Call just before the display updates, at the end of every frame.
     * Returns the time after synching.
     * 
     * @return The time after synching.
     * @throws InterruptedException If the calling thread is interrupted while
     *         this method is running.
     */
    public final long sync() throws InterruptedException
    {
        long t = System.nanoTime();
        
        if (initialized)
        {
            long frameEnd = frameStart + dt;
            
            if (t < frameEnd)
            {
                t = sleepMethod.sleep(t, frameStart + dt);
                frameStart = frameEnd;
            }
            else frameStart = t;
        }
        else
        {
            frameStart = t;
            initialized = true;
        }
        
        return t;
    }
    
    public long getFrameTime()
    {
        return dt;
    }
}
