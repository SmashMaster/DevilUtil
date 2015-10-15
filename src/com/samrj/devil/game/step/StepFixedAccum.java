package com.samrj.devil.game.step;

/**
 * A dynamic-count fixed time step. All time steps have the same length, but
 * will perform multiple steps to catch the simulation up.
 * 
 * Pros:
 *  * Deterministic simulation.
 *  * Simulation speed independent from frame rate.
 * 
 * Cons:
 *  * Vulnerable to "spiral of death" if frame time exceeds ability to catch up.
 *  * Suffers from temporal aliasing unless interpolation is used.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class StepFixedAccum extends TimeStepper
{
    private final float dt;
    private float accumulator;
    
    /**
     * Constructs a new semi-fixed time stepper.
     * 
     * @param dt The fixed time step to use for simulation.
     */
    public StepFixedAccum(float dt)
    {
        if (dt <= 0.0f) throw new IllegalArgumentException();
        this.dt = dt;
    }
    
    @Override
    public void step(TimeStep stepFunc, float frameTime)
    {
        accumulator += frameTime;
        
        while (accumulator >= dt)
        {
            stepFunc.step(dt);
            accumulator -= dt;
            time += dt;
        }
    }

    @Override
    public float interpolant()
    {
        return accumulator/dt;
    }
}
