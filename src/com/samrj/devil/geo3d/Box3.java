package com.samrj.devil.geo3d;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;

/**
 * Utility class for storing and reversing series of matrix multiplications.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Box3
{
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns whether or not the two boxes are touching each-other.
     */
    public static boolean touching(Box3 a, Box3 b)
    {
        return a.max.x >= b.min.x && b.max.x >= a.min.x &&
               a.max.y >= b.min.y && b.max.y >= a.min.y &&
               a.max.z >= b.min.z && b.max.z >= a.min.z;
    }
    
    private static boolean axisFailTest(Triangle3 triangle, Vec3 axis, Vec3 edge)
    {
        Vec3 a = Vec3.cross(axis, edge);
        float r = Math.abs(a.x) + Math.abs(a.y) + Math.abs(a.z);
        
        float p0 = Vec3.dot(a, triangle.a().p());
        float p1 = Vec3.dot(a, triangle.b().p());
        float p2 = Vec3.dot(a, triangle.c().p());
        
        float min = Math.min(Math.min(p0, p1), p2);
        if (min > r) return true;
        
        float max = Math.max(Math.max(p0, p1), p2);
        if (max < -r) return true;
        
        return false;
    }
    
    private static final Vec3 AXIS_X = new Vec3(1.0f, 0.0f, 0.0f);
    private static final Vec3 AXIS_Y = new Vec3(0.0f, 1.0f, 0.0f);
    private static final Vec3 AXIS_Z = new Vec3(0.0f, 0.0f, 1.0f);
    
    /**
     * Returns whether or not the given triangle is touching the unit box.
     * 
     * @param t A triangle.
     * @return Whether the triangle is touching the unit box.
     */
    public static boolean touchingUnitBox(Triangle3 t)
    {
        Vec3 a = t.a().p(), b = t.b().p(), c = t.c().p();
        
        //AABB test
        if (!touching(contain(t), unit())) return false;
        
        //Plane intersection test
        Vec3 n = Triangle3.normal(t);
        float pc = Vec3.dot(n, a);
        if (Math.abs(n.x) + Math.abs(n.y) + Math.abs(n.z) < Math.abs(pc)) return false;
        
        //Separating axis tests
        Vec3 ab = Vec3.sub(b, a);
        Vec3 bc = Vec3.sub(c, b);
        Vec3 ca = Vec3.sub(a, c);
        
        if (axisFailTest(t, AXIS_X, ab)) return false;
        if (axisFailTest(t, AXIS_X, bc)) return false;
        if (axisFailTest(t, AXIS_X, ca)) return false;
        if (axisFailTest(t, AXIS_Y, ab)) return false;
        if (axisFailTest(t, AXIS_Y, bc)) return false;
        if (axisFailTest(t, AXIS_Y, ca)) return false;
        if (axisFailTest(t, AXIS_Z, ab)) return false;
        if (axisFailTest(t, AXIS_Z, bc)) return false;
        if (axisFailTest(t, AXIS_Z, ca)) return false;
        
        return true;
    }
    
    /**
     * Returns whether or not the given triangle is touching the given box.
     * 
     * @param box A box.
     * @param t A triangle.
     * @return Whether the triangle and box are touching.
     */
    public static boolean touching(Box3 box, Triangle3 t)
    {
        Vec3 center = Vec3.add(box.min, box.max).mult(0.5f);
        Vec3 radius = Vec3.sub(box.max, box.min).mult(0.5f);
        
        Vec3 a = Vec3.sub(t.a().p(), center).div(radius);
        Vec3 b = Vec3.sub(t.b().p(), center).div(radius);
        Vec3 c = Vec3.sub(t.c().p(), center).div(radius);
        Triangle3 local = Triangle3.from(a, b, c);
        
        return touchingUnitBox(local);
    }
    
    /**
     * Returns whether or not the given box is touching the given ray.
     * 
     * @param box The box to raytrace against.
     * @param p0 The starting position of the ray.
     * @param dp The difference between the start and end of the ray.
     * @param terminated Whether the ray should terminate at the length of dp.
     * @return Whether the ray hit the box.
     */
    public static boolean touchingRay(Box3 box, Vec3 p0, Vec3 dp, boolean terminated)
    {
        float tx1 = (box.min.x - p0.x)/dp.x;
        float tx2 = (box.max.x - p0.x)/dp.x;
        float ty1 = (box.min.y - p0.y)/dp.y;
        float ty2 = (box.max.y - p0.y)/dp.y;
        float tz1 = (box.min.z - p0.z)/dp.z;
        float tz2 = (box.max.z - p0.z)/dp.z;

        float tmin = Math.min(tx1, tx2);
        float tmax = Math.max(tx1, tx2);
        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));
        tmin = Math.max(tmin, Math.min(tz1, tz2));
        tmax = Math.min(tmax, Math.max(tz1, tz2));

        return tmax >= tmin && tmax >= 0.0f && (!terminated || tmin <= 1.0f);
    }
    
    /**
     * Returns whether or not the given box is touching the given edge.
     * 
     * @param box A box.
     * @param e An edge.
     * @return Whether the edge is touching the box.
     */
    public static boolean touching(Box3 box, Edge3 e)
    {
        Vec3 p0 = e.a().p();
        Vec3 dp = Vec3.sub(e.b().p(), p0);
        return touchingRay(box, p0, dp, true);
    }
    // </editor-fold>
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
     * Sets the given box to the unit box, centered around the origin.
     * 
     * @param r The box to set.
     */
    public static final void unit(Box3 r)
    {
        r.min.set(-1.0f);
        r.max.set(1.0f);
    }
    
    /**
     * Sets the given box to be infinitely large.
     * 
     * @param r The box to set.
     */
    public static final void infinite(Box3 r)
    {
        r.min.set(Float.NEGATIVE_INFINITY);
        r.max.set(Float.POSITIVE_INFINITY);
    }
    
    /**
     * Sets the given box to the smallest one which can contain the given
     * oriented box.
     * 
     * @param b An oriented box to contain.
     * @param r The axis-aligned box in which to store the result.
     */
    public static final void contain(OBox3 b, Box3 r)
    {
        Mat3 m = Mat3.rotation(b.transform.rot);
        m.mult(b.transform.sca);
        
        float wx = Math.abs(m.a) + Math.abs(m.b) + Math.abs(m.c);
        float wy = Math.abs(m.d) + Math.abs(m.e) + Math.abs(m.f);
        float wz = Math.abs(m.g) + Math.abs(m.h) + Math.abs(m.i);
        
        r.min.x = b.transform.pos.x - wx;
        r.min.y = b.transform.pos.y - wy;
        r.min.z = b.transform.pos.z - wz;
        r.max.x = b.transform.pos.x + wx;
        r.max.y = b.transform.pos.y + wy;
        r.max.z = b.transform.pos.z + wz;
    }
    
    /**
     * Sets the given box to the smallest one which can contain the given
     * triangle.
     * 
     * @param t The triangle to contain.
     * @param r The box in which to store the result.
     */
    public static final void contain(Triangle3 t, Box3 r)
    {
        Vec3 a = t.a().p(), b = t.b().p(), c = t.c().p();
        r.min.x = Math.min(Math.min(a.x, b.x), c.x);
        r.max.x = Math.max(Math.max(a.x, b.x), c.x);
        r.min.y = Math.min(Math.min(a.y, b.y), c.y);
        r.max.y = Math.max(Math.max(a.y, b.y), c.y);
        r.min.z = Math.min(Math.min(a.z, b.z), c.z);
        r.max.z = Math.max(Math.max(a.z, b.z), c.z);
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
     * Sets {@code r} to the smallest box that can contain both boxes {@code b0}
     * and {@code b1}.
     * 
     * @param b0 A box to expand.
     * @param b1 An oriented box to expand by.
     * @param r The box in which to store the result.
     */
    public static final void expand(Box3 b0, OBox3 b1, Box3 r)
    {
        expand(b0, contain(b1), r);
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
     * 
     * @return A new empty box.
     */
    public static final Box3 empty()
    {
        Box3 result = new Box3();
        empty(result);
        return result;
    }
    
    /**
     * Returns a new unit box.
     * 
     * @return A new unit box.
     */
    public static final Box3 unit()
    {
        Box3 result = new Box3();
        unit(result);
        return result;
    }
    
    /**
     * Returns a new infinite box.
     * 
     * @return A new infinite box.
     */
    public static final Box3 infinite()
    {
        Box3 result = new Box3();
        infinite(result);
        return result;
    }
    
    /**
     * Returns the smallest axis-aligned box that can contain the given oriented
     * box.
     * 
     * @param b An oriented box to contain.
     * @return A new box containing the result.
     */
    public static final Box3 contain(OBox3 b)
    {
        Box3 result = new Box3();
        contain(b, result);
        return result;
    }
    
    /**
     * Returns the smallest box that can contain the given triangle.
     * 
     * @param t A triangle to contain.
     * @return A new box containing the result.
     */
    public static final Box3 contain(Triangle3 t)
    {
        Box3 result = new Box3();
        contain(t, result);
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
     * Returns the smallest box that can contain both given boxes.
     * 
     * @param b0 The first box to contain.
     * @param b1 A second, oriented box to contain.
     * @return A new box containing the result.
     */
    public static final Box3 expand(Box3 b0, OBox3 b1)
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
    
    // <editor-fold defaultstate="collapsed" desc="Instance accessor methods">
    /**
     * Returns whether or not this is touching the given box.
     * 
     * @param box A box.
     * @return Whether or not this is touching the given box.
     */
    public boolean touching(Box3 box)
    {
        return touching(this, box);
    }
    
    /**
     * Returns whether this is touching the given triangle.
     * 
     * @param t A triangle.
     * @return Whether or not this is touching the triangle.
     */
    public boolean touching(Triangle3 t)
    {
        return touching(this, t);
    }
    // </editor-fold>
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
    
    public Box3 setUnit()
    {
        unit(this);
        return this;
    }
    
    public Box3 setInfinite()
    {
        infinite(this);
        return this;
    }
    
    public Box3 setContain(OBox3 b)
    {
        contain(b, this);
        return this;
    }
    
    public Box3 setContain(Triangle3 t)
    {
        contain(t, this);
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
     * Expands this by the given oriented box.
     * 
     * @param b An oriented box to expand by.
     * @return This box.
     */
    public Box3 expand(OBox3 b)
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
