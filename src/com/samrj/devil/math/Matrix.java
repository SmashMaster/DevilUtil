package com.samrj.devil.math;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Matrix<SELF_TYPE extends Matrix>
{
    public SELF_TYPE set(SELF_TYPE z);
    public SELF_TYPE mult(SELF_TYPE z);
    public SELF_TYPE mult(float s);
    public SELF_TYPE div(float s);
    public SELF_TYPE invert();
    public SELF_TYPE transpose();
    public float determinant();
    public SELF_TYPE clone();
}