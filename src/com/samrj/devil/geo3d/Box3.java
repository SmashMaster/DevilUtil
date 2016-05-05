package com.samrj.devil.geo3d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;

public class Box3
{
    /**
     * Returns whether or not the two boxes are touching each-other.
     */
    public static boolean touching(Box3 a, Box3 b)
    {
        return a.max.x >= b.min.x && b.max.x >= a.min.x &&
               a.max.y >= b.min.y && b.max.y >= a.min.y &&
               a.max.z >= b.min.z && b.max.z >= a.min.z;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies the source box into the target box. 
     * 
     * @param s The box to copy from.
     * @param r The box to copy into.
     */
    public static final void copy(Box3 s, Box3 r)
    {
        Vec3.copy(s.min, r.min);
        Vec3.copy(s.max, r.max);
    }
    
    /**
     * Sets the given box to be infinitely empty. Behaves well during expansion.
     * 
     * @param r The box to set to be empty.
     */
    public static final void empty(Box3 r)
    {
        r.min.set(Float.POSITIVE_INFINITY);
        r.max.set(Float.NEGATIVE_INFINITY);
    }
    
    /**
     * Sets {@code r} to the smallest box that can contain the box {@code b} and
     * vector {@code v}.
     * 
     * @param b The box to expand.
     * @param v The vector to expand by.
     * @param r The box in which to store the result.
     */
    public static final void expand(Box3 b, Vec3 v, Box3 r)
    {
        r.min.x = Util.min(b.min.x, v.x);
        r.min.y = Util.min(b.min.y, v.y);
        r.min.z = Util.min(b.min.z, v.z);
        r.max.x = Util.max(b.max.x, v.x);
        r.max.y = Util.max(b.max.y, v.y);
        r.max.z = Util.max(b.max.z, v.z);
    }
    
    /**
     * Sets {@code r} to the smallest box that can contain both boxes {@code b0}
     * and {@code b1}.
     * 
     * @param b0 A box to expand.
     * @param b1 Another box to expand.
     * @param r The box in which to store the result.
     */
    public static final void expand(Box3 b0, Box3 b1, Box3 r)
    {
        r.min.x = Util.min(b0.min.x, b1.min.x);
        r.min.y = Util.min(b0.min.y, b1.min.y);
        r.min.z = Util.min(b0.min.z, b1.min.z);
        r.max.x = Util.max(b0.max.x, b1.max.x);
        r.max.y = Util.max(b0.max.y, b1.max.y);
        r.max.z = Util.max(b0.max.z, b1.max.z);
    }
    
    /**
     * Sets {@code r} to a box containing {@code b} in its initial position and
     * in an offset position defined by {@code dp}.
     * 
     * @param b The initial box.
     * @param dp The offset to sweep by.
     * @param r The box in which to store the result.
     */
    public static final void sweep(Box3 b, Vec3 dp, Box3 r)
    {
        r.min.x = Util.min(b.min.x, b.min.x + dp.x);
        r.min.y = Util.min(b.min.y, b.min.y + dp.y);
        r.min.z = Util.min(b.min.z, b.min.z + dp.z);
        r.max.x = Util.max(b.max.x, b.max.x + dp.x);
        r.max.y = Util.max(b.max.y, b.max.y + dp.y);
        r.max.z = Util.max(b.max.z, b.max.z + dp.z);
    }
    
    /**
     * Offset the position of the first given box by the given vector, and
     * stores the result in the second box.
     * 
     * @param b The box to translate.
     * @param dp The vector to translate by.
     * @param r The box in which to store the result.
     */
    public static final void translate(Box3 b, Vec3 dp, Box3 r)
    {
        r.min.x = b.min.x + dp.x;
        r.min.y = b.min.y + dp.y;
        r.min.z = b.min.z + dp.z;
        r.max.x = b.max.x + dp.x;
        r.max.y = b.max.y + dp.y;
        r.max.z = b.max.z + dp.z;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns a new empty box. Behaves well during expansion.
     * @return A new empty box.
     */
    public static final Box3 empty()
    {
        Box3 result = new Box3();
        empty(result);
        return result;
    }
    
    /**
     * Returns the smallest box that can contain the given box and vector.
     * 
     * @param b A box to expand.
     * @param v A vector to expand by.
     * @return A new box containing the result.
     */
    public static final Box3 expand(Box3 b, Vec3 v)
    {
        Box3 result = new Box3();
        expand(b, v, result);
        return result;
    }
    
    /**
     * Returns the smallest box that can contain both given boxes.
     * 
     * @param b0 The first box to contain.
     * @param b1 The second box to contain.
     * @return A new box containing the result.
     */
    public static final Box3 expand(Box3 b0, Box3 b1)
    {
        Box3 result = new Box3();
        expand(b0, b1, result);
        return result;
    }
    
    /**
     * Translates the given box by the given vector and returns the result in
     * a new box.
     * 
     * @param b The box to translate.
     * @param v The vector to translate by.
     * @return A new box containing the result.
     */
    public static final Box3 translate(Box3 b, Vec3 v)
    {
        Box3 result = new Box3();
        translate(b, v, result);
        return result;
    }
    // </editor-fold>
    
    public final Vec3 min = new Vec3(), max = new Vec3();
    
    /**
     * Creates a new zero box. Contain one point--the origin.
     */
    public Box3()
    {
    }
    
    /**
     * Creates a new box with the given bounds.
     */
    public Box3(float x0, float y0, float z0, float x1, float y1, float z1)
    {
        min.x = x0; min.y = y0; min.z = z0;
        max.x = x1; max.y = y1; max.z = z1;
    }
    
    /**
     * Creates a new box with the given bounds vectors.
     */
    public Box3(Vec3 min, Vec3 max)
    {
        Vec3.copy(min, this.min);
        Vec3.copy(max, this.max);
    }
    
    /**
     * Creates a new box equal to the given box.
     * 
     * @param box The box to copy.
     */
    public Box3(Box3 box)
    {
        Vec3.copy(box.min, this.min);
        Vec3.copy(box.max, this.max);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this to the empty box and returns this.
     * 
     * @return This box.
     */
    public Box3 setEmpty()
    {
        empty(this);
        return this;
    }
    
    /**
     * Expands this by the given vector and returns this.
     * 
     * @param v The vector to expand by.
     * @return This box.
     */
    public Box3 expand(Vec3 v)
    {
        expand(this, v, this);
        return this;
    }
    
    /**
     * Expands this by the given box and return this.
     * 
     * @param b The box to expand by.
     * @return This box.
     */
    public Box3 expand(Box3 b)
    {
        expand(this, b, this);
        return this;
    }
    
    /**
     * Expands this box by with an offset version of itself.
     * 
     * @param v The vector to offset by.
     * @return This box.
     */
    public Box3 sweep(Vec3 v)
    {
        sweep(this, v, this);
        return this;
    }
    
    /**
     * Translates this box by the given vector.
     * 
     * @param v The vector to translate by.
     * @return This box.
     */
    public Box3 translsate(Vec3 v)
    {
        translate(this, v, this);
        return this;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    @Override
    public String toString()
    {
        return min + " to " + max;
    }
    // </editor-fold>
}
