package com.samrj.devil.geo3d;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;

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
    private final Vec3[] normals = new Vec3[6];
    private final float[] constants = new float[6];

    /**
     * Creates an uninitialized frustum.
     */
    public Frustum()
    {
        for (int i=0; i<6; i++) normals[i] = new Vec3();
    }

    /**
     * Creates a new frustum from the given matrix.
     *
     * @param m A view-projection matrix.
     */
    public Frustum(Mat4 m)
    {
        this();
        set(m);
    }

    /**
     * Sets this frustum to the given view-projection matrix.
     *
     * @param m A view-projection matrix.
     */
    public void set(Mat4 m)
    {
        float m11 = m.a, m12 = m.b, m13 = m.c, m14 = m.d;
        float m21 = m.e, m22 = m.f, m23 = m.g, m24 = m.h;
        float m31 = m.i, m32 = m.j, m33 = m.k, m34 = m.l;
        float m41 = m.m, m42 = m.n, m43 = m.o, m44 = m.p;

        normals[0].set(m41 + m11, m42 + m12, m43 + m13); //Left
        constants[0] = m44 + m14;
        normals[1].set(m41 - m11, m42 - m12, m43 - m13); //Right
        constants[1] = m44 - m14;
        normals[2].set(m41 + m21, m42 + m22, m43 + m23); //Bottom
        constants[2] = m44 + m24;
        normals[3].set(m41 - m21, m42 - m22, m43 - m23); //Top
        constants[3] = m44 - m24;
        normals[4].set(m41 + m31, m42 + m32, m43 + m33); //Near
        constants[4] = m44 + m34;
        normals[5].set(m41 - m31, m42 - m32, m43 - m33); //Far
        constants[5] = m44 - m34;
    }

    /**
     * Returns all 6 planes of this Frustum as Vec4s: (plane normal.xyz, plane constant)
     */
    public Vec4[] getPlanes()
    {
        return new Vec4[]{
                new Vec4(normals[0], constants[0]),
                new Vec4(normals[1], constants[1]),
                new Vec4(normals[2], constants[2]),
                new Vec4(normals[3], constants[3]),
                new Vec4(normals[4], constants[4]),
                new Vec4(normals[5], constants[5])};
    }

    /**
     * Returns true if the given box is inside, or touching the edges of this frustum.
     */
    public boolean touching(Box3 box)
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
        return inside;
    }
}
