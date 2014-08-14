package com.samrj.devil.math.numerical;

/**
 * Numerical state; stores dependent variables for numerical integration.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <SELF_TYPE> This NumState's own type.
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface NumState<SELF_TYPE extends NumState<SELF_TYPE>>
{
    public SELF_TYPE add(SELF_TYPE ns);
    public SELF_TYPE sub(SELF_TYPE ns);
    public SELF_TYPE mult(float f);
    public SELF_TYPE div(float f);
    public SELF_TYPE clone();
}