package com.samrj.devil.math.numerical;

/**
 * Numerical integrator using the midpoint method.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Midpoint implements Integrator
{
    @Override
    public <T extends NumState<T>> T
        integrate(float t0, float dt, T s0, Derivative<T> ds)
    {
        float hdt = dt*.5f;
        
        T s1 = ds.getSlope(t0, s0).mult(hdt).add(s0);
        T s2 = ds.getSlope(t0 + hdt, s1).mult(dt).add(s0);
        
        return s2;
    }
}