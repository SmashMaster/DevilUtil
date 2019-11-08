package com.samrj.devil.geo3d;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;

/**
 * Allows a frustum to be derived from any 4x4 projection matrix; whether it is
 * in view or model space; and whether it is an orthographic or perspective
 * matrix. This can then be used for efficient frustum culling.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Frustum
{
    private final Vec3[] normals;
    private final float[] constants;
    
    public Frustum(Mat4 matrix)
    {
        float m11 = matrix.a, m12 = matrix.b, m13 = matrix.c, m14 = matrix.d;
        float m21 = matrix.e, m22 = matrix.f, m23 = matrix.g, m24 = matrix.h;
        float m31 = matrix.i, m32 = matrix.j, m33 = matrix.k, m34 = matrix.l;
        float m41 = matrix.m, m42 = matrix.n, m43 = matrix.o, m44 = matrix.p;
        
        Vec3 leftN = new Vec3(m41 + m11, m42 + m12, m43 + m13);
        float leftP = m44 + m14;
        Vec3 rightN = new Vec3(m41 - m11, m42 - m12, m43 - m13);
        float rightP = m44 - m14;
        Vec3 bottomN = new Vec3(m41 + m21, m42 + m22, m43 + m23);
        float bottomP = m44 + m24;
        Vec3 topN = new Vec3(m41 - m21, m42 - m22, m43 - m23);
        float topP = m44 - m24;
        Vec3 nearN = new Vec3(m41 + m31, m42 + m32, m43 + m33);
        float nearP = m44 + m34;
        Vec3 farN = new Vec3(m41 - m31, m42 - m32, m43 - m33);
        float farP = m44 - m34;
        
        normals = new Vec3[]{leftN, rightN, bottomN, topN, nearN, farN};
        constants = new float[]{leftP, rightP, bottomP, topP, nearP, farP};
    }

    /**
     * Returns true if the given box is definitely not inside this frustum. A
     * false result does not mean that the box is definitely visible.
     */
    public boolean cull(Box3 box)
    {
        boolean inside = true;
        for (int i=0; i<6; i++)
        {
            Vec3 normal = normals[i];
            float d = Math.max(box.min.x*normal.x, box.max.x*normal.x) +
                      Math.max(box.min.y*normal.y, box.max.y*normal.y) +
                      Math.max(box.min.z*normal.z, box.max.z*normal.z) + constants[i];
            inside &= d > 0;
        }
        return !inside;
    }
}
