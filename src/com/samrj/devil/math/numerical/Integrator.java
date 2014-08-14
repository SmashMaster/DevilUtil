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
     * @param s0 The starting state.
     * @param ds The derivative of the state with respect to t.
     * @return The state at t0 + dt.
     */
    public <T extends NumState<T>> NumState<T>
        integrate(float t0, float dt, NumState<T> s0, Derivative<T> ds);
}