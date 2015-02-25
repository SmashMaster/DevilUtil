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
                if (vertical == 0) throw new IllegalArgumentException();
                return vertical;
            }
            return horizontal;
        });
        
        //Count number of leading vertically colinear points
        float leftBound = points[0].x;
        int numColinear = 1;
        int firstPoint;
        while (numColinear < points.length && points[numColinear].x == leftBound) numColinear++;
        
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
                triangles.add(new Triangle(p0, p1, p2));
                hull.add(new Edge(p0, p1));
                hull.add(new Edge(p1, p2));
                hull.add(new Edge(p2, p0));
            }
            else
            {
                triangles.add(new Triangle(p0, p2, p1));
                hull.add(new Edge(p0, p2));
                hull.add(new Edge(p2, p1));
                hull.add(new Edge(p1, p0));
            }
            
            firstPoint = 3;
        }
        
        //Start building triangles
        for (int i=firstPoint; i<points.length; i++)
        {
            //Work our way down the top leading edge, generating triangles for
            //all hull edges that face our point.
            Vector2f point = points[i];
            
            //Any point added will always add two edges to the hull
            Vector2f edgeStart = null, edgeEnd = null;
            int edgeIndex = -1;
            
            boolean foundEdge = false;
            ListIterator<Edge> it = hull.listIterator();
            while (it.hasNext())
            {
                Edge edge = it.next();
                
                if (edge.faces(point))
                {
                    if (!foundEdge)
                    {
                        edgeStart = edge.a;
                        foundEdge = true;
                        edgeIndex = it.previousIndex();
                    }
                    
                    Triangle triangle = new Triangle(edge.a, point, edge.b);
                    triangles.add(triangle);
                    it.remove();
                    edgeEnd = edge.b;
                }
                else if (foundEdge) break; //We can terminate early because our hull is convex
            }
            
            //Update the hull to include our new edges
            hull.add(edgeIndex, new Edge(point, edgeEnd));
            hull.add(edgeIndex, new Edge(edgeStart, point));
        }
        
        //Fix all illegal triangles by flipping edges
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
        
        private Edge(Vector2f a, Vector2f b)
        {
            this.a = a; this.b = b;
        }
        
        private boolean faces(Vector2f point)
        {
            Vector2f d = b.csub(a);
            Vector2f w = point.csub(a);
            return Util.signum(d.cross(w)) == 1;
        }
    }
    
    public class Triangle
    {
        public Vector2f a, b, c;
        private Triangle ab, bc, ca;
        private final Vector2f circumcenter = new Vector2f();
        private float circumradiusSq;
        
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
    }
}