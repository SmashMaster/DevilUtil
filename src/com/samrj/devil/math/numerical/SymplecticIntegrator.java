package com.samrj.devil.math.numerical;

/**
 * Interface for a symplectic numerical integrator.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface SymplecticIntegrator
{
    public <T extends NumState<T>> T
        integrate(float t0, float dt, StatePair<T> s0, PairDerivative<T> ds);
}