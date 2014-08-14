package com.samrj.devil.math.numerical;

/**
 * Numerical state; stores dependent variables for numerical integration.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <SELF_TYPE> This NumState's own type.
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface NumState<SELF_TYPE extends NumState>
{
    public NumState<SELF_TYPE> add(NumState<SELF_TYPE> ns);
    public NumState<SELF_TYPE> sub(NumState<SELF_TYPE> ns);
    public NumState<SELF_TYPE> mult(float f);
    public NumState<SELF_TYPE> div(float f);
    public NumState<SELF_TYPE> clone();
}