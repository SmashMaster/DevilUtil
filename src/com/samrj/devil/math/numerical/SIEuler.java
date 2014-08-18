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
    public <T extends NumState<T>> StatePair<T>
        integrate(float t0, float dt, StatePair<T> s0, PairDerivative<T> ds)
    {
        T q1 = ds.getForce(t0, s0.p).mult(dt).add(s0.q);
        T p1 = ds.getMomentum(t0, q1).mult(dt).add(s0.p);
        return new StatePair<>(p1, q1);
    }
}
