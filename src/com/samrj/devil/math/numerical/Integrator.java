package com.samrj.devil.math.numerical;

/**
 * Interface for a numerical integrator.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Integrator
{
    /**
     * @param <T> The type of numerical state to use.
     * @param t0 The variable of integration.
     * @param dt The time-step over which to integrate.
     * @param y0 The starting state.
     * @param dydt The derivative of y with respect to t.
     * @return y at t + dt.
     */
    public <T extends NumState<T>> T
        integrate(float t0, float dt, T y0, Derivative<T> dydt);
}
