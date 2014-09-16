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
        integrate(float t0, float dt, T y0, Derivative<T> dydt)
    {
        float hdt = dt*.5f;
        
        T k1 = dydt.getSlope(t0, y0).mult(hdt).add(y0);
        T k2 = dydt.getSlope(t0 + hdt, k1).mult(dt).add(y0);
        
        return k2;
    }
}
