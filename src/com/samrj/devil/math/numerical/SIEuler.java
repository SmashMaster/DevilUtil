package com.samrj.devil.math.numerical;

/**
 * Symplectic integrator using the semi-implicit Euler method.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SIEuler implements SymplecticIntegrator
{
    @Override
    public <T extends NumState<T>> T
        integrate(float t0, float dt, StatePair<T> s0, PairDerivative<T> ds)
    {
        return null;
    }
}
