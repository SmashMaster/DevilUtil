package com.samrj.devil.geo2d;

import com.samrj.devil.math.Vector2f;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DelaunayTriangulation
{
    private Set<Edge> edges = new HashSet<>();
    private Set<Triangle> triangles = new HashSet<>();
    
    public DelaunayTriangulation(Vector2f... pointArray)
    {
        Set<Vector2f> points = new HashSet<>(pointArray.length);
        for (Vector2f point : pointArray)
            points.add(Objects.requireNonNull(point));
        
        AAB bounds = AAB.bounds(pointArray);
        Vector2f size = bounds.size();
        
        //Create a triangle that contains all points in the set. 
        Vector2f tempPointA, tempPointB, tempPointC;
        {
            tempPointA = new Vector2f(bounds.x0 - size.x*0.5f, bounds.y0 - size.y*0.5f);
            tempPointB = new Vector2f(tempPointA.x, bounds.y1 + size.y*1.5f);
            tempPointC = new Vector2f(bounds.x1 + size.x*1.5f, tempPointA.y);
            
            Triangle tempTri = new Triangle(tempPointA, tempPointB, tempPointC);
            
            tempTri.edgeAB = new Edge(tempPointA, tempPointB);
            tempTri.edgeBC = new Edge(tempPointB, tempPointC);
            tempTri.edgeCA = new Edge(tempPointC, tempPointA);
            
            tempTri.edgeAB.right = tempTri;
            tempTri.edgeBC.right = tempTri;
            tempTri.edgeCA.right = tempTri;
            
            triangles.add(tempTri);
            edges.add(tempTri.edgeAB);
            edges.add(tempTri.edgeBC);
            edges.add(tempTri.edgeCA);
        }
        
        //Incrementally build triangulation
        for (Vector2f point : points)
        {
            Object containing = findContaining(point);
            if (containing instanceof Triangle)
            {
                Triangle triangle = (Triangle)containing;
                triangles.remove(triangle);
                
                Triangle triAB = new Triangle(triangle.vertA, triangle.vertB, point);
                Triangle triBC = new Triangle(point, triangle.vertB, triangle.vertC);
                Triangle triCA = new Triangle(triangle.vertA, point, triangle.vertC);
                
                //Update edge pointers
                Edge edgePA = new Edge(point, triangle.vertA);
                edgePA.left = triCA;
                edgePA.right = triAB;
                edges.add(edgePA);
                
                Edge edgePB = new Edge(point, triangle.vertB);
                edgePB.left = triAB;
                edgePB.right = triBC;
                edges.add(edgePB);
                
                Edge edgePC = new Edge(point, triangle.vertC);
                edgePC.left = triBC;
                edgePC.right = triCA;
                edges.add(edgePC);
                
                //Need to update triangle pointers
                //Also finish the rest of the algorithm
            }
            else if (containing instanceof Edge)
            {
                Edge edge = (Edge)containing;
            }
        }
    }
    
    private Object findContaining(Vector2f point)
    {
        for (Triangle triangle : triangles)
        {
            Object out = triangle.side(point);
            if (out != null) return out;
        }
        return null;
    }
    
    private class Edge
    {
        private Vector2f start, end;
        private Triangle left, right;
        
        private Edge(Vector2f start, Vector2f end)
        {
            this.start = start; this.end = end;
        }
    }
    
    private class Triangle
    {
        private Vector2f vertA, vertB, vertC;
        private Edge edgeAB, edgeBC, edgeCA;
        private Triangle triAB, triBC, triCA;
        
        private Triangle(Vector2f a, Vector2f b, Vector2f c)
        {
            vertA = a; vertB = b; vertC = c;
        }
        
        /**
         * @return null for outside, edge for on edge, this for inside.
         */
        private Object side(Vector2f point)
        {
            int abSide = new Line(vertA, vertB).side(point);
            if (abSide < 0) return -1;
            int bcSide = new Line(vertB, vertC).side(point);
            if (bcSide < 0) return -1;
            int caSide = new Line(vertC, vertA).side(point);
            if (caSide < 0) return -1;
            
            if (abSide == 0) return edgeAB;
            if (bcSide == 0) return edgeBC;
            if (caSide == 0) return edgeCA;
            
            return this;
        }
    }
}