package com.samrj.devil.phys;

import com.samrj.devil.math.Matrix3f;

/**
 * Moment of inertia static factory. These methods return the moments of inertia
 * of various 3D volumes about their centers of mass. Multiply these tensors by
 * the mass of the given body to return the actual moment of inertia.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class MOI
{
    /**
     * @param w the width of the cuboid.
     * @param h the height of the cuboid.
     * @param d the depth of the cuboid.
     * @return the moment of inertia tensor of the given cuboid.
     */
    public static Matrix3f cuboid(float w, float h, float d)
    {
        float x = w*w/12f;
        float y = h*h/12f;
        float z = d*d/12f;
        
        return new Matrix3f(y + z, 0f, 0f,
                            0f, x + z, 0f,
                            0f, 0f, x + y);
    }
    
    /**
     * @param a the radius along the x axis.
     * @param b the radius along the y axis.
     * @param c the radius along the z axis.
     * @return the moment of inertia tensor of the given ellipsoid.
     */
    public static Matrix3f ellipsoid(float a, float b, float c)
    {
        float x = a*a/5f;
        float y = b*b/5f;
        float z = c*c/5f;
        
        return new Matrix3f(y + z, 0f, 0f,
                            0f, x + z, 0f,
                            0f, 0f, x + y);
    }
    
    /**
     * @param r the radius along the x axis.
     * @return the moment of inertia tensor of the given sphere.
     */
    public static Matrix3f sphere(float r)
    {
        float m = r*r*(2f/5f);
        
        return new Matrix3f(m, 0f, 0f,
                            0f, m, 0f,
                            0f, 0f, m);
    }
    
    private MOI() {}
}
