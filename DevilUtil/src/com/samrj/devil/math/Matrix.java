package com.samrj.devil.math;

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