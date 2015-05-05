package com.samrj.devil.math;

import com.samrj.devil.buffer.Bufferable;
import static com.samrj.devil.buffer.PublicBuffers.fbuffer;
import com.samrj.devil.math.Util.Axis;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Matrix3f implements Bufferable<FloatBuffer>, Matrix<Matrix3f>
{
    // <editor-fold defaultstate="collapsed" desc="Factory Methods">
    public static Matrix3f identity()
    {
        return new Matrix3f();
    }
    
    public static Matrix3f translate(float x, float y)
    {
        return new Matrix3f(1, 0, x,
                            0, 1, y,
                            0, 0, 1);
    }
    
    public static Matrix3f translate(Vector2f v)
    {
        return translate(v.x, v.y);
    }
    
    public static Matrix3f scale(float x, float y, float z)
    {
        return new Matrix3f(x, 0, 0,
                            0, y, 0,
                            0, 0, z);
    }
    
    public static Matrix3f scale(float x, float y)
    {
        return scale(x, y, 1);
    }
    
    public static Matrix3f rotate(Axis axis, float angle)
    {
        final float sin = Util.sin(angle);
        final float nsn = -sin;
        final float cos = Util.cos(angle);
        
        switch (axis)
        {
            case X: return new Matrix3f(1,   0,   0,
                                        0, cos, nsn,
                                        0, sin, cos);
                
            case Y: return new Matrix3f(cos, 0, sin,
                                        0,   1,   0,
                                        nsn, 0, cos);
                
            case Z: return new Matrix3f(cos, nsn, 0,
                                        sin, cos, 0,
                                        0,     0, 1);
            
            default: throw new IllegalArgumentException();
        }
    }
    
    public static Matrix3f ortho(float l, float r, float b, float t)
    {
        float rml = r-l;
        float tmb = t-b;
        return new Matrix3f(2f/rml, 0, -(r+l)/rml,
                            0, 2f/tmb, -(t+b)/tmb,
                            0, 0, 1);
    }
    // </editor-fold>
    
    public float a, b, c,
                 d, e, f,
                 g, h, i;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Matrix3f(float a, float b, float c,
                    float d, float e, float f,
                    float g, float h, float i)
    {
        set(a, b, c,
            d, e, f,
            g, h, i);
    }
    
    public Matrix3f(Matrix3f z)
    {
        set(z);
    }
    
    public Matrix3f()
    {
        set();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutator Methods">
    public Matrix3f set(float a, float b, float c,
                        float d, float e, float f,
                        float g, float h, float i)
    {
        this.a = a; this.b = b; this.c = c;
        this.d = d; this.e = e; this.f = f;
        this.g = g; this.h = h; this.i = i;
        return this;
    }
    
    @Override
    public Matrix3f set(Matrix3f z)
    {
        return set(z.a, z.b, z.c,
                   z.d, z.e, z.f,
                   z.g, z.h, z.i);
    }
    
    @Override
    public Matrix3f set()
    {
        return set(1f, 0f, 0f,
                   0f, 1f, 0f,
                   0f, 0f, 1f);
    }
    
    @Override
    public Matrix3f mult(Matrix3f z)
    {
        return set(a*z.a + b*z.d + c*z.g,
                   a*z.b + b*z.e + c*z.h,
                   a*z.c + b*z.f + c*z.i,
                   
                   d*z.a + e*z.d + f*z.g,
                   d*z.b + e*z.e + f*z.h,
                   d*z.c + e*z.f + f*z.i,
                   
                   g*z.a + h*z.d + i*z.g,
                   g*z.b + h*z.e + i*z.h,
                   g*z.c + h*z.f + i*z.i);
    }
    
    public Matrix3f multTranslate(float x, float y)
    {
        return set(a, b, a*x + b*y + c,
                   d, e, d*x + e*y + f,
                   g, h, g*x + h*y + i);
    }
    
    public Matrix3f multTranslate(Vector2f v)
    {
        return multTranslate(v.x, v.y);
    }
    
    public Matrix3f multScale(float x, float y, float z)
    {
        return set(a*x, b*y, c*z,
                   d*x, e*y, f*z,
                   g*x, h*y, i*z);
    }
    
    public Matrix3f multScale(float x, float y)
    {
        return set(a*x, b*y, c,
                   d*x, e*y, f,
                   g*x, h*y, i);
    }
    
    public Matrix3f multRotate(Axis axis, float angle)
    {
        final float sin = Util.sin(angle);
        final float nsn = -sin;
        final float cos = Util.cos(angle);
        
        switch (axis)
        {
            case X: return set(a, b*cos + c*sin, b*nsn + c*cos,
                               d, e*cos + f*sin, e*nsn + f*cos,
                               g, h*cos + i*sin, h*nsn + i*cos);
                
            case Y: return set(a*cos + c*nsn, b, a*sin + c*cos,
                               d*cos + f*nsn, e, d*sin + f*cos,
                               g*cos + i*nsn, h, g*sin + i*cos);
                
            case Z: return set(a*cos + b*sin, a*nsn + b*cos, c,
                               d*cos + e*sin, d*nsn + e*cos, f,
                               g*cos + h*sin, g*nsn + h*cos, i);
            
            default: throw new IllegalArgumentException();
        }
    }
    
    @Override
    public Matrix3f mult(float s)
    {
        return set(a*s, b*s, c*s,
                   d*s, e*s, f*s,
                   g*s, h*s, i*s);
    }
    
    @Override
    public Matrix3f div(float s)
    {
        return set(a/s, b/s, c/s,
                   d/s, e/s, f/s,
                   g/s, h/s, i/s);
    }
    
    @Override
    public Matrix3f invert()
    {
        final float na = e*i - f*h;
        final float nd = f*g - d*i;
        final float ng = d*h - e*g;
        
        final float det = a*na + b*nd + c*ng;
        
        if (det == 0f) throw new SingularMatrixException();
        
        return set(na, c*h - b*i, b*f - c*e,
                   nd, a*i - c*g, c*d - a*f,
                   ng, g*b - a*h, a*e - b*d).div(det);
    }
    
    @Override
    public Matrix3f transpose()
    {
        return set(a, d, g,
                   b, e, h,
                   c, f, i);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessor Methods">
    @Override
    public float determinant()
    {
        return a*(e*i - f*h) +
               b*(f*g - d*i) +
               c*(d*h - e*g);
    }
    
    public Matrix2f toMatrix2f()
    {
        return new Matrix2f(a, b,
                            d, e);
    }
    
    public Matrix4f toMatrix4f()
    {
        return new Matrix4f(a, b, c, 0,
                            d, e, f, 0,
                            g, h, i, 0,
                            0, 0, 0, 1);
    }
    
    public Quat4f toQuat4f()
    {
        float s;
        final float tr = a + e + i;
        
        if (tr >= 0.0)
        {
            s = Util.sqrt(1f + tr);
            final float w = s/2f;
            s = .5f/s;
            return new Quat4f(w, (h - f)*s, (c - g)*s, (d - b)*s);
        }
        
        switch (Util.indexMax(a, e, i))
        {
            case 0: //a
                s = Util.sqrt(1f + a - (e + i));
                final float x = s/2f;
                s = .5f/s;
                return new Quat4f((h - f)*s, x, (b + d)*s, (g + c)*s);
                
            case 1: //e
                s = Util.sqrt(1f + e - (a + i));
                final float y = s/2f;
                s = .5f/s;
                return new Quat4f((c - g)*s, (b + d)*s, y, (f + h)*s);
                
            case 2: //i
                s = Util.sqrt(1f + i - (a + e));
                final float z = s/2f;
                s = .5f/s;
                return new Quat4f((d - b)*s, (g + c)*s, (f + h)*s, z);
                
            default: throw new RuntimeException(// <editor-fold defaultstate="collapsed" desc="Why">
                  "This error is impossible. If you get this error, you do not exist in reality.\n"
                + "Remain calm. Natural law no longer applies.\n"
                + "Really, though. Please send an error report to the dev.\n"
                + "This won't be much consolation, but I thought this was impossible.\n"
                + "So, congratulations on being the first to cause an error that cannot be caused, by definition.\n"
                + "I'll bet you hacked the program to make it happen. Cheater.\n"
                + "Yeah, don't even bother sending that error report.\n"
                + "I don't wanna hear anything from a dirty cheater like you.\n"
                + "...\n"
                + "I DIDN'T MEAN IT. PLEASE DON'T LEAVE ME.\n"
                + "I LOVE YOU. PLEASE SEND AN ERROR REPORT.\n"
                + "\n"
                + "...Okay. Back to work.");// </editor-fold>
        }
    }
    
    public void glLoad(int mode)
    {
        GL11.glMatrixMode(mode);
        fbuffer.clear();
        toMatrix4f().putIn(fbuffer);
        fbuffer.rewind();
        GL11.glLoadMatrixf(fbuffer);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Object Methods">
    @Override
    public Matrix3f clone()
    {
        return new Matrix3f(this);
    }
    
    @Override
    public String toString()
    {
        return "[" + a + ", " + b + ", " + c + "]\n" +
               "[" + d + ", " + e + ", " + f + "]\n" +
               "[" + g + ", " + h + ", " + i + "]";
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Bufferable Methods">
    @Override
    public void putIn(FloatBuffer buf)
    {
        buf.put(a); buf.put(d); buf.put(g);
        buf.put(b); buf.put(e); buf.put(h);
        buf.put(c); buf.put(f); buf.put(i);
    }
    
    @Override
    public int size()
    {
        return 9;
    }
    // </editor-fold>
}
