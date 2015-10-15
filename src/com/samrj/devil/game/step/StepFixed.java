package com.samrj.devil.game.step;

/**
 * A locked time step. Performs one time step per frame, with a constant
 * duration.
 * 
 * Pros:
 *  * Deterministic simulation.
 *  * One step per frame.
 * 
 * Cons:
 *  * Simulation speed tied to frame rate.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class StepFixed extends TimeStepper
{
    private final float dt;
    
    /**
     * Constructs a new fixed time stepper.
     * 
     * @param dt The time step to use.
     */
    public StepFixed(float dt)
    {
        this.dt = dt;
    }
    
    @Override
    public void step(TimeStep stepFunc, float frameTime)
    {
        stepFunc.step(dt);
        time += dt;
    }

    @Override
    public float interpolant()
    {
        return 1.0f;
    }
}
