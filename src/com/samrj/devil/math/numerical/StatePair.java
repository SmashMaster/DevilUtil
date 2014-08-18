package com.samrj.devil.math.numerical;

/**
 * Canonical coordinates, for use by a symplectic integrator.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <STATE_TYPE> The type of NumState this pair contains.
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class StatePair<STATE_TYPE extends NumState<STATE_TYPE>>
{
    /**
     * p is the position; q is the momentum.
     */
    public final STATE_TYPE p, q;
    
    public StatePair(STATE_TYPE p, STATE_TYPE q)
    {
        this.p = p; this.q = q;
    }
}