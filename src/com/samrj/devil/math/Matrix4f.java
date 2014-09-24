package com.samrj.devil.math;

import com.samrj.devil.buffer.Bufferable;
import com.samrj.devil.buffer.FloatBuffer;
import static com.samrj.devil.buffer.PublicBuffers.fbuffer;
import com.samrj.devil.math.Util.Axis;
import org.lwjgl.opengl.GL11;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Matrix4f implements Bufferable<FloatBuffer>, Matrix<Matrix4f>
{
    // <editor-fold defaultstate="collapsed" desc="Factory Methods">
    public static Matrix4f identity()
    {
        return new Matrix4f();
    }
    
    /**
     * Returns a 4x4 orthogonal translation matrix from the specified
     * coordinates.
     * 
     * @param x how much to translate on the X axis.
     * @param y how much to translate on the Y axis.
     * @param z how much to translate on the Z axis.
     * @return a translation matrix.
     */
    public static Matrix4f translate(float x, float y, float z)
    {
        return new Matrix4f(1, 0, 0, x,
                            0, 1, 0, y,
                            0, 0, 1, z,
                            0, 0, 0, 1);
    }
    
    /**
     * Returns a 4x4 orthogonal translation matrix from the specified vector.
     * 
     * @param v the translation vector.
     * @return a translation matrix.
     */
    public static Matrix4f translate(Vector3f v)
    {
        return translate(v.x, v.y, v.z);
    }
    
    /**
     * Returns a 4x4 scaling matrix from the specified values.
     * 
     * @param x how much to scale on the X axis.
     * @param y how much to scale on the Y axis.
     * @param z how much to scale on the Z axis.
     * @return a scaling matrix.
     */
    public static Matrix4f scale(float x, float y, float z)
    {
        return new Matrix4f(x, 0, 0, 0,
                            0, y, 0, 0,
                            0, 0, z, 0,
                            0, 0, 0, 1);
    }
    
    /**
     * Returns a 4x4 rotation matrix from the specified axis and angle.
     * 
     * @param axis  the axis on which to rotate.
     * @param angle how much to rotate by, in degrees.
     * @return a rotation matrix.
     */
    public static Matrix4f rotate(Axis axis, float angle)
    {
        final float sin = Util.sin(angle);
        final float nsn = -sin;
        final float cos = Util.cos(angle);
        
        switch (axis)
        {
            case X: return new Matrix4f(1,   0,   0, 0,
                                        0, cos, nsn, 0,
                                        0, sin, cos, 0,
                                        0,   0,   0, 1);
                
            case Y: return new Matrix4f(cos, 0, sin, 0,
                                        0,   1,   0, 0,
                                        nsn, 0, cos, 0,
                                        0,   0,   0, 1);
                
            case Z: return new Matrix4f(cos, nsn, 0, 0,
                                        sin, cos, 0, 0,
                                        0,     0, 1, 0,
                                        0,     0, 0, 1);
            
            default: throw new IllegalArgumentException();
        }
    }
    
    /*
     * These projection matrices are based on the tutorial at:
     * http://www.songho.ca/opengl/gl_projectionmatrix.html
     * 
     * For more information on how they are used, take a look at:
     * http://www.songho.ca/opengl/gl_transform.html
     */
    
    /**
     * Returns a symmetric frustum projection matrix from the specified values.
     * Equivalent to calling {@code frustum(-w, w, -h, h, n, f)} but more
     * computationally efficient.
     * 
     * @param w the half-width of the frustum.
     * @param h the half-height of the frustum.
     * @param n the near clipping plane of the frustum.
     * @param f the far clipping plane of the frustum.
     * @return a frustum projection matrix.
     */
    public static Matrix4f frustum(float w, float h, float n, float f)
    {
        final float fmn = f-n;
        final float n2 = 2f*n;
        return new Matrix4f(n/w,      0,          0,           0,
                            0,      n/h,          0,           0,
                            0,        0, -(f+n)/fmn, (-f*n2)/fmn,
                            0,        0,         -1,           0);
    }
    
    /**
     * Returns a frustum projection matrix from the specified values.
     * 
     * @param l the left bound of the frustum.
     * @param r the right bound of the frustum.
     * @param b the lower bound of the frustum.
     * @param t the upper bound of the frustum.
     * @param n the near clipping plane of the frustum.
     * @param f the far clipping plane of the frustum.
     * @return a frustum projection matrix.
     */
    public static Matrix4f frustum(float l, float r, float b, float t, float n, float f)
    {
        final float rml = r-l;
        final float tmb = t-b;
        final float fmn = f-n;
        final float n2 = 2f*n;
        return new Matrix4f(n2/rml,      0,  (r+l)/rml,           0,
                            0,      n2/tmb,  (t+b)/tmb,           0,
                            0,           0, -(f+n)/fmn, (-f*n2)/fmn,
                            0,           0,         -1,           0);
    }
    
    /**
     * Returns a symmetric frustum projection from the specified values.
     * 
     * @param fov    the full field of view of the frustum, along its larger
     *               dimension.
     * @param aspect the aspect ratio of the frustum, height/width.
     * @param near   the near clipping plane of the frustum.
     * @param far    the far clipping plane of the frustum.
     * @return a frustum projection matrix.
     */
    public static Matrix4f perspective(float fov, float aspect, float near, float far)
    {
        if (fov <= 0f || fov >= 180f) throw new IllegalArgumentException();
        if (aspect <= 0) throw new IllegalArgumentException();
        
        float greaterDimension = Math.abs(near) * Util.tan(fov/2f);
        
        float w, h;
        if (aspect <= 1f) //Width is greater than height. (standard)
        {
            w = greaterDimension;
            h = w*aspect;
        }
        else //Widgth is smaller than height.
        {
            h = greaterDimension;
            w = h/aspect;
        }
        
        return Matrix4f.frustum(w, h, near, far);
    }
    
    /**
     * Returns a symmetric orthogonal projection matrix from the specified
     * values. Equivalent to calling {@code ortho(-w, w, -h, h, n, f)} but
     * more computationally efficient.
     * 
     * @param w the half-width of the projection box.
     * @param h the half-height of the projection box.
     * @param n the near clipping plane of the projection box.
     * @param f the far clipping plane of the projection box.
     * @return an orthogonal projection matrix.
     */
    public static Matrix4f ortho(float w, float h, float n, float f)
    {
        final float fmn = f-n;
        return new Matrix4f(1f/w, 0, 0, 0,
                            0, 1f/h, 0, 0,
                            0, 0, -2f/fmn, -(f+n)/fmn,
                            0, 0, 0, 1);
    }
    
    /**
     * Returns an orthogonal projection matrix from the specified values.
     * 
     * @param l the left bound of the projection box.
     * @param r the right bound of the projection box.
     * @param b the lower bound of the projection box.
     * @param t the upper bound of the projection box.
     * @param n the near clipping plane of the projection box.
     * @param f the far clipping plane of the projection box.
     * @return 
     */
    public static Matrix4f ortho(float l, float r, float b, float t, float n, float f)
    {
        final float rml = r-l;
        final float tmb = t-b;
        final float fmn = f-n;
        return new Matrix4f(2f/rml, 0, 0, -(r+l)/rml,
                            0, 2f/tmb, 0, -(t+b)/tmb,
                            0, 0, -2f/fmn, -(f+n)/fmn,
                            0, 0, 0, 1);
    }
    // </editor-fold>
    
    public float a, b, c, d,
                 e, f, g, h,
                 i, j, k, l,
                 m, n, o, p;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Matrix4f(float a, float b, float c, float d,
                    float e, float f, float g, float h,
                    float i, float j, float k, float l,
                    float m, float n, float o, float p)
    {
        set(a, b, c, d,
            e, f, g, h,
            i, j, k, l,
            m, n, o, p);
    }
    
    public Matrix4f(Matrix4f z)
    {
        set(z);
    }
    
    public Matrix4f()
    {
        set();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutator Methods">
    public Matrix4f set(float a, float b, float c, float d,
                        float e, float f, float g, float h,
                        float i, float j, float k, float l,
                        float m, float n, float o, float p)
    {
        this.a = a; this.b = b; this.c = c; this.d = d;
        this.e = e; this.f = f; this.g = g; this.h = h;
        this.i = i; this.j = j; this.k = k; this.l = l;
        this.m = m; this.n = n; this.o = o; this.p = p;
        return this;
    }
    
    @Override
    public Matrix4f set(Matrix4f z)
    {
        return set(z.a, z.b, z.c, z.d,
                   z.e, z.f, z.g, z.h,
                   z.i, z.j, z.k, z.l,
                   z.m, z.n, z.o, z.p);
    }
    
    @Override
    public Matrix4f set()
    {
        return set(1, 0, 0, 0,
                   0, 1, 0, 0,
                   0, 0, 1, 0,
                   0, 0, 0, 1);
    }
    
    public Matrix4f set(FloatBuffer buf)
    {
        java.nio.FloatBuffer fb = buf.get();
        set(fb.get(), fb.get(), fb.get(), fb.get(),
            fb.get(), fb.get(), fb.get(), fb.get(),
            fb.get(), fb.get(), fb.get(), fb.get(),
            fb.get(), fb.get(), fb.get(), fb.get());
        return this;
    }
    
    public Matrix4f glGet(int mode)
    {
        switch (mode)
        {
            case GL11.GL_MODELVIEW: mode = GL11.GL_MODELVIEW_MATRIX; break;
            case GL11.GL_PROJECTION: mode = GL11.GL_PROJECTION_MATRIX; break;
        }
        fbuffer.clear();
        GL11.glGetFloat(mode, fbuffer.write(16));
        return set(fbuffer).transpose();
    }
    
    @Override
    public Matrix4f mult(Matrix4f z)
    {
        return set(a*z.a + b*z.e + c*z.i + d*z.m,
                   a*z.b + b*z.f + c*z.j + d*z.n,
                   a*z.c + b*z.g + c*z.k + d*z.o,
                   a*z.d + b*z.h + c*z.l + d*z.p,
                   
                   e*z.a + f*z.e + g*z.i + h*z.m,
                   e*z.b + f*z.f + g*z.j + h*z.n,
                   e*z.c + f*z.g + g*z.k + h*z.o,
                   e*z.d + f*z.h + g*z.l + h*z.p,
                   
                   i*z.a + j*z.e + k*z.i + l*z.m,
                   i*z.b + j*z.f + k*z.j + l*z.n,
                   i*z.c + j*z.g + k*z.k + l*z.o,
                   i*z.d + j*z.h + k*z.l + l*z.p,
                   
                   m*z.a + n*z.e + o*z.i + p*z.m,
                   m*z.b + n*z.f + o*z.j + p*z.n,
                   m*z.c + n*z.g + o*z.k + p*z.o,
                   m*z.d + n*z.h + o*z.l + p*z.p);
    }
    
    public Matrix4f multTranslate(float x, float y, float z)
    {
        
        return set(a, b, c, a*x + b*y + c*z + d,
                   e, f, g, e*x + f*y + g*z + h,
                   i, j, k, i*x + j*y + k*z + l,
                   m, n, o, m*x + n*y + o*z + p);
    }
    
    public Matrix4f multTranslate(Vector3f v)
    {
        return multTranslate(v.x, v.y, v.z);
    }
    
    public Matrix4f multScale(float x, float y, float z)
    {
        return set(a*x, b*y, c*z, d,
                   e*x, f*y, g*z, h,
                   i*x, j*y, k*z, l,
                   m*x, n*y, o*z, p);
    }
    
    public Matrix4f multRotate(Axis axis, float angle)
    {
        final float sin = Util.sin(angle);
        final float nsn = -sin;
        final float cos = Util.cos(angle);
        
        switch (axis)
        {
            case X: return set(a, b*cos + c*sin, b*nsn + c*cos, d,
                               e, f*cos + g*sin, f*nsn + g*cos, h,
                               i, j*cos + k*sin, j*nsn + k*cos, l,
                               m, n*cos + o*sin, n*nsn + o*cos, p);
                
            case Y: return set(a*cos + c*nsn, b, a*sin + c*cos, d,
                               e*cos + g*nsn, f, e*sin + g*cos, h,
                               i*cos + k*nsn, j, i*sin + k*cos, l,
                               m*cos + o*nsn, n, m*sin + o*cos, p);
                
                
            case Z: return set(a*cos + b*sin, a*nsn + b*cos, c, d,
                               e*cos + f*sin, e*nsn + f*cos, g, h,
                               i*cos + j*sin, i*nsn + j*cos, k, l,
                               m*cos + n*sin, m*nsn + n*cos, o, p);
            
            default: throw new IllegalArgumentException();
        }
    }
    
    public Matrix4f multRotate(Quat4f quat)
    {
        return mult(quat.toMatrix4f());
    }
    
    @Override
    public Matrix4f mult(float s)
    {
        return set(a*s, b*s, c*s, d*s,
                   e*s, f*s, g*s, h*s,
                   i*s, j*s, k*s, l*s,
                   m*s, n*s, o*s, p*s);
    }
    
    @Override
    public Matrix4f div(float s)
    {
        return set(a/s, b/s, c/s, d/s,
                   e/s, f/s, g/s, h/s,
                   i/s, j/s, k/s, l/s,
                   m/s, n/s, o/s, p/s);
    }
    
    @Override
    public Matrix4f invert()
    {
        final float na = f*k*p + g*l*n + h*j*o - f*l*o - g*j*p - h*k*n;
        final float ne = e*l*o + g*i*p + h*k*m - e*k*p - g*l*m - h*i*o;
        final float ni = e*j*p + f*l*m + h*i*n - e*l*n - f*i*p - h*j*m;
        final float nm = e*k*n + f*i*o + g*j*m - e*j*o - f*k*m - g*i*n;
        
        final float det = a*na + b*ne + c*ni + d*nm;
        
        if (det == 0f) throw new SingularMatrixException();
        
        return set(na,
                   b*l*o + c*j*p + d*k*n - b*k*p - c*l*n - d*j*o,
                   b*g*p + c*h*n + d*f*o - b*h*o - c*f*p - d*g*n,
                   b*h*k + c*f*l + d*g*j - b*g*l - c*h*j - d*f*k,
                   ne,
                   a*k*p + c*l*m + d*i*o - a*l*o - c*i*p - d*k*m,
                   a*h*o + c*e*p + d*g*m - a*g*p - c*h*m - d*e*o,
                   a*g*l + c*h*i + d*e*k - a*h*k - c*e*l - d*g*i,
                   ni,
                   a*l*n + b*i*p + d*j*m - a*j*p - b*l*m - d*i*n,
                   a*f*p + b*h*m + d*e*n - a*h*n - b*e*p - d*f*m,
                   a*h*j + b*e*l + d*f*i - a*f*l - b*h*i - d*e*j,
                   nm,
                   a*j*o + b*k*m + c*i*n - a*k*n - b*i*o - c*j*m,
                   a*g*n + b*e*o + c*f*m - a*f*o - b*g*m - c*e*n,
                   a*f*k + b*g*i + c*e*j - a*g*j - b*e*k - c*f*i).div(det);
    }
    
    @Override
    public Matrix4f transpose()
    {
        return set(a, e, i, m,
                   b, f, j, n,
                   c, g, k, o,
                   d, h, l, p);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessor Methods">
    @Override
    public float determinant()
    {
        return a*(f*k*p + g*l*n + h*j*o - f*l*o - g*j*p - h*k*n) +
               b*(e*l*o + g*i*p + h*k*m - e*k*p - g*l*m - h*i*o) +
               c*(e*j*p + f*l*m + h*i*n - e*l*n - f*i*p - h*j*m) +
               d*(e*k*n + f*i*o + g*j*m - e*j*o - f*k*m - g*i*n);
    }
    
    public Matrix3f toMatrix3f()
    {
        return new Matrix3f(a, b, c,
                            e, f, g,
                            i, j, k);
    }
    
    public void glLoad(int mode)
    {
        GL11.glMatrixMode(mode);
        fbuffer.clear();
        clone().transpose().putIn(fbuffer);
        GL11.glLoadMatrix(fbuffer.get());
    }
    
    public void glMult(int mode)
    {
        GL11.glMatrixMode(mode);
        fbuffer.clear();
        clone().transpose().putIn(fbuffer);
        GL11.glMultMatrix(fbuffer.get());
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Object Methods">
    @Override
    public Matrix4f clone()
    {
        return new Matrix4f(this);
    }
    
    @Override
    public String toString()
    {
        return "[" + a + ", " + b + ", " + c + ", " + d + "]\n" +
               "[" + e + ", " + f + ", " + g + ", " + h + "]\n" +
               "[" + i + ", " + j + ", " + k + ", " + l + "]\n" +
               "[" + m + ", " + n + ", " + o + ", " + p + "]";
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Bufferable Methods">
    @Override
    public void putIn(FloatBuffer buf)
    {
        buf.put(a, b, c, d,
                e, f, g, h,
                i, j, k, l,
                m, n, o, p);
    }
    
    @Override
    public int size()
    {
        return 16;
    }
    // </editor-fold>
}
