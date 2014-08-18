package com.samrj.devil.math.numerical;

/**
 * Classical Runge-Kutta integrator.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class RK4 implements Integrator
{
    @Override
    public <T extends NumState<T>> T
        integrate(float t0, float dt, T s0, Derivative<T> ds)
    {
        float hdt = dt*.5f;
        
        T k1 = ds.getSlope(t0, s0);
        T k2 = ds.getSlope(t0 + hdt, k1.clone().mult(hdt).add(s0));
        T k3 = ds.getSlope(t0 + hdt, k2.clone().mult(hdt).add(s0));
        T k4 = ds.getSlope(t0 + dt, k3.clone().mult(dt).add(s0));
        
        return k1.add(k2.add(k3).mult(2f)).add(k4).mult(dt/6f).add(s0);
    }
}