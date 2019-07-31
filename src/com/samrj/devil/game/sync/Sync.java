package com.samrj.devil.game.sync;

import java.util.Objects;

/**
 * Frame synchronization class, for game loops without v-sync.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Sync
{
    private long dt;
    private SleepMethod sleepMethod;
    private boolean initialized;
    private long frameStart; //AKA the end of the previous frame
    
    public Sync(int fps, SleepMethod sleepMethod)
    {
        setFPS(fps);
        setSleeper(sleepMethod);
    }
    
    public void setFPS(int fps)
    {
        dt = Math.round(1_000_000_000.0/fps);
        initialized = false;
    }
    
    public void setSleeper(SleepMethod sleeper)
    {
        sleepMethod = Objects.requireNonNull(sleeper);
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
        if (dt <= 0) return t;
        
        if (initialized)
        {
            long frameEnd = frameStart + dt;
            
            if (t < frameEnd)
            {
                t = sleepMethod.sleep(t, frameEnd);
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
