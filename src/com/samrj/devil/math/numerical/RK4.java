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
    public <T extends NumState<T>> NumState<T>
        integrate(float t0, float dt, NumState<T> s0, Derivative<T> ds)
    {
        float halfdt = dt*.5f;
        
        NumState<T> k1 = ds.getSlope(t0, s0).mult(dt);
        NumState<T> k2 = ds.getSlope(halfdt, k1.clone().mult(.5f).add(s0)).mult(dt);
        NumState<T> k3 = ds.getSlope(halfdt, k2.clone().mult(.5f).add(s0)).mult(dt);
        NumState<T> k4 = ds.getSlope(t0 + dt, k3.clone().add(s0)).mult(dt);
        
        return k1.add(k2.add(k3).mult(2f)).add(k4).div(6f).add(s0);
    }
}