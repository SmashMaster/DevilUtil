package com.samrj.devil.geo3d;

import com.samrj.devil.graphics.model.Mesh;
import com.samrj.devil.math.Util;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 3D geometry class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Geometry implements Shape
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
        ByteBuffer vBuffer = mesh.vertexData;
        vBuffer.rewind();
        verts = new ArrayList<>(mesh.numVertices);
        for (int i=0; i<mesh.numVertices; i++)
        {
            Vertex p = new Vertex();
            p.read(vBuffer);
            verts.add(p);
        }
        vBuffer.rewind();
        
        ByteBuffer iBuffer = mesh.indexData;
        iBuffer.rewind();
        faces = new ArrayList<>(mesh.numTriangles);
        edges = new ArrayList<>(mesh.numTriangles*3);
        for (int i=0; i<mesh.numTriangles; i++)
        {
            Face f = new Face(verts.get(iBuffer.getInt()),
                              verts.get(iBuffer.getInt()),
                              verts.get(iBuffer.getInt()));
            faces.add(f);
            edges.add(f.ab);
            edges.add(f.bc);
            edges.add(f.ca);
        }
        iBuffer.rewind();
        
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
    
    private static Contact closer(Contact a, Contact b)
    {
        return (a == null || b == null) ? b : (a.t < b.t ? a : b);
    }
    
    private static Intersection deeper(Intersection a, Intersection b)
    {
        return (a == null || b == null) ? b : (a.d > b.d ? a : b);
    }
    
    private <T> T mapReduce(Function<Shape, T> mapper, BinaryOperator<T> reducer)
    {
        return Stream.of(verts, edges, faces)
                .flatMap(list -> list.stream())
                .map(mapper)
                .filter(e -> e != null)
                .reduce(reducer)
                .get();
    }
    
    @Override
    public Contact collide(SweptEllipsoid swEll)
    {
        return mapReduce(shape -> shape.collide(swEll), Geometry::closer);
    }
    
    @Override
    public Intersection collide(Ellipsoid ell)
    {
        return mapReduce(shape -> shape.collide(ell), Geometry::deeper);
    }
    
    @Override
    public Contact collide(Ray ray)
    {
        return mapReduce(shape -> shape.collide(ray), Geometry::closer);
    }
}
