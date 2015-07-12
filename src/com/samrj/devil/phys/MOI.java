package com.samrj.devil.phys;

import com.samrj.devil.math.Mat3;

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
    public static MOI cuboid(float w, float h, float d)
    {
        float cx = w*w/12f;
        float cy = h*h/12f;
        float cz = d*d/12f;
        
        float x = cy + cz;
        float y = cx + cz;
        float z = cx + cy;
        
        return new MOI(new Mat3(x,    0f,   0f,
                                0f,   y,    0f,
                                0f,   0f,   z),
                       new Mat3(1f/x, 0f,   0f,
                                0f,   1f/y, 0f,
                                0f,   0f,   1f/z));
    }
    
    /**
     * @param a the radius along the x axis.
     * @param b the radius along the y axis.
     * @param c the radius along the z axis.
     * @return the moment of inertia tensor of the given ellipsoid.
     */
    public static MOI ellipsoid(float a, float b, float c)
    {
        float cx = a*a/5f;
        float cy = b*b/5f;
        float cz = c*c/5f;
        
        float x = cy + cz;
        float y = cx + cz;
        float z = cx + cy;
        
        return new MOI(new Mat3(x,    0f,   0f,
                                0f,   y,    0f,
                                0f,   0f,   z),
                       new Mat3(1f/x, 0f,   0f,
                                0f,   1f/y, 0f,
                                0f,   0f,   1f/z));
    }
    
    /**
     * @param r the radius along the x axis.
     * @return the moment of inertia tensor of the given sphere.
     */
    public static MOI sphere(float r)
    {
        float m = r*r*(2f/5f);
        float i = 1f/m;
        
        return new MOI(new Mat3(m,  0f, 0f,
                                0f, m,  0f,
                                0f, 0f, m),
                       new Mat3(i,  0f, 0f,
                                0f, i,  0f,
                                0f, 0f, i));
    }
    
    public final Mat3 mat, inv;
    
    private MOI(Mat3 mat, Mat3 inv)
    {
        this.mat = mat;
        this.inv = inv;
    }
    
    public MOI()
    {
        this(Mat3.identity(), Mat3.identity());
    }
    
    public MOI set(MOI moi)
    {
        mat.set(moi.mat);
        inv.set(moi.inv);
        return this;
    }
}
