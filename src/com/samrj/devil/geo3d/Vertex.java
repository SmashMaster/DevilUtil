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

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;

/**
 * @author Samuel Johnson (SmashMaster)
 */
public class Vertex extends Vec3 implements Shape
{
    /**
     * Creates a new zero point.
     */
    public Vertex()
    {
    }
    
    /**
     * Creates a new point with the given coordinates.
     */
    public Vertex(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
    }
    
    /**
     * Creates a point at the given vector.
     * 
     * @param v The vector to copy.
     */
    public Vertex(Vec3 v)
    {
        x = v.x; y = v.y; z = v.z;
    }
    
    @Override
    public VertexContact collide(SweptEllipsoid swEll)
    {
        Vec3 p0 = Vec3.div(swEll.p0, swEll.radius);
        Vec3 p1 = Vec3.div(swEll.p1, swEll.radius);
        Vec3 cDir = Vec3.sub(p1, p0);
        float cSqLen = cDir.squareLength();
        
        Vec3 a = Vec3.div(this, swEll.radius);
        Vec3 dir = Vec3.div(swEll.p0, swEll.radius).sub(a);

        float t = Geometry.solveQuadratic(cSqLen,
                                          2.0f*cDir.dot(dir),
                                          dir.squareLength() - 1.0f);

        if (Float.isNaN(t)) return null; //We miss the vertex.
        if (t != t || t<= 0.0f || (swEll.terminated && t >= 1.0f))
            return null; //Moving away, won't get there in time, or NaN.
        
        Vec3 cp = Vec3.lerp(swEll.p0, swEll.p1, t);
        float dist = swEll.p0.dist(p1)*t;
        Vec3 n = Vec3.sub(cp, this).normalize();
        
        return new VertexContact(t, dist, cp, n);
    }

    @Override
    public VertexIntersection collide(Ellipsoid ell)
    {
        Vec3 dir = Vec3.sub(ell.pos, this).div(ell.radius);
        float sqDist = dir.squareLength();
        if (sqDist != sqDist || sqDist > 1.0f) return null; //Too far apart or NaN.
        
        Vec3 n = Vec3.div(dir, (float)Math.sqrt(sqDist)).mult(ell.radius);
        float nLen = n.length();
        float depth = nLen - Vec3.dist(ell.pos, this);
        n.div(nLen);
        
        return new VertexIntersection(depth, n);
    }

    @Override
    public VertexContact collide(Ray ray)
    {
        return null;
    }

    @Override
    public Intersection collide(Cylinder cyl)
    {
        Vec3 dir = Vec3.sub(cyl.pos, this);
        float yd = Math.abs(dir.y);
        if (yd > cyl.halfHeight) return null; //Too far above/below.
        
        float hdsq = dir.x*dir.x + dir.y*dir.y;
        if (hdsq > cyl.rsq) return null; //Too far horizontally.
        
        float hd = (float)Math.sqrt(hdsq);
        float hPen = cyl.radius - hd;
        float yPen = cyl.halfHeight - yd;
        
        return hPen < yPen ? //Choose between side and top/bottom
            new VertexIntersection(hPen, new Vec3(dir.x/hd, 0.0f, dir.z/hd)) :
            new VertexIntersection(yPen, new Vec3(0.0f, Util.signum(dir.y), 0.0f));
    }

    /**
     * Contact class for vertices.
     */
    public final class VertexContact extends Contact
    {
        private VertexContact(float t, float d, Vec3 cp, Vec3 n)
        {
            super(t, d, cp, Vertex.this, n);
        }
        
        @Override
        public Vertex shape()
        {
            return Vertex.this;
        }

        @Override
        public Type type()
        {
            return Type.VERTEX;
        }
    }
    
    /**
     * Intersection class for vertices.
     */
    public final class VertexIntersection extends Intersection
    {
        VertexIntersection(float d, Vec3 n)
        {
            super(d, Vertex.this, n);
        }
        
        @Override
        public Vertex shape()
        {
            return Vertex.this;
        }
        
        @Override
        public Type type()
        {
            return Type.VERTEX;
        }
    }
}
