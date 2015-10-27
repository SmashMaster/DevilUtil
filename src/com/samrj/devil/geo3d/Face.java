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

import com.samrj.devil.math.Vec3;

/**
 * @author Samuel Johnson (SmashMaster)
 */
public class Face implements Shape
{
    private static float sweepSpherePlane(Vec3 p0, Vec3 v, Vec3 n, Vec3 a, float r)
    {
        float dist = n.dot(Vec3.sub(p0, a));
        if (dist < 0.0f)
        {
            dist = -dist;
            n.negate();
        }
        
        if (dist <= r) return 0.0f;
        return (r - dist)/n.dot(v);
    }
    
    private static Vec3 barycentric(Vec3 a, Vec3 b, Vec3 c, Vec3 p)
    {
        Vec3 v0 = Vec3.sub(b, a), v1 = Vec3.sub(c, a), v2 = Vec3.sub(p, a);
        float d00 = v0.dot(v0);
        float d01 = v0.dot(v1);
        float d11 = v1.dot(v1);
        float d20 = v2.dot(v0);
        float d21 = v2.dot(v1);
        float denom = d00*d11 - d01*d01;
        
        Vec3 coords = new Vec3();
        coords.y = (d11*d20 - d01*d21)/denom;
        coords.z = (d00*d21 - d01*d20)/denom;
        coords.x = 1.0f - coords.y - coords.z;
        return coords;
    }
    
    public Vertex a, b, c;
    public Edge ab, bc, ca;
    
    public Face()
    {
    }
    
    public Face(Vertex a, Vertex b, Vertex c)
    {
        this.a = a; this.b = b; this.c = c;
        ab = new Edge(a, b);
        bc = new Edge(b, c);
        ca = new Edge(c, a);
    }
    
    @Override
    public FaceContact collide(Ray ray)
    {
        Vec3 qp = Vec3.sub(ray.p0, ray.p1);
        Vec3 ab = Vec3.sub(b, a);
        Vec3 ac = Vec3.sub(c, a);

        Vec3 n = Vec3.cross(ab, ac);
        float d = qp.dot(n);
        if (d == 0.0f) return null;
        boolean backface = d < 0.0f;
        if (backface)
        {
            d = -d;
            n.negate();
        }

        float ood = 1.0f/d;
        Vec3 ap = Vec3.sub(ray.p0, a);
        float t = ap.dot(n)*ood;
        if (t != t || t < 0.0f) return null; //Pointing away, too far, or NaN.
        if (ray.terminated && t > 1.0f) return null;

        Vec3 e = backface ? Vec3.cross(ap, qp) : Vec3.cross(qp, ap);
        float v = ac.dot(e);
        if (v < 0.0f || v > d) return null; //Missed
        float w = -ab.dot(e);
        if (w < 0.0f || v + w > d) return null; //Missed

        v = v*ood;
        w = w*ood;
        float u = 1.0f - v - w;
        
        float dist = t*qp.length();
        Vec3 p = Vec3.mult(a, u).madd(b, v).madd(c, w);
        n.normalize();
        Vec3 bary = new Vec3(u, v, w);
        return new FaceContact(t, dist, p, p, n, bary);
    }
    
    @Override
    public FaceContact collide(SweptEllipsoid swEll)
    {
        Vec3 p0 = Vec3.div(swEll.p0, swEll.radius);
        Vec3 p1 = Vec3.div(swEll.p1, swEll.radius);
        Vec3 cDir = Vec3.sub(p1, p0);
        
        Vec3 ae = Vec3.div(a, swEll.radius);
        Vec3 be = Vec3.div(b, swEll.radius);
        Vec3 ce = Vec3.div(c, swEll.radius);
        
        Vec3 normal = Vec3.sub(ce, ae).cross(Vec3.sub(be, ae)).normalize(); //Plane normal
        float t = sweepSpherePlane(p0, cDir, normal, ae, 1.0f); //Time of contact
        if (t != t || t <= 0.0f || (swEll.terminated && t >= 1.0f))
            return null; //Moving away, won't get there in time, or NaN.
        
        Vec3 cp = Vec3.lerp(swEll.p0, swEll.p1, t);
        Vec3 bary = barycentric(a, b, c, cp);
        if (bary.y < 0.0f || bary.z < 0.0f || (bary.y + bary.z) > 1.0f) return null; //We will miss the face.
        
        float dist = swEll.p0.dist(swEll.p1)*t;
        Vec3 p = Vec3.mult(a, bary.x).madd(b, bary.y).madd(c, bary.z);
        Vec3 n = Vec3.sub(cp, p).normalize();
        
        return new FaceContact(t, dist, cp, p, n, bary);
    }
    
    @Override
    public FaceIntersection collide(Ellipsoid ell)
    {
        Vec3 p = Vec3.div(ell.pos, ell.radius);
        Vec3 ae = Vec3.div(a, ell.radius);
        Vec3 be = Vec3.div(b, ell.radius);
        Vec3 ce = Vec3.div(c, ell.radius);
        
        Vec3 normal = Vec3.sub(ce, ae).cross(Vec3.sub(be, ae)).normalize();
        float dist = normal.dot(Vec3.sub(p, ae));
        if (dist < 0.0f)
        {
            dist = -dist;
            normal.negate();
        }
        if (dist != dist || dist > 1.0f) return null; //Too far apart or NaN
        
        Vec3 bary = barycentric(ae, be, ce, p);
        if (bary.y < 0.0f || bary.z < 0.0f || (bary.y + bary.z) > 1.0f) return null; //Not touching face.
        
        Vec3 cp = Vec3.mult(a, bary.x).madd(b, bary.y).madd(c, bary.z);
        normal.mult(ell.radius);
        float nLen = normal.length();
        float depth = nLen - Vec3.dist(ell.pos, cp);
        normal.div(nLen);
        
        return new FaceIntersection(depth, cp, normal, bary);
    }

    @Override
    public Intersection collide(Cylinder cyl)
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Contact class for faces.
     */
    public final class FaceContact extends Contact
    {
        /**
         * The contact barycentric coordinates.
         */
        public final Vec3 bary;

        FaceContact(float t, float d, Vec3 cp, Vec3 p, Vec3 n, Vec3 bary)
        {
            super(t, d, cp, p, n);
            this.bary = bary;
        }

        @Override
        public Face shape()
        {
            return Face.this;
        }

        @Override
        public Type type()
        {
            return Type.FACE;
        }
    }
    
    public final class FaceIntersection extends Intersection
    {
        /**
         * The face barycentric coordinates.
         */
        public final Vec3 bary;
        
        FaceIntersection(float d, Vec3 p, Vec3 n, Vec3 bary)
        {
            super(d, p, n);
            this.bary = bary;
        }
        
        @Override
        public Face shape()
        {
            return Face.this;
        }

        @Override
        public Type type()
        {
            return Type.FACE;
        }
    }
}
