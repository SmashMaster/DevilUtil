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
    public <T extends NumState<T>> StatePair<T>
        integrate(float t0, float dt, T p0, T v0, Derivative<T> dvdt);
}