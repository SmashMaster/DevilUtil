package com.samrj.devil.game.step;

/**
 * A dynamic time step. Performs one time step per frame, with the duration of
 * the previous frame.
 * 
 * Pros:
 *  * One step per frame.
 *  * Simulation speed independent from frame rate.
 * 
 * Cons:
 *  * Non-deterministic simulation.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class StepDynamic extends TimeStepper
{
    @Override
    public void step(TimeStep stepFunc, float dt)
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
