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
        integrate(float t0, float dt, StatePair<T> s0, PairDerivative<T> ds)
    {
        float hdt = dt*.5f;
        
        T qMid = ds.getForce(t0, s0.q).mult(hdt).add(s0.q);
        T p1 = qMid.clone().mult(dt).add(s0.p);
        T q1 = ds.getForce(t0 + dt, s0.q).mult(hdt).add(qMid);
        return new StatePair<>(p1, q1);
    }
}
