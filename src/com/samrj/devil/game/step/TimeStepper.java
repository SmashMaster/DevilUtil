package com.samrj.devil.game.step;

/**
 * Abstract class for time stepping methods, which decide how many steps to take
 * and what time step to use.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class TimeStepper
{
    protected double time;
    
    /**
     * Steps with the given method by the given duration.
     * 
     * @param stepFunc What to do during each step.
     * @param dt The time of the previous frame, in seconds.
     */
    public abstract void step(float dt, TimeStep stepFunc);
    
    /**
     * Returns the time, in seconds, since the simulation began. This not the
     * current system time, but rather the in-game time.
     * 
     * @return The time, in seconds, since the simulation began.
     */
    public final double time()
    {
        return time;
    }
    
    /**
     * Returns the frame display interpolant. A zero means we should display the
     * state of the previous frame, a one means we should display the current
     * state. Anything in between means we should display a mixture of the two
     * states.
     * 
     * @return the frame display interpolant.
     */
    public abstract float interpolant();
}
