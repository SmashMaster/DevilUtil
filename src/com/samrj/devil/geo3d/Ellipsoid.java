/*
 * Copyright (c) 2015 Sam Johnson
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

import com.samrj.devil.geo3d.GeoMesh.Edge;
import com.samrj.devil.geo3d.GeoMesh.Face;
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
    
    private IsectResult isectCenter(Object object)
    {
        IsectResult out = new IsectResult(object);
        out.point.set(pos);
        out.surface.set(pos);
        out.surface.y -= radii.y;
        out.depth = radii.y;
        out.normal.y = 1.0f;
        return out;
    }
    
    @Override
    public IsectResult isect(Vec3 p)
    {
        Vec3 dir = Vec3.sub(p, pos).div(radii);
        float sqLen = dir.squareLength();
        if (sqLen > 1.0f) return null; //Too far away.
        
        float len = (float)Math.sqrt(sqLen);
        if (Float.isNaN(len)) return null;
        if (Util.isZero(len, EPSILON)) return isectCenter(p); //Intersecting center.
        
        IsectResult out = new IsectResult(p);
        Vec3.copy(p, out.point);
        Vec3 tmp = Vec3.div(dir, len);
        Vec3.negate(tmp, out.normal);
        Vec3.mult(tmp, radii, out.surface);
        out.surface.add(pos);
        out.depth = Vec3.dist(out.point, out.surface);
        return out;
    }

    @Override
    public IsectResult isect(Edge e)
    {
        Vec3 aDir = Vec3.sub(e.a, pos).div(radii);
        Vec3 eDir = Vec3.sub(e.b, e.a).div(radii);
        
        float eLenSq = eDir.squareLength();
        float et = -aDir.dot(eDir)/eLenSq;
        if (et < 0.0f || et > 1.0f) return null; //Not touching segment.
        
        Vec3 dir = Vec3.madd(aDir, eDir, et);
        float sqLen = dir.squareLength();
        if (sqLen > 1.0f) return null; //Too far away.
        
        float len = (float)Math.sqrt(sqLen);
        if (Float.isNaN(len)) return null;
        if (Util.isZero(len, EPSILON)) return isectCenter(e); //Intersecting center.
        
        IsectResult out = new IsectResult(e);
        Vec3.lerp(e.a, e.b, et, out.point);
        Vec3 tmp = Vec3.div(dir, len);
        Vec3.negate(tmp, out.normal);
        out.normal.div(radii).normalize();
        Vec3.mult(tmp, radii, out.surface);
        out.surface.add(pos);
        out.depth = Vec3.dist(out.point, out.surface);
        return out;
    }

    @Override
    public IsectResult isect(Face f)
    {
        Vec3 aDir = Vec3.sub(f.a, pos).div(radii);
        Vec3 bDir = Vec3.sub(f.b, pos).div(radii);
        Vec3 cDir = Vec3.sub(f.c, pos).div(radii);
        
        Vec4 plane = Geo3DUtil.plane(aDir, bDir, cDir);
        if (plane.w > 0.0f) plane.negate();
        if (plane.w < -1.0f || Float.isNaN(plane.w)) return null; //Too far apart or NaN.
        
        Vec3 bary = Geo3DUtil.baryCoords(f.a, f.b, f.c, pos);
        if (!Geo3DUtil.baryContained(bary)) return null; //Not inside triangle.
        
        if (Util.isZero(plane.w, EPSILON)) return isectCenter(f); //Intersected center.
        
        IsectResult out = new IsectResult(f);
        Geo3DUtil.baryPoint(f.a, f.b, f.c, bary, out.point);
        Geo3DUtil.baryPoint(aDir, bDir, cDir, bary, out.surface);
        out.surface.div(-plane.w).mult(radii).add(pos);
        Geo3DUtil.normal(plane, out.normal);
        out.normal.div(radii).normalize();
        out.depth = Vec3.dist(out.point, out.surface);
        return out;
    }

    @Override
    public SweepResult sweep(Vec3 dp, Vec3 p)
    {
        Vec3 dpe = Vec3.div(dp, radii);
        float dpSqLen = dpe.squareLength();
        Vec3 pDir = Vec3.sub(pos, p).div(radii);
        float pSqDist = pDir.squareLength();
        
        float t = Geo3DUtil.solveQuadratic(dpSqLen,
                                           2.0f*pDir.dot(dpe),
                                           pSqDist - 1.0f);

        if (Float.isNaN(t)) return null; //Missed the vertex.
        if (t < 0.0f || t > 1.0f)
            return null; //Moving away or won't get there in time.
        
        SweepResult out = new SweepResult(p);
        out.time = t;
        Vec3.copy(p, out.point);
        Vec3.madd(pos, dp, t, out.position);
        Vec3.sub(out.position, out.point, out.normal);
        out.normal.div(radii).normalize();
        return out;
    }

    @Override
    public SweepResult sweep(Vec3 dp, Edge e)
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

        float t = Geo3DUtil.solveQuadratic(
                segDotDP*segDotDP - segSqLen*dpeLen,
                2.0f*(segSqLen*dpe.dot(aDir) - segDotDP*segDotA),
                segSqLen*(1.0f - aDir.squareLength()) + segDotA*segDotA);

        if (Float.isNaN(t)) return null; //Missed the line.
        if (t < 0.0f || t > 1.0f)  return null; //Moving away or won't get there in time.

        float et = (segDotDP*t - segDotA)/segSqLen;
        if (et < 0.0f || et > 1.0f) return null; //Hit the line but missed the segment.
        if (!Util.isFinite(et)) return null; //Degenerate segment.
        
        SweepResult out = new SweepResult(e);
        out.time = t;
        Vec3.lerp(e.a, e.b, et, out.point);
        Vec3.madd(pos, dp, t, out.position);
        Vec3.sub(out.position, out.point, out.normal);
        out.normal.div(radii).div(radii).normalize(); //wtf?
        return out;
    }

    @Override
    public SweepResult sweep(Vec3 dp, Face f)
    {
        Vec3 p0 = Vec3.div(pos, radii);
        Vec3 cDir = Vec3.div(dp, radii);
        
        Vec3 ae = Vec3.div(f.a, radii);
        Vec3 be = Vec3.div(f.b, radii);
        Vec3 ce = Vec3.div(f.c, radii);
        
        Vec4 plane = Geo3DUtil.plane(ae, be, ce);
        float t = Geo3DUtil.sweepSpherePlane(p0, cDir, plane, 1.0f);
        if (Float.isNaN(t) || t <= 0.0f || t >= 1.0f)
            return null; //Moving away or won't get there in time.
        
        Vec3 position = Vec3.madd(pos, dp, t);
        Vec3 bary = Geo3DUtil.baryCoords(f.a, f.b, f.c, position);
        if (!Geo3DUtil.baryContained(bary)) return null; //Missed the triangle.
        
        SweepResult out = new SweepResult(f);
        out.time = t;
        Geo3DUtil.baryPoint(f.a, f.b, f.c, bary, out.point);
        Vec3.copy(position, out.position);
        Vec3.sub(position, out.point, out.normal);
        out.normal.normalize();
        return out;
    }

    @Override
    public Box3 bounds()
    {
        return new Box3(Vec3.sub(pos, radii), Vec3.add(pos, radii));
    }
}
