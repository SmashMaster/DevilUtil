package com.samrj.devil.math.numerical;

/**
 * Interface for a derivative function, for use by integrators.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <STATE_TYPE> The type of numerical state this derivative handles.
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Derivative<STATE_TYPE extends NumState<STATE_TYPE>>
{
    /**
     * @param t The independent variable.
     * @param state The state at which to compute the slope.
     * @return The rate of change of the state, with respect to t.
     */
    public STATE_TYPE getSlope(float t, STATE_TYPE state);
}