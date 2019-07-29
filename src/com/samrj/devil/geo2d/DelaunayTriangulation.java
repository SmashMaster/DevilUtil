/*
 * Copyright (c) 2019 Sam Johnson
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

package com.samrj.devil.geo2d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import java.util.*;

/**
 * Creates a triangulation of the given 2D point set. Uses a sweep-line
 * algorithm. Probably has O(n log n) time complexity? Seems to handle most
 * degenerate cases and large point sets (100,000+) easily. Has problems when
 * points are very close to eachother.
 * 
 * Will throw an exception if duplicate points are given.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class DelaunayTriangulation
{
    private static boolean matches(Vec2 a0, Vec2 b0, Vec2 a1, Vec2 b1)
    {
        return (a0 == a1 && b0 == b1) || (a0 == b1 && b0 == a1);
    }
    
    private List<Triangle> triangles = new LinkedList<>();
    private LinkedList<Edge> hull = new LinkedList<>();
    private LinkedList<Edge> markedEdges = new LinkedList<>();
    
    public DelaunayTriangulation(Vec2... points)
    {
        if (points.length < 3) return;
        
        /**
         * Sort all points in ascending horizontal order. Vertically collinear
         * points are sorted in descending vertical order.
         */
        Arrays.sort(points, (Vec2 v1, Vec2 v2) ->
        {
            int horizontal = (int)Math.signum(v1.x - v2.x);
            if (horizontal == 0)
            {
                int vertical = (int)Math.signum(v2.y - v1.y);
                if (vertical == 0) throw new IllegalArgumentException("Duplicate point!");
                return vertical;
            }
            return horizontal;
        });
        
        //Count number of leading collinear points
        Vec2 dir = Vec2.sub(points[1], points[0]);
        int numCollinear = 2;
        for (int i=2; i<points.length; i++)
        {
            if (Util.isZero(Vec2.sub(points[i], points[0]).cross(dir), .001f)) numCollinear++;
            else break;
        }
        int firstPoint;
        
        if (numCollinear > 2)
        {
            //Generate leading hull
            for (int i=1; i<numCollinear; i++)
                hull.addLast(new Edge(points[i-1], points[i]));
            for (int i=numCollinear-1; i>=1; i--)
                hull.addLast(new Edge(points[i], points[i-1]));
            
            firstPoint = numCollinear;
        }
        else
        {
            //If no collinear points, generate first triangle
            Vec2 p0 = points[0];
            Vec2 p1 = points[1];
            Vec2 p2 = points[2];
            
            //Triangle and hull must have clockwise winding order
            if (new Line(p0, p2).side(p1) == 1)
            {
                Triangle triangle = new Triangle(p0, p1, p2);
                triangles.add(triangle);
                hull.add(new Edge(p0, p1, triangle));
                hull.add(new Edge(p1, p2, triangle));
                hull.add(new Edge(p2, p0, triangle));
            }
            else
            {
                Triangle triangle = new Triangle(p0, p2, p1);
                triangles.add(triangle);
                hull.add(new Edge(p0, p2, triangle));
                hull.add(new Edge(p2, p1, triangle));
                hull.add(new Edge(p1, p0, triangle));
            }
            
            for (Edge edge : hull) edge.add();
            
            firstPoint = 3;
        }
        
        //Start building triangles
        for (int i=firstPoint; i<points.length; i++)
        {
            //Work our way down the top leading edge, generating triangles for
            //all hull edges that face our point.
            Vec2 point = points[i];
            
            //Any point added will always add two edges to the hull
            Edge leftEdge = new Edge(null, point), rightEdge = new Edge(point, null);
            int edgeIndex = -1;
            
            Triangle prevTriangle = null;
            
            boolean foundEdge = false;
            ListIterator<Edge> it = hull.listIterator();
            while (it.hasNext())
            {
                Edge edge = it.next();
                if (edge.faces(point))
                {
                    Triangle triangle = new Triangle(edge.a, point, edge.b);
                    triangles.add(triangle);
                    
                    //Maintain references
                    if (edge.inside == null) edge.inside = triangle;
                    else
                    {
                        edge.outside = triangle;
                    }
                    triangle.edges[2] = edge;
                    if (prevTriangle != null)
                    {
                        Edge splitEdge = new Edge(triangle.a, triangle.b);
                        splitEdge.inside = prevTriangle;
                        splitEdge.outside = triangle;
                        splitEdge.add();
                        splitEdge.mark();
                    }
                    prevTriangle = triangle;
                    
                    if (!foundEdge)
                    {
                        leftEdge.a = edge.a;
                        leftEdge.inside = triangle;
                        leftEdge.add();
                        foundEdge = true;
                        edgeIndex = it.previousIndex();
                    }
                    
                    it.remove();
                    edge.mark();
                    rightEdge.b = edge.b;
                    rightEdge.inside = triangle;
                }
                else if (foundEdge) break; //We can terminate early because our hull is convex
            }
            
            rightEdge.add();
            
            //Update the hull to include our new edges
            hull.add(edgeIndex, rightEdge);
            hull.add(edgeIndex, leftEdge);
        }
        
        while (!markedEdges.isEmpty())
        {
            Edge edge = markedEdges.pop();
            edge.mark = false;
            validate(edge);
        }
        
//        //Debug mode.
//        {
////            int numIllegalTriangles = 0;
////            for (Vector2f point : points) for (Triangle triangle : triangles)
////            {
////                float dsq = Util.sqrt(triangle.circumcenter.squareDist(point)) - Util.sqrt(triangle.circumradiusSq);
////                if (dsq < -1f) numIllegalTriangles++;
////            }
////            System.out.println("Illegal triangles found: " + numIllegalTriangles);
//            System.out.println("Number of points triangulated: " + points.length);
//            System.out.println("Number of triangles made: " + triangles.size());
//        }
    }
    
    public DelaunayTriangulation(Collection<Vec2> collection)
    {
        this(collection.toArray(new Vec2[collection.size()]));
    }
    
    /**
     * This is where the algorithm spends most of its time. Optimize this.
     */
    private void validate(Edge edge)
    {
        Triangle inside = edge.inside, outside = edge.outside;
        Vec2 c = outside.adjacentPoint(edge);
        
        //Perform a Lawson flip if our edge isn't Delaunay
        if (inside.circumcenter.squareDist(c) < inside.circumradiusSq)
        {
            Vec2 a = inside.adjacentPoint(edge);
            Vec2 b = inside.leftPoint(edge);
            Vec2 d = outside.leftPoint(edge);

            Edge abe = inside.getEdge(a, b);
            Edge bce = outside.getEdge(b, c);
            Edge cde = outside.getEdge(c, d);
            Edge dae = inside.getEdge(d, a);
            
            Triangle abt = abe.getAdjacent(inside);
            Triangle bct = bce.getAdjacent(outside);
            Triangle cdt = cde.getAdjacent(outside);
            Triangle dat = dae.getAdjacent(inside);
            
            inside.set(a, b, c);
            inside.setEdges(abe, bce, edge);
            outside.set(a, c, d);
            outside.setEdges(edge, cde, dae);
            
            abe.inside = inside; abe.outside = abt;
            bce.inside = inside; bce.outside = bct;
            cde.inside = outside; cde.outside = cdt;
            dae.inside = outside; dae.outside = dat;
            
            edge.a = a;
            edge.b = c;
            
            abe.mark();
            bce.mark();
            cde.mark();
            dae.mark();
        }
    }
    
    public List<Triangle> getTriangles()
    {
        return Collections.unmodifiableList(triangles);
    }
    
    public List<Edge> getHull()
    {
        return Collections.unmodifiableList(hull);
    }
    
    public class Edge
    {
        public Vec2 a, b;
        public Triangle inside, outside;
        private boolean mark = false;
        
        private Edge(Vec2 a, Vec2 b, Triangle inside)
        {
            this.a = a; this.b = b; this.inside = inside;
        }
        
        private Edge(Vec2 a, Vec2 b)
        {
            this(a, b, null);
        }
        
        private boolean isInternal()
        {
            return inside != null && outside != null;
        }
        
        private void mark()
        {
            if (!mark && isInternal())
            {
                mark = true;
                markedEdges.addFirst(this);
            }
        }
        
        private boolean faces(Vec2 point)
        {
            return (b.x - a.x)*(point.y - a.y) > (point.x - a.x)*(b.y - a.y);
        }
        
        private void add()
        {
            if (inside != null) inside.addEdge(this);
            if (outside != null) outside.addEdge(this);
        }
        
        private Triangle getAdjacent(Triangle triangle)
        {
            if (inside == triangle) return outside;
            if (outside == triangle) return inside;
            throw new IllegalArgumentException();
        }
    }
    
    public class Triangle
    {
        public Vec2 a, b, c;
        public final Edge[] edges = new Edge[3];
        public final Vec2 circumcenter = new Vec2();
        public float circumradiusSq;
        
        private Triangle(Vec2 a, Vec2 b, Vec2 c)
        {
            set(a, b, c);
        }
        
        private void set(Vec2 a, Vec2 b, Vec2 c)
        {
            this.a = a; this.b = b; this.c = c;
            
            float asq = a.x*a.x + a.y*a.y;
            float bsq = b.x*b.x + b.y*b.y;
            float csq = c.x*c.x + c.y*c.y;
            
            circumcenter.x = asq*(b.y - c.y) + bsq*(c.y - a.y) + csq*(a.y - b.y);
            circumcenter.y = asq*(c.x - b.x) + bsq*(a.x - c.x) + csq*(b.x - a.x);
            circumcenter.div(2f*(a.x*(b.y - c.y) + b.x*(c.y - a.y) + c.x*(a.y - b.y)));
            
            circumradiusSq = a.squareDist(circumcenter);
        }
        
        private void setEdges(Edge ab, Edge bc, Edge ca)
        {
            edges[0] = ab;
            edges[1] = bc;
            edges[2] = ca;
        }
        
        private int edgeIndex(Vec2 a0, Vec2 b0)
        {
            if (matches(a0, b0, a, b)) return 0;
            if (matches(a0, b0, b, c)) return 1;
            if (matches(a0, b0, c, a)) return 2;
            return -1;
        }
        
        private int edgeIndex(Edge edge)
        {
            if (edge == null) throw new NullPointerException();
            return edgeIndex(edge.a, edge.b);
        }
        
        private void addEdge(Edge edge)
        {
            edges[edgeIndex(edge)] = edge;
        }
        
        private Edge getEdge(Vec2 a0, Vec2 b0)
        {
            if (a0 == null || b0 == null) throw new NullPointerException();
            int index = edgeIndex(a0, b0);
            return index == -1 ? null : edges[index];
        }
        
        private Vec2 leftPoint(Edge edge)
        {
            switch (edgeIndex(edge))
            {
                case 0: return a;
                case 1: return b;
                case 2: return c;
            }
            throw new IllegalArgumentException();
        }
        
        private Vec2 adjacentPoint(Edge edge)
        {
            switch (edgeIndex(edge))
            {
                case 0: return c;
                case 1: return a;
                case 2: return b;
            }
            throw new IllegalArgumentException();
        }
    }
}