package com.samrj.devil.geo2d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector2f;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DelaunayTriangulation
{
    private Set<Triangle> triangles = new HashSet<>();
    
    public DelaunayTriangulation(Vector2f... points)
    {
        Arrays.sort(points, (Vector2f v1, Vector2f v2) -> Util.signum(v1.x - v2.x));
        
        Edge topLeadingEdge;
        {
            Vector2f p0 = points[0];
            Vector2f p1 = points[1];

            topLeadingEdge = p0.y > p1.y ? new Edge(p0, p1) : new Edge(p1, p0);
        }
        
        for (int i=2; i<points.length; i++)
        {
            Edge edge = topLeadingEdge;
            //Work our way down the top leading edge, generating triangles for
            //all leading edges that face our point, and updating the leading
            //edge as we go.
        }
        
        //Fix all illegal triangles by flipping edges
    }
    
    private class Edge
    {
        private Vector2f a, b;
        private Edge next;
        
        private Edge(Vector2f a, Vector2f b)
        {
            this.a = a; this.b = b;
        }
    }
    
    private class Triangle
    {
        private Vector2f a, b, c;
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