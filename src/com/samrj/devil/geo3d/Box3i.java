package com.samrj.devil.geo3d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3i;

/**
 * Axis-aligned integer bounding box class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2021 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Box3i
{
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns whether or not the two boxes are touching each-other.
     */
    public static boolean touching(Box3i a, Box3i b)
    {
        return a.max.x >= b.min.x && b.max.x >= a.min.x &&
               a.max.y >= b.min.y && b.max.y >= a.min.y &&
               a.max.z >= b.min.z && b.max.z >= a.min.z;
    }
    
    /**
     * Returns whether or not the second box is totally enclosed by the first.
     */
    public static boolean encloses(Box3i a, Box3i b)
    {
        return a.max.x >= b.max.x && a.min.x <= b.min.x &&
               a.max.y >= b.max.y && a.min.y <= b.min.y &&
               a.max.z >= b.max.z && a.min.z <= b.min.z;
    }
    
    /**
     * Returns whether the given box is touching the given vertex.
     * 
     * @param box A box.
     * @param v A vertex.
     * @return Whether the vertex is touching the box.
     */
    public static boolean touching(Box3i box, Vec3i v)
    {
        return v.x >= box.min.x && v.x <= box.max.x &&
               v.y >= box.min.y && v.y <= box.max.y &&
               v.z >= box.min.z && v.z <= box.max.z;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies the source box into the target box. 
     * 
     * @param s The box to copy from.
     * @param r The box to copy into.
     */
    public static final void copy(Box3i s, Box3i r)
    {
        Vec3i.copy(s.min, r.min);
        Vec3i.copy(s.max, r.max);
    }
    
    /**
     * Sets the given box to be infinitely empty. Behaves well during expansion.
     * 
     * @param r The box to set to be empty.
     */
    public static final void empty(Box3i r)
    {
        r.min.set(Integer.MAX_VALUE);
        r.max.set(Integer.MIN_VALUE);
    }
    
    /**
     * Sets the given box to a cube of width 2, centered around the origin.
     * 
     * @param r The box to set.
     */
    public static final void unit(Box3i r)
    {
        r.min.set(-1);
        r.max.set(1);
    }
    
    /**
     * Sets the given box to be as large as possible.
     * 
     * @param r The box to set.
     */
    public static final void infinite(Box3i r)
    {
        r.min.set(Integer.MIN_VALUE);
        r.max.set(Integer.MAX_VALUE);
    }
    
    /**
     * Sets {@code r} to the smallest one which can contain the given box.
     * 
     * @param b An oriented box to contain.
     * @param r The axis-aligned box in which to store the result.
     */
    public static final void contain(Box3 b, Box3i r)
    {
        r.min.x = Util.floor(b.min.x);
        r.min.y = Util.floor(b.min.y);
        r.min.z = Util.floor(b.min.z);
        r.max.x = Util.ceil(b.max.x);
        r.max.y = Util.ceil(b.max.y);
        r.max.z = Util.ceil(b.max.z);
    }
    
    /**
     * Sets {@code r} to the smallest box that can contain the box {@code b} and
     * vector {@code v}.
     * 
     * @param b The box to expand.
     * @param v The vector to expand by.
     * @param r The box in which to store the result.
     */
    public static final void expand(Box3i b, Vec3i v, Box3i r)
    {
        r.min.x = Math.min(b.min.x, v.x);
        r.min.y = Math.min(b.min.y, v.y);
        r.min.z = Math.min(b.min.z, v.z);
        r.max.x = Math.max(b.max.x, v.x);
        r.max.y = Math.max(b.max.y, v.y);
        r.max.z = Math.max(b.max.z, v.z);
    }
    
    /**
     * Sets {@code r} to the smallest box that can contain both boxes {@code b0}
     * and {@code b1}.
     * 
     * @param b0 A box to expand.
     * @param b1 Another box to expand.
     * @param r The box in which to store the result.
     */
    public static final void expand(Box3i b0, Box3i b1, Box3i r)
    {
        r.min.x = Math.min(b0.min.x, b1.min.x);
        r.min.y = Math.min(b0.min.y, b1.min.y);
        r.min.z = Math.min(b0.min.z, b1.min.z);
        r.max.x = Math.max(b0.max.x, b1.max.x);
        r.max.y = Math.max(b0.max.y, b1.max.y);
        r.max.z = Math.max(b0.max.z, b1.max.z);
    }
    
    /**
     * Sets {@code r} to the intersection of the given boxes, or to the empty
     * box if they do not intersect.
     * 
     * @param a The first box to intersect.
     * @param b The second box to intersect.
     * @param r The box in which to store the result.
     */
    public static final void intersect(Box3i a, Box3i b, Box3i r)
    {
        int minX = Math.max(a.min.x, b.min.x);
        int minY = Math.max(a.min.y, b.min.y);
        int minZ = Math.max(a.min.z, b.min.z);
        int maxX = Math.min(a.max.x, b.max.x);
        int maxY = Math.min(a.max.y, b.max.y);
        int maxZ = Math.min(a.max.z, b.max.z);
        
        if (maxX < minX || maxY < minY || maxZ < minZ) r.setEmpty();
        else
        {
            r.min.set(minX, minY, minZ);
            r.max.set(maxX, maxY, maxZ);
        }
    }
    
    /**
     * Sets {@code r} to a box containing {@code b} in its initial position and
     * in an offset position defined by {@code dp}.
     * 
     * @param b The initial box.
     * @param dp The offset to sweep by.
     * @param r The box in which to store the result.
     */
    public static final void sweep(Box3i b, Vec3i dp, Box3i r)
    {
        r.min.x = Math.min(b.min.x, b.min.x + dp.x);
        r.min.y = Math.min(b.min.y, b.min.y + dp.y);
        r.min.z = Math.min(b.min.z, b.min.z + dp.z);
        r.max.x = Math.max(b.max.x, b.max.x + dp.x);
        r.max.y = Math.max(b.max.y, b.max.y + dp.y);
        r.max.z = Math.max(b.max.z, b.max.z + dp.z);
    }
    
    /**
     * Offset the position of the first given box by the given vector, and
     * stores the result in the second box.
     * 
     * @param b The box to translate.
     * @param dp The vector to translate by.
     * @param r The box in which to store the result.
     */
    public static final void translate(Box3i b, Vec3i dp, Box3i r)
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
     * 
     * @return A new empty box.
     */
    public static final Box3i empty()
    {
        Box3i result = new Box3i();
        empty(result);
        return result;
    }
    
    /**
     * Returns a new unit box.
     * 
     * @return A new unit box.
     */
    public static final Box3i unit()
    {
        Box3i result = new Box3i();
        unit(result);
        return result;
    }
    
    /**
     * Returns a new infinite box.
     * 
     * @return A new infinite box.
     */
    public static final Box3i infinite()
    {
        Box3i result = new Box3i();
        infinite(result);
        return result;
    }
    
    /**
     * Returns the smallest integer box that can contain the given box.
     * 
     * @param b The box to contain.
     * @return A new box.
     */
    public static final Box3i contain(Box3 b)
    {
        Box3i result = new Box3i();
        contain(b, result);
        return result;
    }
    
    /**
     * Returns the smallest box that can contain the given box and vector.
     * 
     * @param b A box to expand.
     * @param v A vector to expand by.
     * @return A new box containing the result.
     */
    public static final Box3i expand(Box3i b, Vec3i v)
    {
        Box3i result = new Box3i();
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
    public static final Box3i expand(Box3i b0, Box3i b1)
    {
        Box3i result = new Box3i();
        expand(b0, b1, result);
        return result;
    }
    
    /**
     * Returns the intersection of the two given boxes.
     * 
     * @param b0 The first box to intersect.
     * @param b1 The second box to intersect.
     * @return A new box containing the result.
     */
    public static final Box3i intersect(Box3i b0, Box3i b1)
    {
        Box3i result = new Box3i();
        intersect(b0, b1, result);
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
    public static final Box3i translate(Box3i b, Vec3i v)
    {
        Box3i result = new Box3i();
        translate(b, v, result);
        return result;
    }
    // </editor-fold>
    
    public final Vec3i min = new Vec3i(), max = new Vec3i();
    
    /**
     * Creates a new zero box. Contain one point--the origin.
     */
    public Box3i()
    {
    }
    
    /**
     * Creates a new box with the given bounds.
     */
    public Box3i(int x0, int y0, int z0, int x1, int y1, int z1)
    {
        min.x = x0; min.y = y0; min.z = z0;
        max.x = x1; max.y = y1; max.z = z1;
    }
    
    /**
     * Creates a new box with the given bounds vectors.
     */
    public Box3i(Vec3i min, Vec3i max)
    {
        Vec3i.copy(min, this.min);
        Vec3i.copy(max, this.max);
    }
    
    /**
     * Creates a new box equal to the given box.
     * 
     * @param box The box to copy.
     */
    public Box3i(Box3i box)
    {
        Vec3i.copy(box.min, this.min);
        Vec3i.copy(box.max, this.max);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Instance accessor methods">
    /**
     * Returns whether or not this is touching the given box.
     * 
     * @param box A box.
     * @return Whether or not this is touching the given box.
     */
    public boolean touching(Box3i box)
    {
        return touching(this, box);
    }
    
    /**
     * Returns whether or not the given box is totally enclosed by this.
     */
    public boolean encloses(Box3i b)
    {
        return encloses(this, b);
    }
    
    /**
     * Returns whether this is touching the given vertex.
     * 
     * @param v A vertex.
     * @return True if this box touches the vertex.
     */
    public boolean touching(Vec3i v)
    {
        return touching(this, v);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this to the given box.
     * 
     * @param b The box to set this to.
     * @return This box.
     */
    public Box3i set(Box3i b)
    {
        copy(b, this);
        return this;
    }
    
    /**
     * Sets this to the empty box and returns this.
     * 
     * @return This box.
     */
    public Box3i setEmpty()
    {
        empty(this);
        return this;
    }
    
    /**
     * Sets this to a cube of width 2, centered around the origin.
     * 
     * @return This box.
     */
    public Box3i setUnit()
    {
        unit(this);
        return this;
    }
    
    /**
     * Sets this to the infinite box.
     * 
     * @return This box.
     */
    public Box3i setInfinite()
    {
        infinite(this);
        return this;
    }
    
    /**
     * Sets this to the smallest integer box that can contain the given float
     * box.
     * 
     * @param b The box to contain.
     * @return This box.
     */
    public Box3i setContain(Box3 b)
    {
        contain(b, this);
        return this;
    }
    
    /**
     * Expands this by the given vector and returns this.
     * 
     * @param v The vector to expand by.
     * @return This box.
     */
    public Box3i expand(Vec3i v)
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
    public Box3i expand(Box3i b)
    {
        expand(this, b, this);
        return this;
    }
    
    /**
     * Sets this to its intersection with the given box.
     * 
     * @param b The box to intersect with.
     * @return This box.
     */
    public Box3i intersect(Box3i b)
    {
        intersect(this, b, this);
        return this;
    }
    
    /**
     * Expands this box by with an offset version of itself.
     * 
     * @param v The vector to offset by.
     * @return This box.
     */
    public Box3i sweep(Vec3i v)
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
    public Box3i translsate(Vec3i v)
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
