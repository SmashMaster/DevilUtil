package com.samrj.devil.math.numerical;

/**
 * Symplectic integrator using the velocity Verlet algorithm.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Verlet implements SymplecticIntegrator
{
    @Override
    public <T extends NumState<T>> StatePair<T>
        integrate(float t0, float dt, T p0, T v0, Derivative<T> dvdt)
    {
        float hdt = dt*.5f;
        
        T qMid = dvdt.getSlope(t0, p0).mult(hdt).add(v0);
        T p1 = qMid.copy().mult(dt).add(p0);
        T v1 = dvdt.getSlope(t0 + dt, p1).mult(hdt).add(qMid);
        return new StatePair<>(p1, v1);
    }
}
