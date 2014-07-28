package com.samrj.devil.math;

import static com.samrj.devil.math.Util.Axis.X;
import static com.samrj.devil.math.Util.Axis.Y;

public class Vector2d
{
    // <editor-fold defaultstate="collapsed" desc="Static Factories">
    public static Vector2f versor(Util.Axis axis)
    {
        switch (axis)
        {
            case X: return new Vector2f(1f, 0f);
            case Y: return new Vector2f(0f, 1f);
            default: throw new IllegalArgumentException();
        }
    }
    // </editor-fold>
    
    public double x, y;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Vector2d(float x, float y)
    {
        set(x, y);
    }
    
    public Vector2d(Vector2d v)
    {
        set(v);
    }
    
    public Vector2d(Vector2f v)
    {
        set(v);
    }
    
    public Vector2d(Vector2i v)
    {
        set(v);
    }
    
    public Vector2d(org.jbox2d.common.Vec2 v)
    {
        set(v);
    }
    
    public Vector2d()
    {
        set();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutators">
    public Vector2d set(double x, double y)
    {
        this.x = x; this.y = y;
        return this;
    }
    
    public Vector2d set(Vector2d v)
    {
        return set(v.x, v.y);
    }

    public Vector2d set(Vector2f v)
    {
        return set(v.x, v.y);
    }
    
    public Vector2d set(Vector2i v)
    {
        return set(v.x, v.y);
    }
    
    public Vector2d set(org.jbox2d.common.Vec2 v)
    {
        return set(v.x, v.y);
    }
    
    public Vector2d set()
    {
        return set(0f, 0f);
    }

    public Vector2d add(double x, double y)
    {
        return set(this.x+x, this.y+y);
    }
    
    public Vector2d add(Vector2d v)
    {
        return add(v.x, v.y);
    }
    
    public Vector2d add(Vector2f v)
    {
        return add(v.x, v.y);
    }
    
    public Vector2d sub(double x, double y)
    {
        return set(this.x-x, this.y-y);
    }
    
    public Vector2d sub(Vector2d v)
    {
        return sub(v.x, v.y);
    }
    
    public Vector2d sub(Vector2f v)
    {
        return sub(v.x, v.y);
    }
    
    public Vector2d mult(double s)
    {
        return set(x*s, y*s);
    }
    
    public Vector2d mult(Matrix2f mat)
    {
        return set(x*mat.a + y*mat.b,
                   x*mat.c + y*mat.d);
    }
    
    public Vector2d mult(Matrix3f mat)
    {
        return set(x*mat.a + y*mat.b + mat.c,
                   x*mat.d + y*mat.e + mat.f);
    }
    
    public Vector2d div(double s)
    {
        return set(x/s, y/s);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Nonlocal Mutators">
    public Vector2d cadd(float x, float y)
    {
        return clone().add(x, y);
    }
    
    public Vector2d cadd(Vector2d v)
    {
        return clone().add(v);
    }
    
    public Vector2d cadd(Vector2f v)
    {
        return clone().add(v);
    }
    
    public Vector2d csub(double x, double y)
    {
        return clone().sub(x, y);
    }

    public Vector2d csub(Vector2d v)
    {
        return clone().sub(v);
    }
    
    public Vector2d csub(Vector2f v)
    {
        return clone().sub(v);
    }
    
    public Vector2d cmult(double s)
    {
        return clone().mult(s);
    }
    
    public Vector2d cmult(Matrix2f mat)
    {
        return clone().mult(mat);
    }
    
    public Vector2d cmult(Matrix3f mat)
    {
        return clone().mult(mat);
    }
    
    public Vector2d cdiv(double s)
    {
        return clone().div(s);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessors">
    public double squareLength()
    {
        return x*x + y*y;
    }

    public double length()
    {
        return Math.sqrt(squareLength());
    }
    
    public double squareDist(Vector2d v)
    {
        if (v == null) throw new NullPointerException();
        
        final double dx = x - v.x,
                     dy = y - v.y;
        
        return dx*dx + dy*dy;
    }

    public double dist(Vector2d v)
    {
        return Math.sqrt(squareDist(v));
    }
    
    public double dot(Vector2d v)
    {
        return x*v.x + y*v.y;
    }
    
    public double cross(Vector2d v)
    {
        return x*v.y - v.x*y;
    }
    
    public Vector2f as2f()
    {
        return new Vector2f((float)x, (float)y);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Object Overriden Methods">
    @Override
    public String toString()
    {
        return "("+x+", "+y+")";
    }
    
    @Override
    public Vector2d clone()
    {
        return new Vector2d(this);
    }
    // </editor-fold>
}