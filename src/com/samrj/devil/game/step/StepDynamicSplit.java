package com.samrj.devil.game.step;

/**
 * A dynamic split time stepper. Time steps are guaranteed lower and upper
 * bounds, so stable physics can be maintained.
 * 
 * Pros:
 *  * Simulation speed independent from frame rate.
 *  * Time steps are bounded.
 * 
 * Cons:
 *  * Vulnerable to "spiral of death" if frame time exceeds ability to catch up.
 *  * Non-deterministic simulation.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class StepDynamicSplit extends TimeStepper
{
    private final float mindt, maxdt;
    
    /**
     * Constructs a new dynamic split time stepper.
     * 
     * @param mindt The minimum time step. Ignored if the total frame time is
     *        too short.
     * @param maxdt The maximum time step.
     */
    public StepDynamicSplit(float mindt, float maxdt)
    {
        if (mindt > maxdt || mindt <= 0.0f || maxdt <= 0.0f)
            throw new IllegalArgumentException();
        this.mindt = mindt;
        this.maxdt = maxdt;
    }

    @Override
    public void step(TimeStep stepFunc, float dt)
    {
        if (dt <= mindt) stepFunc.step(dt);
        else
        {
            float remainder = dt % maxdt;
            int numSegments = Math.round((dt - remainder)/maxdt);

            if (remainder > mindt)
            {
                for (int s=0; s<numSegments; s++) stepFunc.step(maxdt);
                stepFunc.step(remainder);
            }
            else
            {
                float segdt = dt/numSegments;
                for (int s=0; s<numSegments; s++) stepFunc.step(segdt);
            }
        }
    }

    @Override
    public float interpolant()
    {
        return 1.0f;
    }
}
