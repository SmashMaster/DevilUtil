package com.samrj.devil.math.numerical;

/**
 * Canonical coordinates, used and returned by a symplectic integrator.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <STATE_TYPE> The type of NumState this pair contains.
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class StatePair<STATE_TYPE extends NumState<STATE_TYPE>>
{
    public final STATE_TYPE p, v;
    
    public StatePair(STATE_TYPE p, STATE_TYPE v)
    {
        this.p = p; this.v = v;
    }
}
