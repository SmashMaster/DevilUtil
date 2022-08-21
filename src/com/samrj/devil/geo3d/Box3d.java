package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3d;

/**
 * Double precision axis-aligned bounding box class.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Box3d
{
    public static boolean touching(Box3d a, Box3d b)
    {
        return a.max.x >= b.min.x && b.max.x >= a.min.x &&
               a.max.y >= b.min.y && b.max.y >= a.min.y &&
               a.max.z >= b.min.z && b.max.z >= a.min.z;
    }
    
    public static boolean encloses(Box3d a, Box3d b)
    {
        return a.max.x >= b.max.x && a.min.x <= b.min.x &&
               a.max.y >= b.max.y && a.min.y <= b.min.y &&
               a.max.z >= b.max.z && a.min.z <= b.min.z;
    }
    
    public static double raytrace(Box3d box, Vec3d p0, Vec3d dp, boolean terminated)
    {
        double tx0 = (box.min.x - p0.x)/dp.x;
        double tx1 = (box.max.x - p0.x)/dp.x;
        double ty0 = (box.min.y - p0.y)/dp.y;
        double ty1 = (box.max.y - p0.y)/dp.y;
        double tz0 = (box.min.z - p0.z)/dp.z;
        double tz1 = (box.max.z - p0.z)/dp.z;
        
        if (Double.isNaN(tx0)) tx0 = Double.NEGATIVE_INFINITY;
        if (Double.isNaN(tx1)) tx1 = Double.POSITIVE_INFINITY;
        if (Double.isNaN(ty0)) ty0 = Double.NEGATIVE_INFINITY;
        if (Double.isNaN(ty1)) ty1 = Double.POSITIVE_INFINITY;
        if (Double.isNaN(tz0)) tz0 = Double.NEGATIVE_INFINITY;
        if (Double.isNaN(tz1)) tz1 = Double.POSITIVE_INFINITY;
        
        double tmin = Math.min(tx0, tx1);
        double tmax = Math.max(tx0, tx1);
        tmin = Math.max(tmin, Math.min(ty0, ty1));
        tmax = Math.min(tmax, Math.max(ty0, ty1));
        tmin = Math.max(tmin, Math.min(tz0, tz1));
        tmax = Math.min(tmax, Math.max(tz0, tz1));
        
        if (tmax >= tmin && tmax >= 0.0 && (!terminated || tmin <= 1.0)) return tmin >= 0.0 ? tmin : tmax;
        else return Double.POSITIVE_INFINITY;
    }
    
    public static boolean touchingRay(Box3d box, Vec3d p0, Vec3d dp, boolean terminated)
    {
        return Double.isFinite(raytrace(box, p0, dp, terminated));
    }
    
    public static boolean touching(Box3d box, Vec3d v)
    {
        return v.x >= box.min.x && v.x <= box.max.x &&
               v.y >= box.min.y && v.y <= box.max.y &&
               v.z >= box.min.z && v.z <= box.max.z;
    }
    public static double surfaceArea(Box3d box)
    {
        Vec3d d = Vec3d.sub(box.max, box.min);
        return (d.x*d.y + d.y*d.z + d.z*d.x)*2.0;
    }
    
    public static final void copy(Box3d s, Box3d r)
    {
        Vec3d.copy(s.min, r.min);
        Vec3d.copy(s.max, r.max);
    }
    
    public static final void empty(Box3d r)
    {
        r.min.set(Double.POSITIVE_INFINITY);
        r.max.set(Double.NEGATIVE_INFINITY);
    }
    
    public static final void unit(Box3d r)
    {
        r.min.set(-1.0);
        r.max.set(1.0);
    }
    
    public static final void infinite(Box3d r)
    {
        r.min.set(Double.NEGATIVE_INFINITY);
        r.max.set(Double.POSITIVE_INFINITY);
    }
    
    public static final void expand(Box3d b, Vec3d v, Box3d r)
    {
        r.min.x = Math.min(b.min.x, v.x);
        r.min.y = Math.min(b.min.y, v.y);
        r.min.z = Math.min(b.min.z, v.z);
        r.max.x = Math.max(b.max.x, v.x);
        r.max.y = Math.max(b.max.y, v.y);
        r.max.z = Math.max(b.max.z, v.z);
    }
    
    public static final void expand(Box3d b0, Box3d b1, Box3d r)
    {
        r.min.x = Math.min(b0.min.x, b1.min.x);
        r.min.y = Math.min(b0.min.y, b1.min.y);
        r.min.z = Math.min(b0.min.z, b1.min.z);
        r.max.x = Math.max(b0.max.x, b1.max.x);
        r.max.y = Math.max(b0.max.y, b1.max.y);
        r.max.z = Math.max(b0.max.z, b1.max.z);
    }
    
    public static final void sweep(Box3d b, Vec3d dp, Box3d r)
    {
        r.min.x = Math.min(b.min.x, b.min.x + dp.x);
        r.min.y = Math.min(b.min.y, b.min.y + dp.y);
        r.min.z = Math.min(b.min.z, b.min.z + dp.z);
        r.max.x = Math.max(b.max.x, b.max.x + dp.x);
        r.max.y = Math.max(b.max.y, b.max.y + dp.y);
        r.max.z = Math.max(b.max.z, b.max.z + dp.z);
    }
    
    public static final void translate(Box3d b, Vec3d dp, Box3d r)
    {
        r.min.x = b.min.x + dp.x;
        r.min.y = b.min.y + dp.y;
        r.min.z = b.min.z + dp.z;
        r.max.x = b.max.x + dp.x;
        r.max.y = b.max.y + dp.y;
        r.max.z = b.max.z + dp.z;
    }
    
    public static final Box3d empty()
    {
        Box3d result = new Box3d();
        empty(result);
        return result;
    }
    
    public static final Box3d unit()
    {
        Box3d result = new Box3d();
        unit(result);
        return result;
    }
    
    public static final Box3d infinite()
    {
        Box3d result = new Box3d();
        infinite(result);
        return result;
    }
    
    public static final Box3d expand(Box3d b, Vec3d v)
    {
        Box3d result = new Box3d();
        expand(b, v, result);
        return result;
    }
    
    public static final Box3d expand(Box3d b0, Box3d b1)
    {
        Box3d result = new Box3d();
        expand(b0, b1, result);
        return result;
    }
    
    public static final Box3d translate(Box3d b, Vec3d v)
    {
        Box3d result = new Box3d();
        translate(b, v, result);
        return result;
    }
    
    public final Vec3d min = new Vec3d(), max = new Vec3d();
    
    public Box3d()
    {
    }
    
    public Box3d(double x0, double y0, double z0, double x1, double y1, double z1)
    {
        min.x = x0; min.y = y0; min.z = z0;
        max.x = x1; max.y = y1; max.z = z1;
    }
    
    public Box3d(Vec3d min, Vec3d max)
    {
        Vec3d.copy(min, this.min);
        Vec3d.copy(max, this.max);
    }
    
    public Box3d(Box3d box)
    {
        Vec3d.copy(box.min, this.min);
        Vec3d.copy(box.max, this.max);
    }
    
    public Box3d(Box3 box)
    {
        Vec3d.copy(box.min, this.min);
        Vec3d.copy(box.max, this.max);
    }
    
    public boolean touching(Box3d box)
    {
        return touching(this, box);
    }
    
    public boolean encloses(Box3d box)
    {
        return encloses(this, box);
    }
    
    public boolean touching(Vec3d v)
    {
        return touching(this, v);
    }
    
    public double surfaceArea()
    {
        return surfaceArea(this);
    }
    
    public Box3d set(Box3d box)
    {
        copy(box, this);
        return this;
    }
    
    public Box3d setEmpty()
    {
        empty(this);
        return this;
    }
    
    public Box3d setUnit()
    {
        unit(this);
        return this;
    }
    
    public Box3d setInfinite()
    {
        infinite(this);
        return this;
    }
    
    public Box3d expand(Vec3d v)
    {
        expand(this, v, this);
        return this;
    }
    
    public Box3d expand(Box3d b)
    {
        expand(this, b, this);
        return this;
    }
    
    public Box3d sweep(Vec3d v)
    {
        sweep(this, v, this);
        return this;
    }
    
    public Box3d translsate(Vec3d v)
    {
        translate(this, v, this);
        return this;
    }
    
    @Override
    public String toString()
    {
        return min + " to " + max;
    }
}
