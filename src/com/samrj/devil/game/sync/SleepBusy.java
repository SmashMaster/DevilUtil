package com.samrj.devil.game.sync;

/**
 * Sleep method which uses a very simple busy-wait. Will completely hog the CPU,
 * and should only be used when the expected sleep time is very low and there is
 * no time to calibrate another sleep method.
 * 
 * Will always sleep slightly too long, rather than sleep too little.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SleepBusy extends SleepMethod
{
    @Override
    public long sleep(long now, long end) throws InterruptedException
    {
        while (now < end) now = System.nanoTime();
        return now;
    }
}
