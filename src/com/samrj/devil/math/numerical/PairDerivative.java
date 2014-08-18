package com.samrj.devil.math.numerical;

/**
 * Interface for a pair derivative function, for use by symplectic integrators.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <STATE_TYPE> The type of numerical state this derivative handles.
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface PairDerivative<STATE_TYPE extends NumState<STATE_TYPE>>
{
    public STATE_TYPE getForce(float t, STATE_TYPE q);
    public STATE_TYPE getMomentum(float t, STATE_TYPE p);
}