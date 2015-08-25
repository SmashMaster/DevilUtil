/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.samrj.devil.geo3d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.util.Comparator;

/**
 * Intersection class for results from ellipsoid clip tests.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <TYPE> The type of object this intersection handles.
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Intersection<TYPE>
{
    public static final Comparator<Intersection> comparator = new SectComparator();
    
    public enum Type
    {
        FACE, EDGE, VERTEX;
    }
    
    /**
     * The depth of intersection.
     */
    public final float d;
    
    /**
     * The position on the intersected geometry which is deepest inside of the
     * intersecting volume.
     */
    public final Vec3 p;
    
    /**
     * The normal vector of the intersection.
     */
    public final Vec3 n;
    
    Intersection(float d, Vec3 p, Vec3 n)
    {
        this.d = d;
        this.p = p;
        this.n = n;
    }
    
    /**
     * @return The type of object this intersection handles.
     */
    public abstract Type type();
    
    /**
     * @return The object that is being intersected.
     */
    public abstract TYPE intersected();
    
    private static class SectComparator implements Comparator<Intersection>
    {
        @Override
        public int compare(Intersection i1, Intersection i2)
        {
            if (i1 == i2) return 0;
            if (i1 == null) return 1;
            if (i2 == null) return -1;
            return Util.compare(i1.d, i2.d, 0.0f);
        }
    }
}
