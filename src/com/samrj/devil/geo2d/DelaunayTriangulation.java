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
        
        //Start building triangles
        for (int i=firstPoint; i<points.length; i++)
        {
            //Work our way down the top leading edge, generating triangles for
            //all hull edges that face our point.
            Vector2f point = points[i];
            
            //Any point added will always add two edges to the hull
            Edge leftEdge = new Edge(null, point), rightEdge = new Edge(point, null);
            int edgeIndex = -1;
            
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
                    rightEdge.add();
                    
                    //Perform a Lawson flip if our edge does not meet the Delaunay criterion
                    validate(edge, triangle, point);
                }
                else if (foundEdge) break; //We can terminate early because our hull is convex
            }
            
            //Update the hull to include our new edges
            hull.add(edgeIndex, rightEdge);
            hull.add(edgeIndex, leftEdge);
        }
    }
    
    private void validate(Edge edge, Triangle outside, Vector2f point)
    {
        Triangle inside = edge.inside;
        edge.remove();
        if (inside.circumcenter.squareDist(point) < inside.circumradiusSq)
            flip(edge, inside, outside);
    }
    
    private void flip(Edge edge, Triangle inside, Triangle outside)
    {
        Vector2f a = inside.adjacentPoint(edge);
        Vector2f b = edge.a;
        Vector2f c = outside.adjacentPoint(edge);
        Vector2f d = edge.b;
        
        Edge ab = inside.getEdge(a, b);
        Edge bc = outside.getEdge(b, c);
        Edge cd = outside.getEdge(c, d);
        Edge da = inside.getEdge(d, a);
        
        inside.set(a, b, c);
        inside.setEdges(ab, bc, null);
        outside.set(a, c, d);
        outside.setEdges(null, cd, da);
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
        public Triangle inside;
        
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
            inside.addEdge(this);
        }
        
        private void remove()
        {
            inside.removeEdge(this);
        }
    }
    
    public class Triangle
    {
        public Vector2f a, b, c;
        private final Edge[] edges = new Edge[3];
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
            
            for (Edge edge : edges) if (edge != null) edge.inside = this;
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
        
        private void removeEdge(Edge edge)
        {
            edges[edgeIndex(edge)] = null;
        }
        
        private void addEdge(Edge edge)
        {
            edges[edgeIndex(edge)] = edge;
        }
        
        private Edge getEdge(Vector2f a, Vector2f b)
        {
            if (a == null || b == null) throw new NullPointerException();
            int index = edgeIndex(a, b);
            return index == -1 ? null : edges[index];
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