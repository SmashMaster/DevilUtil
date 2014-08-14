package com.samrj.devil.math;

import com.samrj.devil.buffer.Bufferable;
import com.samrj.devil.buffer.FloatBuffer;
import com.samrj.devil.math.Util.Axis;
import com.samrj.devil.math.numerical.NumState;
import org.lwjgl.opengl.GL11;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Vector2f implements Bufferable<FloatBuffer>, NumState<Vector2f>
{
    // <editor-fold defaultstate="collapsed" desc="Static Factories">
    public static Vector2f versor(Axis axis)
    {
        switch (axis)
        {
            case X: return new Vector2f(1f, 0f);
            case Y: return new Vector2f(0f, 1f);
            default: throw new IllegalArgumentException();
        }
    }
    // </editor-fold>
    
    public float x, y;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Vector2f(float x, float y)
    {
        set(x, y);
    }
    
    public Vector2f(Vector2f v)
    {
        set(v);
    }
    
    public Vector2f(Vector2i v)
    {
        set(v);
    }
    
    public Vector2f(org.jbox2d.common.Vec2 v)
    {
        set(v);
    }
    
    public Vector2f()
    {
        set();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutators">
    public Vector2f set(float x, float y)
    {
        this.x = x; this.y = y;
        return this;
    }

    public Vector2f set(Vector2f v)
    {
        return set(v.x, v.y);
    }
    
    public Vector2f set(Vector2i v)
    {
        return set(v.x, v.y);
    }
    
    public Vector2f set(org.jbox2d.common.Vec2 v)
    {
        return set(v.x, v.y);
    }
    
    public Vector2f set()
    {
        return set(0f, 0f);
    }

    public Vector2f add(float x, float y)
    {
        return set(this.x+x, this.y+y);
    }
    
    public Vector2f add(Vector2f v)
    {
        return add(v.x, v.y);
    }
    
    public Vector2f sub(float x, float y)
    {
        return set(this.x-x, this.y-y);
    }
    
    public Vector2f sub(Vector2f v)
    {
        return sub(v.x, v.y);
    }
    
    public Vector2f mult(float f)
    {
        return set(x*f, y*f);
    }
    
    public Vector2f mult(float x, float y)
    {
        return set(this.x*x, this.y*y);
    }
    
    public Vector2f mult(Vector2f v)
    {
        return mult(v.x, v.y);
    }
    
    public Vector2f mult(Matrix2f mat)
    {
        return set(x*mat.a + y*mat.b,
                   x*mat.c + y*mat.d);
    }
    
    public Vector2f mult(Matrix3f mat)
    {
        return set(x*mat.a + y*mat.b + mat.c,
                   x*mat.d + y*mat.e + mat.f);
    }
    
    public Vector2f mult(Matrix4f mat)
    {
        return set(x*mat.a + y*mat.b + mat.c + mat.d,
                   x*mat.e + y*mat.f + mat.g + mat.h);
    }
    
    /**
     * Sets this to the cross product of a three-dimensional vector whose z
     * component is {@code z}.
     */
    public Vector2f cross(float z)
    {
        return set(y*z, -x*z);
    }
    
    public Vector2f div(float f)
    {
        return set(x/f, y/f);
    }
    
    public Vector2f div(float x, float y)
    {
        return set(this.x/x, this.y/y);
    }
    
    public Vector2f div(Vector2f v)
    {
        return div(v.x, v.y);
    }
    
    public Vector2f loop(float min, float max)
    {
        return set(Util.loop(x, min, max),
                   Util.loop(y, min, max));
    }
    
    public Vector2f lerp(Vector2f v, float t)
    {
        return set(Util.lerp(x, v.x, t),
                   Util.lerp(y, v.y, t));
    }
    
    public Vector2f avg(Vector2f v)
    {
        return add(v).mult(.5f);
    }
    
    public Vector2f normalize()
    {
        if (isZero(Float.MIN_NORMAL)) return this;
        return div(length());
    }
    
    public Vector2f setLength(float length)
    {
        if (isZero(Float.MIN_NORMAL)) return this;
        return mult(length/length());
    }
    
    public Vector2f setDist(Vector2f v, float dist)
    {
        return sub(v).setLength(dist).add(v);
    }
    
    public Vector2f setChebyLength(float length)
    {
        if (isZero(Float.MIN_NORMAL)) return this;
        return mult(length/chebyLength());
    }
    
    public Vector2f setChebyDist(Vector2f v, float dist)
    {
        return sub(v).setChebyLength(dist).add(v);
    }
    
    public Vector2f rotate(float angle)
    {
        return rotate(Util.sin(angle), Util.cos(angle));
    }
    
    public Vector2f rotate(float sin, float cos)
    {
        return set(x*cos - y*sin, x*sin + y*cos);
    }
    
    public Vector2f rotate(Vector2f v)
    {
        return rotate(v.x, v.y);
    }
    
    /**
     * Sets this to the vector projection of this onto {@code v}.
     * 
     * @param v the vector to project onto.
     * @return this.
     */
    public Vector2f projVec(Vector2f v)
    {
        float m = dot(v)/v.squareLength();
        return set(v).mult(m);
    }
    
    /**
     * Same as {@code projVec} but assumes {@code v} is a unit vector.
     */
    public Vector2f projUnitVec(Vector2f v)
    {
        float m = dot(v);
        return set(v).mult(m);
    }
    
    public Vector2f reflect(Vector2f n)
    {
        final float m = 2f*dot(n);
        
        return set(m*n.x - x,
                   m*n.y - y);
    }
    
    public Vector2f rotCW()     {return set(y, -x);}
    public Vector2f rotCCW()    {return set(-y, x);}
    public Vector2f flipX()     {return set(-x, y);}
    public Vector2f flipY()     {return set(x, -y);}
    public Vector2f negate()    {return set(-x, -y);}
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Nonlocal Mutators">
    public Vector2f cadd(float x, float y)  {return clone().add(x, y);}
    public Vector2f cadd(Vector2f v)        {return clone().add(v);}
    public Vector2f csub(float x, float y)  {return clone().sub(x, y);}
    public Vector2f csub(Vector2f v)        {return clone().sub(v);}
    public Vector2f cmult(float s)          {return clone().mult(s);}
    public Vector2f cmult(float x, float y) {return clone().mult(x, y);}
    public Vector2f cmult(Vector2f v)       {return clone().mult(v);}
    public Vector2f cmult(Matrix2f mat)     {return clone().mult(mat);}
    public Vector2f cmult(Matrix3f mat)     {return clone().mult(mat);}
    public Vector2f cdiv(float s)           {return clone().div(s);}
    
    public Vector2f cloop(float min, float max) {return clone().loop(min, max);}
    public Vector2f clerp(Vector2f v, float t)  {return clone().lerp(v, t);}
    
    public Vector2f cavg(Vector2f v) {return clone().avg(v);}
    public Vector2f cnormalize()     {return clone().normalize();}
    public Vector2f crotCW()         {return clone().rotCW();}
    public Vector2f crotCCW()        {return clone().rotCCW();}
    public Vector2f cflipX()         {return clone().flipX();}
    public Vector2f cflipY()         {return clone().flipY();}
    public Vector2f cnegate()        {return clone().negate();}
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessors">
    public float squareLength()
    {
        return x*x + y*y;
    }

    public float length()
    {
        return Util.sqrt(squareLength());
    }
    
    public float chebyLength()
    {
        return Math.max(Math.abs(x),
                        Math.abs(y));
    }
    
    public float squareDist(Vector2f v)
    {
        if (v == null) throw new IllegalArgumentException();
        
        final float dx = x - v.x,
                    dy = y - v.y;
        
        return dx*dx + dy*dy;
    }

    public float dist(Vector2f v)
    {
        return Util.sqrt(squareDist(v));
    }
    
    public float chebyDist(Vector2f v)
    {
        return Math.max(Math.abs(v.x - x),
                        Math.abs(v.y - y));
    }
    
    public float dot(Vector2f v)
    {
        return x*v.x + y*v.y;
    }
    
    public float cross(Vector2f v)
    {
        return x*v.y - v.x*y;
    }
    
    /**
     * Returns the scalar projection of this onto {@code v}.
     * 
     * @param v the vector to project onto.
     * @return the scalar projection of this onto {@code v}.
     */
    public float projScal(Vector2f v)
    {
        return dot(v)/v.length();
    }
    
    public float angle()
    {
        return Util.atan2(y, x);
    }
    
    public boolean isZero(float threshold)
    {
        return Util.isZero(x, threshold) &&
               Util.isZero(y, threshold);
    }
    
    public boolean isZero()
    {
        return x == 0f && y == 0f;
    }
    
    public boolean epsEqual(Vector2f v, int tolerance)
    {
        return Util.epsEqual(v.x, x, tolerance) &&
               Util.epsEqual(v.y, y, tolerance);
    }
    
    public Vector2i floor()
    {
        return new Vector2i(Util.floor(x), Util.floor(y));
    }
    
    public Vector2i round()
    {
        return new Vector2i(Math.round(x), Math.round(y));
    }
    
    public Vector2i ceil()
    {
        return new Vector2i(Util.ceil(x), Util.ceil(y));
    }
    
    public void glVertex()
    {
        GL11.glVertex2f(x, y);
    }
    
    public void glDblVertex()
    {
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y);
    }
    
    public void glTranslate()
    {
        GL11.glTranslatef(x, y, 0f);
    }
    
    public org.jbox2d.common.Vec2 asB2D()
    {
        return new org.jbox2d.common.Vec2(x, y);
    }
    
    public Vector2d as2D()
    {
        return new Vector2d(this);
    }
    
    public boolean isFinite()
    {
        return Util.isFinite(x) && Util.isFinite(y);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Object Overriden Methods">
    @Override
    public String toString()
    {
        return "("+x+", "+y+")";
    }
    
    @Override
    public Vector2f clone()
    {
        return new Vector2f(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Vector2f other = (Vector2f) obj;
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) return false;
        if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) return false;
        return true;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Bufferable Overriden Methods">
    @Override
    public void putIn(FloatBuffer buf)
    {
        buf.put(x, y);
    }
    
    @Override
    public int size()
    {
        return 2;
    }
    // </editor-fold>
}