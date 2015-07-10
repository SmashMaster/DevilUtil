
package com.samrj.devil.math;

public class Mat4
{
    private static final float SQRT_2 = (float)Math.sqrt(2.0);
    private static final Mat4 tempMat = new Mat4();
    
    /**
     * Returns the determinant of the given matrix.
     * 
     * @param m The matrix to calculate the determinant of.
     * @return The determinant of the given matrix.
     */
    public static final float determinant(Mat4 m)
    {
        return m.a*(m.f*m.k*m.p + m.g*m.l*m.n + m.h*m.j*m.o - m.f*m.l*m.o - m.g*m.j*m.p - m.h*m.k*m.n) +
               m.b*(m.e*m.l*m.o + m.g*m.i*m.p + m.h*m.k*m.m - m.e*m.k*m.p - m.g*m.l*m.m - m.h*m.i*m.o) +
               m.c*(m.e*m.j*m.p + m.f*m.l*m.m + m.h*m.i*m.n - m.e*m.l*m.n - m.f*m.i*m.p - m.h*m.j*m.m) +
               m.d*(m.e*m.k*m.n + m.f*m.i*m.o + m.g*m.j*m.m - m.e*m.j*m.o - m.f*m.k*m.m - m.g*m.i*m.n);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies the source matrix into the target matrix. 
     * 
     * @param m The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat4 m, Mat4 r)
    {
        r.a = m.a; r.b = m.b; r.c = m.c; r.d = m.d;
        r.e = m.e; r.f = m.f; r.g = m.g; r.h = m.h;
        r.i = m.i; r.j = m.j; r.k = m.k; r.l = m.l;
        r.m = m.m; r.n = m.n; r.o = m.o; r.p = m.p;
    }
    
    /**
     * Sets the given matrix to the identity matrix.
     * 
     * @param r The matrix to set to the identity matrix.
     */
    public static final void identity(Mat4 r)
    {
        scaling(1.0f, r);
    }
    
    /**
     * Sets the given matrix to a scaling matrix by the given scalar.
     * 
     * @param s The scalar to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(float s, Mat4 r)
    {
        r.a = s; r.b = 0.0f; r.c = 0.0f; r.d = 0.0f;
        r.e = 0.0f; r.f = s; r.g = 0.0f; r.h = 0.0f;
        r.i = 0.0f; r.j = 0.0f; r.k = s; r.l = 0.0f;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Sets the given matrix to the rotation matrix, using the given {@code axis}
     * of rotation, and {@code ang} as the angle. The axis must be normalized.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotation(Vec3 axis, float angle, Mat4 r)
    {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);
        float omcos = 1.0f - cos;
        
        float xsq = axis.x*axis.x, ysq = axis.y*axis.y, zsq = axis.z*axis.z;
        
        float xyomcos = axis.x*axis.y*omcos, zsin = axis.z*sin;
        float xzomcos = axis.x*axis.z*omcos, ysin = axis.y*sin;
        float yzomcos = axis.y*axis.z*omcos, xsin = axis.x*sin;
        
        r.a = xsq + (1.0f - xsq)*cos;
        r.b = xyomcos - zsin;
        r.c = xzomcos + ysin;
        r.d = 0.0f;
        
        r.e = xyomcos + zsin;
        r.f = ysq + (1.0f - ysq)*cos;
        r.g = yzomcos - xsin;
        r.h = 0.0f;
        
        r.i = xzomcos - ysin;
        r.j = yzomcos + xsin;
        r.k = zsq + (1.0f - zsq)*cos;
        r.l = 0.0f;
        
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Sets the given matrix to a rotation matrix representation of the given
     * quaternion.
     * 
     * @param q The quaternion to represent as a matrix.
     * @param r The matrix in which to store the result.
     */
    public static final void rotation(Quat q, Mat4 r)
    {
        float q0 = SQRT_2*q.w, q1 = SQRT_2*q.x, q2 = SQRT_2*q.y, q3 = SQRT_2*q.z;

	float qda = q0*q1, qdb = q0*q2, qdc = q0*q3;
	float qaa = q1*q1, qab = q1*q2, qac = q1*q3;
	float qbb = q2*q2, qbc = q2*q3, qcc = q3*q3;
        
        r.a = 1.0f - qbb - qcc; r.b = -qdc + qab; r.c = qdb + qac; r.d = 0.0f;
        r.e = qdc + qab; r.f = 1.0f - qaa - qcc; r.g = -qda + qbc; r.h = 0.0f;
        r.i = -qdb + qac; r.j = qda + qbc; r.k = 1.0f - qaa - qbb; r.l = 0.0f;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Sets the given matrix to the translation matrix by the given vector.
     * 
     * @param v The vector to translate by.
     * @param r The matrix in which to store the result.
     */
    public static final void translation(Vec3 v, Mat4 r)
    {
        r.a = 1.0f; r.b = 0.0f; r.c = 0.0f; r.d = v.x;
        r.e = 0.0f; r.f = 1.0f; r.g = 0.0f; r.h = v.y;
        r.i = 0.0f; r.j = 0.0f; r.k = 1.0f; r.l = v.z;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Rotates {@code m} about the given {@code axis} by the given angle
     * {@code ang} and stores the result in {@code r}. The axis must be
     * normalized.
     * 
     * @param m The matrix to rotate.
     * @param axis The axis to rotate around.
     * @param angle The angle to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotate(Mat4 m, Vec3 axis, float angle, Mat4 r)
    {
        rotation(axis, angle, tempMat); //Could probably be improved.
        mult(m, tempMat, r);
    }
    
    /**
     * Rotates {@code m} by the given quaternion and stores the result in {@code r}.
     * 
     * @param m The matrix to rotate.
     * @param q The quaternion to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotate(Mat4 m, Quat q, Mat4 r)
    {
        rotation(q, tempMat); //Could probably be improved, as above
        mult(m, tempMat, r);
    }
    
    /**
     * Translates {@code m} by the given vector {@code v}, and stores the result
     * in {@code r}.
     * 
     * @param m The matrix to translate.
     * @param v The vector to translate by.
     * @param r The matrix in which to store the result.
     */
    public static final void translate(Mat4 m, Vec3 v, Mat4 r)
    {
        r.a = m.a; r.b = m.b; r.c = m.c; r.d = m.a*v.x + m.b*v.y + m.c*v.z + m.d;
        r.e = m.e; r.f = m.f; r.g = m.g; r.h = m.e*v.x + m.f*v.y + m.g*v.z + m.h;
        r.i = m.i; r.j = m.j; r.k = m.k; r.l = m.i*v.x + m.j*v.y + m.k*v.z + m.l;
        r.m = m.m; r.n = m.n; r.o = m.o; r.p = m.m*v.x + m.n*v.y + m.o*v.z + m.p;
    }
    
    /**
     * Performs a matrix multiplication on {@code m0} and {@code m1}, and stores
     * the result in {@code r}. 
     * 
     * @param m0 The left-hand matrix to multiply.
     * @param m1 The right-hand matrix to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat4 m0, Mat4 m1, Mat4 r)
    {
        float a = m0.a*m1.a + m0.b*m1.e + m0.c*m1.i + m0.d*m1.m;
        float b = m0.a*m1.b + m0.b*m1.f + m0.c*m1.j + m0.d*m1.n;
        float c = m0.a*m1.c + m0.b*m1.g + m0.c*m1.k + m0.d*m1.o;
        float d = m0.a*m1.d + m0.b*m1.h + m0.c*m1.l + m0.d*m1.p;
        
        float e = m0.e*m1.a + m0.f*m1.e + m0.g*m1.i + m0.h*m1.m;
        float f = m0.e*m1.b + m0.f*m1.f + m0.g*m1.j + m0.h*m1.n;
        float g = m0.e*m1.c + m0.f*m1.g + m0.g*m1.k + m0.h*m1.o;
        float h = m0.e*m1.d + m0.f*m1.h + m0.g*m1.l + m0.h*m1.p;
        
        float i = m0.i*m1.a + m0.j*m1.e + m0.k*m1.i + m0.l*m1.m;
        float j = m0.i*m1.b + m0.j*m1.f + m0.k*m1.j + m0.l*m1.n;
        float k = m0.i*m1.c + m0.j*m1.g + m0.k*m1.k + m0.l*m1.o;
        float l = m0.i*m1.d + m0.j*m1.h + m0.k*m1.l + m0.l*m1.p;
        
        float m = m0.m*m1.a + m0.n*m1.e + m0.o*m1.i + m0.p*m1.m;
        float n = m0.m*m1.b + m0.n*m1.f + m0.o*m1.j + m0.p*m1.n;
        float o = m0.m*m1.c + m0.n*m1.g + m0.o*m1.k + m0.p*m1.o;
        float p = m0.m*m1.d + m0.n*m1.h + m0.o*m1.l + m0.p*m1.p;
        
        r.a = a; r.b = b; r.c = c; r.d = d;
        r.e = e; r.f = f; r.g = g; r.h = h;
        r.i = i; r.j = j; r.k = k; r.l = l;
        r.m = m; r.n = n; r.o = o; r.p = p;
    }
    
    /**
     * Multiplies each entry in the given matrix by the given scalar.
     * 
     * @param m The matrix to multiply.
     * @param s The scalar to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat4 m, float s, Mat4 r)
    {
        r.a = m.a*s; r.b = m.b*s; r.c = m.c*s; r.d = m.d*s;
        r.e = m.e*s; r.f = m.f*s; r.g = m.g*s; r.h = m.h*s;
        r.i = m.i*s; r.j = m.j*s; r.k = m.k*s; r.l = m.l*s;
        r.m = m.m*s; r.n = m.n*s; r.o = m.o*s; r.p = m.p*s;
    }
    
    /**
     * Divides the given matrix by the given scalar.
     * 
     * @param m The matrix to divide.
     * @param s The scalar to divide by.
     * @param r The matrix in which to store the result.
     */
    public static final void div(Mat4 m, float s, Mat4 r)
    {
        r.a = m.a/s; r.b = m.b/s; r.c = m.c/s; r.d = m.d/s;
        r.e = m.e/s; r.f = m.f/s; r.g = m.g/s; r.h = m.h/s;
        r.i = m.i/s; r.j = m.j/s; r.k = m.k/s; r.l = m.l/s;
        r.m = m.m/s; r.n = m.n/s; r.o = m.o/s; r.p = m.p/s;
    }
    
    /**
     * Sets {@code r} to the transpose of {@code m}.
     * 
     * @param m The matrix to compute the transpose of.
     * @param r The matrix in which to store the result.
     */
    public static final void transpose(Mat4 m, Mat4 r)
    {
        float tb = m.b, tc = m.c, td = m.d;
        float tg = m.g, th = m.h;
        float tl = m.l;
        r.a = m.a; r.b = m.e; r.c = m.i; r.d = m.m;
        r.e = tb;  r.f = m.f; r.g = m.j; r.h = m.n;
        r.i = tc;  r.j = tg;  r.k = m.k; r.l = m.o;
        r.m = td;  r.n = th;  r.o = tl;  r.p = m.p;
    }
    
    /**
     * Calculates the inverse of {@code m} and stores the result in {@code r}.
     * 
     * @param m The matrix to compute the inverse of.
     * @param r The matrix in which to store the result.
     * @throws com.samrj.devil.math.SingularMatrixException If {@code m} is
     *         a singular matrix. (Its determinant is zero.)
     */
    public static final void invert(Mat4 m, Mat4 r)
    {
        float a = m.e*m.i - m.f*m.h;
        float d = m.f*m.g - m.d*m.i;
        float g = m.d*m.h - m.e*m.g;
        
        float det = m.a*a + m.b*d + m.c*g;
        if (det == 0.0f) throw new SingularMatrixException();
        
        float b = m.c*m.h - m.b*m.i, c = m.b*m.f - m.c*m.e;
        float e = m.a*m.i - m.c*m.g, f = m.c*m.d - m.a*m.f;
        float h = m.g*m.b - m.a*m.h, i = m.a*m.e - m.b*m.d;
        
        r.a = a/det; r.b = b/det; r.c = c/det;
        r.d = d/det; r.e = e/det; r.f = f/det;
        r.g = g/det; r.h = h/det; r.i = i/det;
    }
    // </editor-fold>
    
    public float a, b, c, d,
                 e, f, g, h,
                 i, j, k, l,
                 m, n, o, p;
}
