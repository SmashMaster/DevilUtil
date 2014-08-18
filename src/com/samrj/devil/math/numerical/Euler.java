package com.samrj.devil.math.numerical;

/**
 * Numerical integrator using the Euler method.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Euler implements Integrator
{
    @Override
    public <T extends NumState<T>> T
        integrate(float t0, float dt, T y0, Derivative<T> dydt)
    {
        return dydt.getSlope(t0, y0).mult(dt).add(y0);
    }
}