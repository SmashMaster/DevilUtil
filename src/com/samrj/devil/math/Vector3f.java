package com.samrj.devil.math;


import com.samrj.devil.buffer.Bufferable;
import com.samrj.devil.math.Util.Axis;
import com.samrj.devil.math.numerical.NumState;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Vector3f implements Bufferable<FloatBuffer>, NumState<Vector3f>
{
    /*
     * Sources of information, code, or algorithms:
     * [0] OGRE by Torus Knot Software Ltd - http://www.ogre3d.org/
     */
    
    // <editor-fold defaultstate="collapsed" desc="Factory Methods">
    public static Vector3f versor(Axis axis)
    {
        return axis.versor();
    }
    // </editor-fold>
    
    public float x, y, z;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Vector3f(float x, float y, float z)
    {
        set(x, y, z);
    }
    
    public Vector3f(Vector3f v)
    {
        set(v);
    }
    
    public Vector3f()
    {
        set();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutator Methods">
    public Vector3f set(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
        return this;
    }

    public Vector3f set(Vector3f v)
    {
        return set(v.x, v.y, v.z);
    }
    
    public Vector3f set()
    {
        return set(0f, 0f, 0f);
    }

    public Vector3f add(float x, float y, float z)
    {
        return set(this.x+x, this.y+y, this.z+z);
    }
    
    public Vector3f add(Vector3f v)
    {
        return add(v.x, v.y, v.z);
    }
    
    public Vector3f sub(float x, float y, float z)
    {
        return set(this.x-x, this.y-y, this.z-z);
    }

    public Vector3f sub(Vector3f v)
    {
        return sub(v.x, v.y, v.z);
    }
    
    public Vector3f mult(float s)
    {
        return set(x*s, y*s, z*s);
    }
    
    public Vector3f mult(float x, float y, float z)
    {
        return set(this.x*x, this.y*y, this.z*z);
    }
    
    public Vector3f mult(Vector3f v)
    {
        return mult(v.x, v.y, v.z);
    }
    
    public Vector3f mult(Matrix3f m)
    {
        return set(m.a*x + m.b*y + m.c*z,
                   m.d*x + m.e*y + m.f*z,
                   m.g*x + m.h*y + m.i*z);
    }
    
    public Vector3f mult(Matrix4f m)
    {
        return set(m.a*x + m.b*y + m.c*z + m.d,
                   m.e*x + m.f*y + m.g*z + m.h,
                   m.i*x + m.j*y + m.k*z + m.l);
    }
    
    public Vector3f div(float s)
    {
        return set(x/s, y/s, z/s);
    }
    
    public Vector3f div(float x, float y, float z)
    {
        return set(this.x/x, this.y/y, this.z/z);
    }
    
    public Vector3f div(Vector3f v)
    {
        return div(v.x, v.y, v.z);
    }
    
    public Vector3f loop(float min, float max)
    {
        return set(Util.loop(x, min, max),
                   Util.loop(y, min, max),
                   Util.loop(z, min, max));
    }

    public Vector3f avg(Vector3f v)
    {
        return add(v).mult(.5f);
    }
    
    public Vector3f lerp(Vector3f v, float t)
    {
        return set(Util.lerp(x, v.x, t),
                   Util.lerp(y, v.y, t),
                   Util.lerp(z, v.z, t));
    }
    
    public Vector3f normalize()
    {
        if (isZero(Float.MIN_NORMAL)) return this;
        return div(length());
    }

    public Vector3f setLength(float length)
    {
        if (isZero(Float.MIN_NORMAL)) return this;
        return mult(length/length());
    }
    
    public Vector3f maxLength(float length)
    {
        if (length < 0f) throw new IllegalArgumentException();
        if (isZero(Float.MIN_NORMAL)) return this;
        
        float curLength = length();
        return length > curLength ? mult(length/curLength) : this;
    }
    
    public Vector3f setDist(Vector3f v, float dist)
    {
        return sub(v).setLength(dist).add(v);
    }
    
    public Vector3f setChebyLength(float length)
    {
        if (isZero(Float.MIN_NORMAL)) return this;
        return mult(length/chebyLength());
    }
    
    public Vector3f setChebyDist(Vector3f v, float dist)
    {
        return sub(v).setChebyLength(dist).add(v);
    }
    
    /**
     * Sets this to the vector projection of this onto {@code v}.
     * 
     * @param v the vector to project onto.
     * @return this.
     */
    public Vector3f projVec(Vector3f v)
    {
        float m = dot(v)/v.squareLength();
        
        return set(v).mult(m);
    }
    
    public Vector3f reflect(Vector3f n)
    {
        final float m = 2f*dot(n);
        
        return set(m*n.x - x,
                   m*n.y - y,
                   m*n.z - z);
    }
    
    public Vector3f negate()
    {
        return set(-x, -y, -z);
    }
    
    /**
     * @return The element-wise reciprocal of this vector.
     */
    public Vector3f reciprocal()
    {
        return set(1.0f/x, 1.0f/y, 1.0f/z);
    }
    
    public Vector3f cross(Vector3f v)
    {
        return set(y*v.z - z*v.y,
                   z*v.x - x*v.z,
                   x*v.y - y*v.x);
    }
    
    public Vector3f floor()
    {
        return set(Util.floor(x),
                   Util.floor(y),
                   Util.floor(z));
    }
    
    public Vector3f ceil()
    {
        return set(Util.ceil(x),
                   Util.ceil(y),
                   Util.ceil(z));
    }
    
    public Vector3f round()
    {
        return set(Util.round(x),
                   Util.round(y),
                   Util.round(z));
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Nonlocal Mutators">
    public Vector3f cadd(float x, float y, float z)  {return copy().add(x, y, z);}
    public Vector3f cadd(Vector3f v)                 {return copy().add(v);}
    public Vector3f csub(float x, float y, float z)  {return copy().sub(x, y, z);}
    public Vector3f csub(Vector3f v)                 {return copy().sub(v);}
    public Vector3f cmult(float s)                   {return copy().mult(s);}
    public Vector3f cmult(float x, float y, float z) {return copy().mult(x, y, z);}
    public Vector3f cmult(Vector3f v)                {return copy().mult(v);}
    public Vector3f cmult(Matrix3f mat)              {return copy().mult(mat);}
    public Vector3f cmult(Matrix4f mat)              {return copy().mult(mat);}
    public Vector3f cdiv(float s)                    {return copy().div(s);}
    public Vector3f cdiv(float x, float y, float z)  {return copy().div(x, y, z);}
    public Vector3f cdiv(Vector3f v)                 {return copy().div(v);}
    
    public Vector3f cloop(float min, float max) {return copy().loop(min, max);}
    public Vector3f clerp(Vector3f v, float t)  {return copy().lerp(v, t);}
    
    public Vector3f cavg(Vector3f v) {return copy().avg(v);}
    public Vector3f cnormalize()     {return copy().normalize();}
    public Vector3f cnegate()        {return copy().negate();}
    public Vector3f creciprocal()        {return copy().reciprocal();}
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessor Methods">
    public float squareLength()
    {
        return x*x + y*y + z*z;
    }

    public float length()
    {
        return Util.sqrt(squareLength());
    }
    
    public float chebyLength()
    {
        return Util.max(Math.abs(x),
                        Math.abs(y),
                        Math.abs(z));
    }
    
    public float squareDist(Vector3f v)
    {
        if (v == null) throw new IllegalArgumentException();
        
        final float dx = x - v.x,
                    dy = y - v.y,
                    dz = z - v.z;
        
        return dx*dx + dy*dy + dz*dz;
    }

    public float dist(Vector3f v)
    {
        return Util.sqrt(squareDist(v));
    }
    
    public float chebyDist(Vector3f v)
    {
        return Util.max(Math.abs(v.x - x),
                        Math.abs(v.y - y),
                        Math.abs(v.z - z));
    }
    
    public float dot(Vector3f v)
    {
        return x*v.x + y*v.y + z*v.z;
    }
    
    /**
     * Returns the scalar projection of this onto {@code v}.
     * 
     * @param v the vector to project onto.
     * @return the scalar projection of this onto {@code v}.
     */
    public float projScal(Vector3f v)
    {
        return dot(v)/v.length();
    }
    
    public boolean isZero(float threshold)
    {
        return Util.isZero(x, threshold) &&
               Util.isZero(y, threshold) &&
               Util.isZero(z, threshold);
    }
    
    public boolean isFinite()
    {
        return Float.isFinite(x) &&
               Float.isFinite(y) &&
               Float.isFinite(z);
    }
    
    public boolean epsEqual(Vector3f v, int tolerance)
    {
        return Util.epsEqual(v.x, x, tolerance) &&
               Util.epsEqual(v.y, y, tolerance) &&
               Util.epsEqual(v.z, z, tolerance);
    }
    
    public Quat4f rotationTo(Vector3f v) //Source: [0]
    {
        if (isZero(1e-6f) || v.isZero(1e-6f)) throw new IllegalArgumentException();
        
        Vector3f v0 = copy().normalize();
        Vector3f v1 = v.copy().normalize();

        final float dot = v0.dot(v1);
        //this and v have same direction.
        if (Util.epsEqual(dot, 1f, 1<<6)) return new Quat4f();
        
        //this and v have opposite direction. Rotate 180 degrees about an
        //arbitrary axis normal to this.
        if (Util.epsEqual(dot, -1f, 1<<6))
        {
            Vector3f axis = Axis.X.versor().cross(v0);
            //this lies along X axis and v is our opposite, so we can optimize.
            if (axis.isZero(1e-6f)) return new Quat4f(0f, 0f, 0f, -1f);
            
            return Quat4f.axisAngle(axis, 180);
        }
        
        v0.cross(v1);
        final float s = Util.sqrt(2f + dot*2f);
        return new Quat4f(.5f*s, v0.x/s, v0.y/s, v0.z/s).normalize();
    }
    
    public void glVertex()
    {
        GL11.glVertex3f(x, y, z);
    }
    
    public void glTranslate()
    {
        GL11.glTranslatef(x, y, z);
    }
    
    @Override
    public Vector3f copy()
    {
        return new Vector3f(this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Object Methods">
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != Vector3f.class) return false;
        return equals((Vector3f)obj);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 43 * hash + Float.floatToIntBits(x);
        hash = 43 * hash + Float.floatToIntBits(y);
        hash = 43 * hash + Float.floatToIntBits(z);
        return hash;
    }

    @Override
    public String toString()
    {
        return "("+x+", "+y+", "+z+")";
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Bufferable Methods">
    @Override
    public void putIn(FloatBuffer buf)
    {
        buf.put(x);
        buf.put(y);
        buf.put(z);
    }
    
    @Override
    public int size()
    {
        return 3;
    }
    // </editor-fold>
}
