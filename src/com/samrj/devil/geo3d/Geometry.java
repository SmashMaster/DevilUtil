package com.samrj.devil.geo3d;

import com.samrj.devil.graphics.model.Mesh;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 3D geometry class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Geometry
{
    static final float solveQuadratic(float a, float b, float c)
    {
        float[] solutions = Util.quadFormula(a, b, c);
        
        switch (solutions.length)
        {
            case 0: return Float.NaN;
            case 1: return solutions[0];
            case 2:
                float s1 = solutions[0];
                float s2 = solutions[1];
                
                if (s1 < 0.0f || s2 < 0.0f)
                     return s1 > s2 ? s1 : s2; //If either are negative, return the larger one.
                else return s1 < s2 ? s1 : s2; //Otherwise, return the smaller one.
            default:
                assert(false);
                throw new Error();
        }
    }
    
    private static void replace(List<Edge> edges, List<Face> faces, Vertex ov, Vertex nv)
    {
        for (Edge edge : edges)
        {
            if (edge.a == ov) edge.a = nv;
            if (edge.b == ov) edge.b = nv;
        }
        
        for (Face face : faces)
        {
            if (face.a == ov) face.a = nv;
            if (face.b == ov) face.b = nv;
            if (face.c == ov) face.c = nv;
        }
    }
    
    private static Edge findEdge(List<Edge> edges, Vertex a, Vertex b)
    {
        for (Edge edge : edges) if (edge.equals(a, b)) return edge;
        return null;
    }
    
    public final List<Vertex> verts;
    public final List<Edge> edges;
    public final List<Face> faces;
    
    public Geometry(Mesh mesh)
    {
        mesh.rewindBuffers();
        
        ByteBuffer vertexData = mesh.vertexData();
        verts = new ArrayList<>(mesh.numVertices);
        for (int i=0; i<mesh.numVertices; i++)
        {
            Vertex p = new Vertex();
            p.read(vertexData);
            verts.add(p);
        }
        
        ByteBuffer indexData = mesh.indexData();
        faces = new ArrayList<>(mesh.numTriangles);
        edges = new ArrayList<>(mesh.numTriangles*3);
        for (int i=0; i<mesh.numTriangles; i++)
        {
            Face f = new Face(verts.get(indexData.getInt()),
                              verts.get(indexData.getInt()),
                              verts.get(indexData.getInt()));
            faces.add(f);
            edges.add(f.ab);
            edges.add(f.bc);
            edges.add(f.ca);
        }
        
        optimize(0.0f);
    }
    
    public Geometry(Geometry geom)
    {
        verts = new ArrayList<>(geom.verts);
        faces = new ArrayList<>(geom.faces);
        edges = new ArrayList<>(geom.edges);
    }
    
    public Geometry()
    {
        verts = new ArrayList<>();
        faces = new ArrayList<>();
        edges = new ArrayList<>();
    }
    
    /**
     * Optimizes this geometry, removing degenerate vertices, edges, and
     * triangles. Also calculates adjacency.
     * 
     * @param vertWeldDist The distance in which to weld nearby vertices.
     */
    public final void optimize(float vertWeldDist)
    {
        {//Weld vertices
            vertWeldDist *= vertWeldDist;
            
            Deque<Vertex> unchecked = new LinkedList<>();
            unchecked.addAll(verts);
            verts.clear();

            while (!unchecked.isEmpty())
            {
                Vertex vertex = unchecked.pop();
                float sum = 0.0f;

                Iterator<Vertex> it = unchecked.iterator();
                while (it.hasNext())
                {
                    Vertex v = it.next();
                    if (v.squareDist(vertex) <= vertWeldDist)
                    {
                        vertex.mult(sum).add(v).div(++sum);
                        it.remove();
                        replace(edges, faces, v, vertex);
                    }
                }

                verts.add(vertex);
            }
        }
        
        {//Remove degenerate edges
            Iterator<Edge> it = edges.iterator();
            
            while (it.hasNext())
            {
                Edge e = it.next();
                if (e.a == e.b) it.remove();
            }
        }
        
        {//Remove degenerate faces
            Iterator<Face> it = faces.iterator();
            
            while (it.hasNext())
            {
                Face f = it.next();
                if (f.a == f.b || f.b == f.c || f.c == f.a) it.remove();
            }
        }
        
        {//Remove redundant edges
            Deque<Edge> unchecked = new LinkedList<>();
            unchecked.addAll(edges);
            edges.clear();

            while (!unchecked.isEmpty())
            {
                Edge edge = unchecked.pop();

                Iterator<Edge> it = unchecked.iterator();
                while (it.hasNext())
                {
                    Edge e = it.next();
                    if (edge.equals(e)) it.remove();
                }

                edges.add(edge);
            }
        }
        
        for (Face face : faces) //Compute adjacency
        {
            face.ab = findEdge(edges, face.a, face.b);
            face.bc = findEdge(edges, face.b, face.c);
            face.ca = findEdge(edges, face.c, face.a);
        }
        
        /**
         * Could also remove the following cases.
         * -Vertices contained by edges.
         * -Vertices contained by faces.
         * -Edges contained by edges.
         * -Edges contained by faces.
         * -Faces with zero surface area. (All vertices collinear)
         * -Faces contained by faces.
         */
    }
    
    public EllipsoidClip clipEllipsoid(Vec3 p, Vec3 radius)
    {
        p = new Vec3(p);
        radius = new Vec3(radius);
        EllipsoidClip clip = new EllipsoidClip(p, radius);
        
        for (Vertex vertex : verts) clip.test(vertex);
        for (Edge edge : edges) clip.test(edge);
        for (Face face : faces) clip.test(face);
        
        return clip;
    }
    
    /**
     * Casts a ray through this geometry and returns a list of contacts.
     * 
     * @param p0 The start of the ray.
     * @param p1 The end of the ray.
     * @param terminate Whether to stop at the end of ray, or continue past.
     * @return A new RayCast object;
     */
    public RayCast castRay(Vec3 p0, Vec3 p1, boolean terminate)
    {
        p0 = new Vec3(p0);
        p1 = new Vec3(p1);
        RayCast cast = new RayCast(p0, p1, terminate);
        for (Face face : faces) cast.test(face);
        return cast;
    }
    
    /**
     * Casts an ellipsoid through this geometry and returns a list of contacts.
     * 
     * @param p0 The starting position of the ellipsoid.
     * @param p1 The end position of the ellipsoid.
     * @param radius The radii of the ellipsoid, along each axis.
     * @param terminated Whether to stop at the end position, or continue past.
     * @return A new EllipsoidCast object.
     */
    public EllipsoidCast castEllpsoid(Vec3 p0, Vec3 p1, Vec3 radius, boolean terminated)
    {
        p0 = new Vec3(p0);
        p1 = new Vec3(p1);
        radius = new Vec3(radius);
        EllipsoidCast cast = new EllipsoidCast(p0, p1, radius, terminated);
        
        for (Vertex vertex : verts) cast.test(vertex);
        for (Edge edge : edges) cast.test(edge);
        for (Face face : faces) cast.test(face);
        
        return cast;
    }
}
