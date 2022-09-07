/*
 * Copyright (c) 2022 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.geo3d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;

/**
 * Ellipsoid shape class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Ellipsoid implements ConvexShape
{
    private static final float EPSILON = 1.0f/65536.0f;
    
    public final Vec3 pos = new Vec3();
    public final Vec3 radii = new Vec3();
    
    private boolean isectCenter(Object object, Isect result)
    {
        result.object = object;
        result.point.set(pos);
        result.surface.set(pos);
        result.surface.y -= radii.y;
        result.depth = radii.y;
        result.normal.y = 1.0f;
        return true;
    }
    
    @Override
    public boolean isect(Vec3 v, Isect result)
    {
        Vec3 dir = Vec3.sub(v, pos).div(radii);
        float sqLen = dir.squareLength();
        if (sqLen > 1.0f) return false; //Too far away.
        
        float len = (float)Math.sqrt(sqLen);
        if (Float.isNaN(len)) return false;
        if (Util.isZero(len, EPSILON)) return isectCenter(v, result); //Intersecting center.

        result.object = v;
        Vec3.copy(v, result.point);
        Vec3 tmp = Vec3.div(dir, len);
        Vec3.negate(tmp, result.normal);
        Vec3.mult(tmp, radii, result.surface);
        result.surface.add(pos);
        result.depth = Vec3.dist(result.point, result.surface);
        return true;
    }

    @Override
    public boolean isect(Edge3 e, Isect result)
    {
        Vec3 aDir = Vec3.sub(e.a, pos).div(radii);
        Vec3 eDir = Vec3.sub(e.b, e.a).div(radii);
        
        float eLenSq = eDir.squareLength();
        float et = -aDir.dot(eDir)/eLenSq;
        if (et < 0.0f || et > 1.0f) return false; //Not touching segment.
        
        Vec3 dir = Vec3.madd(aDir, eDir, et);
        float sqLen = dir.squareLength();
        if (sqLen > 1.0f) return false; //Too far away.
        
        float len = (float)Math.sqrt(sqLen);
        if (Float.isNaN(len)) return false;
        if (Util.isZero(len, EPSILON)) return isectCenter(e, result); //Intersecting center.

        result.object = e;
        Vec3.lerp(e.a, e.b, et, result.point);
        Vec3 tmp = Vec3.div(dir, len);
        Vec3.negate(tmp, result.normal);
        result.normal.div(radii).normalize();
        Vec3.mult(tmp, radii, result.surface);
        result.surface.add(pos);
        result.depth = Vec3.dist(result.point, result.surface);
        return true;
    }

    @Override
    public boolean isect(Triangle3 f, Isect result)
    {
        Vec3 aDir = Vec3.sub(f.a, pos).div(radii);
        Vec3 bDir = Vec3.sub(f.b, pos).div(radii);
        Vec3 cDir = Vec3.sub(f.c, pos).div(radii);
        Triangle3 localTri = new Triangle3(aDir, bDir, cDir);
        Vec4 plane = Triangle3.plane(localTri);
        
        if (plane.w > 0.0f) plane.negate();
        if (plane.w < -1.0f || Float.isNaN(plane.w)) return false; //Too far apart or NaN.
        
        Vec3 bary = Triangle3.barycentric(f, pos);
        if (!Geo3D.baryContained(bary)) return false; //Not inside triangle.
        
        if (Util.isZero(plane.w, EPSILON)) return isectCenter(f, result); //Intersected center.

        result.object = f;
        Triangle3.interpolate(f, bary, result.point);
        Triangle3.interpolate(localTri, bary, result.surface);
        result.surface.div(-plane.w).mult(radii).add(pos);
        Geo3D.normal(plane, result.normal);
        result.normal.div(radii).normalize();
        result.depth = Vec3.dist(result.point, result.surface);
        return true;
    }

    @Override
    public boolean sweep(Vec3 dp, Vec3 v, Sweep result)
    {
        Vec3 dpe = Vec3.div(dp, radii);
        float dpSqLen = dpe.squareLength();
        Vec3 pDir = Vec3.sub(pos, v).div(radii);
        float pSqDist = pDir.squareLength();
        
        float t = Util.quadFormulaSmallestPositive(dpSqLen, 2.0f*pDir.dot(dpe), pSqDist - 1.0f);

        if (Float.isNaN(t)) return false; //Missed the vertex.
        if (t < 0.0f || t > 1.0f)
            return false; //Moving away or won't get there in time.

        result.object = v;
        result.time = t;
        Vec3.copy(v, result.point);
        Vec3.madd(pos, dp, t, result.position);
        Vec3.sub(result.position, result.point, result.normal);
        result.normal.div(radii).normalize();
        return true;
    }

    @Override
    public boolean sweep(Vec3 dp, Edge3 e, Sweep result)
    {
        Vec3 dpe = Vec3.div(dp, radii);
        float dpeLen = dpe.squareLength();
        
        Vec3 ae = Vec3.div(e.a, radii);
        Vec3 be = Vec3.div(e.b, radii);

        Vec3 segDir = Vec3.sub(be, ae);
        float segSqLen = segDir.squareLength();
        Vec3 aDir = Vec3.sub(e.a, pos).div(radii);

        float segDotDP = segDir.dot(dpe);
        float segDotA = segDir.dot(aDir);

        float t = Util.quadFormulaSmallestPositive(
                segDotDP*segDotDP - segSqLen*dpeLen,
                2.0f*(segSqLen*dpe.dot(aDir) - segDotDP*segDotA),
                segSqLen*(1.0f - aDir.squareLength()) + segDotA*segDotA);

        if (Float.isNaN(t)) return false; //Missed the line.
        if (t < 0.0f || t > 1.0f)  return false; //Moving away or won't get there in time.

        float et = (segDotDP*t - segDotA)/segSqLen;
        if (et < 0.0f || et > 1.0f) return false; //Hit the line but missed the segment.
        if (!Float.isFinite(et)) return false; //Degenerate segment.

        result.object = e;
        result.time = t;
        Vec3.lerp(e.a, e.b, et, result.point);
        Vec3.madd(pos, dp, t, result.position);
        Vec3.sub(result.position, result.point, result.normal);
        result.normal.div(radii).div(radii).normalize(); //wtf?
        return true;
    }

    @Override
    public boolean sweep(Vec3 dp, Triangle3 f, Sweep result)
    {
        Vec3 p0 = Vec3.div(pos, radii);
        Vec3 cDir = Vec3.div(dp, radii);
        
        Vec3 ae = Vec3.div(f.a, radii);
        Vec3 be = Vec3.div(f.b, radii);
        Vec3 ce = Vec3.div(f.c, radii);
        Triangle3 localTri = new Triangle3(ae, be, ce);
        
        Vec4 plane = Triangle3.plane(localTri);
        float t = Geo3D.sweepSpherePlane(p0, cDir, plane, 1.0f);
        if (Float.isNaN(t) || t <= 0.0f || t >= 1.0f)
            return false; //Moving away or won't get there in time.
        
        Vec3 position = Vec3.madd(pos, dp, t);
        Vec3 bary = Triangle3.barycentric(f, position);
        if (!Geo3D.baryContained(bary)) return false; //Missed the triangle.

        result.object = f;
        result.time = t;
        Triangle3.interpolate(f, bary, result.point);
        Vec3.copy(position, result.position);
        Vec3.sub(position, result.point, result.normal);
        result.normal.normalize();
        return true;
    }

    @Override
    public void getBounds(Box3 result)
    {
        Vec3.sub(pos, radii, result.min);
        Vec3.add(pos, radii, result.max);
    }
}
