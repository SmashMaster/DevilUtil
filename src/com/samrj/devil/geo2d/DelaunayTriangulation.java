package com.samrj.devil.geo2d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector2f;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DelaunayTriangulation
{
    private static boolean matches(Vector2f a0, Vector2f b0, Vector2f a1, Vector2f b1)
    {
        return (a0 == a1 && b0 == b1) || (a0 == b1 && b0 == a1);
    }
    
    private Set<Triangle> triangles = new HashSet<>();
    private LinkedList<Edge> hull = new LinkedList<>();
    
    public DelaunayTriangulation(Vector2f... points)
    {
        if (points.length < 3) return;
        
        /**
         * Sort all points in ascending horizontal order. Vertically collinear
         * points are sorted in descending vertical order.
         */
        Arrays.sort(points, (Vector2f v1, Vector2f v2) ->
        {
            int horizontal = Util.signum(v1.x - v2.x);
            if (horizontal == 0)
            {
                int vertical = Util.signum(v2.y - v1.y);
                if (vertical == 0) throw new IllegalArgumentException("Duplicate point!");
                return vertical;
            }
            return horizontal;
        });
        
        //Count number of leading vertically colinear points
        float leftBound = points[0].x;
        int numColinear = 1;
        while (numColinear < points.length && points[numColinear].x == leftBound) numColinear++;
        int firstPoint;
        
        if (numColinear > 1)
        {
            //Generate vertical leading hull
            for (int i=1; i<numColinear; i++)
            {
                Edge edge = new Edge(points[i-1], points[i]);
                hull.addLast(edge);
            }
            
            firstPoint = numColinear;
        }
        else
        {
            //If no collinear points, generate first triangle
            Vector2f p0 = points[0];
            Vector2f p1 = points[1];
            Vector2f p2 = points[2];
            
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
        
        List<Edge> badEdges = new LinkedList<>();
        
        //Start building triangles
        for (int i=firstPoint; i<points.length; i++)
        {
            //Work our way down the top leading edge, generating triangles for
            //all hull edges that face our point.
            Vector2f point = points[i];
            
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
                        edge.inside.setNeighbor(edge, triangle);
                        edge.outside = triangle;
                    }
                    triangle.edges[2] = edge;
                    triangle.neighbors[2] = edge.inside;
                    if (prevTriangle != null)
                    {
                        Edge splitEdge = new Edge(triangle.a, triangle.b);
                        splitEdge.inside = prevTriangle;
                        splitEdge.outside = triangle;
                        splitEdge.add();
                        badEdges.add(splitEdge);
                        prevTriangle.neighbors[1] = triangle;
                        triangle.neighbors[0] = prevTriangle;
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
                    rightEdge.b = edge.b;
                    rightEdge.inside = triangle;
                    
                    badEdges.add(edge);
                }
                else if (foundEdge) break; //We can terminate early because our hull is convex
            }
            
            rightEdge.add();
            
            //Update the hull to include our new edges
            hull.add(edgeIndex, rightEdge);
            hull.add(edgeIndex, leftEdge);
        }
        
        for (int i=0; i<1000; i++) for (Edge edge : badEdges) validate(edge);
        
        //Now let's BRUTE FORCE TEST this motherfucker to make sure we're solid.
        //Get rid of this when this is fixed!!!
        for (Vector2f point : points) for (Triangle triangle : triangles)
        {
            float dsq = Util.sqrt(triangle.circumcenter.squareDist(point)) - Util.sqrt(triangle.circumradiusSq);
            if (dsq < -1f) throw new RuntimeException("Non-Delaunay triangulation. Error: " + -dsq);
        }
    }
    
    private void validate(Edge edge)
    {
        Triangle inside = edge.inside, outside = edge.outside;
        Vector2f c = outside.adjacentPoint(edge);
        
        if (inside.circumcenter.squareDist(c) < inside.circumradiusSq)
        {
            Vector2f a = inside.adjacentPoint(edge);
            Vector2f b = inside.leftPoint(edge);
            Vector2f d = outside.leftPoint(edge);

            Edge abe = inside.getEdge(a, b);
            Edge bce = outside.getEdge(b, c);
            Edge cde = outside.getEdge(c, d);
            Edge dae = inside.getEdge(d, a);

            Triangle abt = inside.getNeighbor(a, b);
            Triangle bct = outside.getNeighbor(b, c);
            Triangle cdt = outside.getNeighbor(c, d);
            Triangle dat = inside.getNeighbor(d, a);
            
            inside.set(a, b, c);
            inside.setEdges(abe, bce, edge);
            inside.setNeighbors(abt, bct, outside);
            outside.set(a, c, d);
            outside.setEdges(edge, cde, dae);
            outside.setNeighbors(inside, cdt, dat);
            
            abe.inside = inside; abe.outside = abt;
            bce.inside = inside; bce.outside = bct;
            cde.inside = outside; cde.outside = cdt;
            dae.inside = outside; dae.outside = dat;
            
            if (abt != null) abt.setNeighbor(a, b, inside);
            if (bct != null) bct.setNeighbor(b, c, inside);
            if (cdt != null) cdt.setNeighbor(c, d, outside);
            if (dat != null) dat.setNeighbor(d, a, outside);
            
            edge.a = a;
            edge.b = c;
        }
    }
    
    public Set<Triangle> getTriangles()
    {
        return Collections.unmodifiableSet(triangles);
    }
    
    public List<Edge> getHull()
    {
        return Collections.unmodifiableList(hull);
    }
    
    public class Edge
    {
        public Vector2f a, b;
        public Triangle inside, outside;
        
        private Edge(Vector2f a, Vector2f b, Triangle inside)
        {
            this.a = a; this.b = b; this.inside = inside;
        }
        
        private Edge(Vector2f a, Vector2f b)
        {
            this(a, b, null);
        }
        
        private boolean faces(Vector2f point)
        {
            Vector2f d = b.csub(a);
            Vector2f w = point.csub(a);
            return Util.signum(d.cross(w)) == 1;
        }
        
        private void add()
        {
            if (inside != null) inside.addEdge(this);
            if (outside != null) outside.addEdge(this);
        }
    }
    
    public class Triangle
    {
        public Vector2f a, b, c;
        public final Edge[] edges = new Edge[3];
        public final Triangle[] neighbors = new Triangle[3];
        public final Vector2f circumcenter = new Vector2f();
        public float circumradiusSq;
        
        private Triangle(Vector2f a, Vector2f b, Vector2f c)
        {
            set(a, b, c);
        }
        
        private void set(Vector2f a, Vector2f b, Vector2f c)
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
        
        private void setNeighbors(Triangle ab, Triangle bc, Triangle ca)
        {
            neighbors[0] = ab;
            neighbors[1] = bc;
            neighbors[2] = ca;
        }
        
        private int edgeIndex(Vector2f a0, Vector2f b0)
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
        
        private void setNeighbor(Edge edge, Triangle triangle)
        {
            neighbors[edgeIndex(edge)] = triangle;
        }
        
        private Edge getEdge(Vector2f a0, Vector2f b0)
        {
            if (a0 == null || b0 == null) throw new NullPointerException();
            int index = edgeIndex(a0, b0);
            return index == -1 ? null : edges[index];
        }
        
        private Triangle getNeighbor(Vector2f a0, Vector2f b0)
        {
            if (a0 == null || b0 == null) throw new NullPointerException();
            int index = edgeIndex(a0, b0);
            return index == -1 ? null : neighbors[index];
        }
        
        private void setNeighbor(Vector2f a0, Vector2f b0, Triangle triangle)
        {
            if (a0 == null || b0 == null) throw new NullPointerException();
            neighbors[edgeIndex(a0, b0)] = triangle;
        }
        
        private Vector2f leftPoint(Edge edge)
        {
            switch (edgeIndex(edge))
            {
                case 0: return a;
                case 1: return b;
                case 2: return c;
            }
            throw new IllegalArgumentException();
        }
        
        private Vector2f adjacentPoint(Edge edge)
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