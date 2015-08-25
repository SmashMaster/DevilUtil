package com.samrj.devil.geo3d;

import com.samrj.devil.graphics.model.Mesh;
import com.samrj.devil.math.Vec3;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Geometry
{
    private static void replace(List<Edge> edges, List<Face> faces, Vec3 ov, Vec3 nv)
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
    
    private static Edge findEdge(List<Edge> edges, Vec3 a, Vec3 b)
    {
        for (Edge edge : edges) if (edge.equals(a, b)) return edge;
        return null;
    }
    
    public static final void optimize(Geometry geom, float vertWeldDist)
    {
        ArrayList<Vec3> verts = new ArrayList<>(Arrays.asList(geom.verts));
        ArrayList<Edge> edges = new ArrayList<>(Arrays.asList(geom.edges));
        ArrayList<Face> faces = new ArrayList<>(Arrays.asList(geom.faces));
        
        {//Weld vertices
            vertWeldDist *= vertWeldDist;
            
            Deque<Vec3> unchecked = new LinkedList<>();
            unchecked.addAll(verts);
            verts.clear();

            while (!unchecked.isEmpty())
            {
                Vec3 vertex = unchecked.pop();
                float sum = 0.0f;

                Iterator<Vec3> it = unchecked.iterator();
                while (it.hasNext())
                {
                    Vec3 v = it.next();
                    if (v.squareDist(vertex) < vertWeldDist)
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
        
        geom.verts = verts.toArray(new Vec3[verts.size()]);
        geom.edges = edges.toArray(new Edge[edges.size()]);
        geom.faces = faces.toArray(new Face[faces.size()]);
    }
    
    public Vec3[] verts;
    public Edge[] edges;
    public Face[] faces;
    
    public Geometry(Mesh mesh)
    {
        mesh.rewindBuffers();
        
        ByteBuffer vertexData = mesh.vertexData();
        verts = new Vec3[mesh.numVertices];
        for (int i=0; i<mesh.numVertices; i++)
        {
            Vec3 v = new Vec3();
            v.read(vertexData);
            verts[i] = v;
        }
        
        ByteBuffer indexData = mesh.indexData();
        faces = new Face[mesh.numTriangles];
        edges = new Edge[mesh.numTriangles*3];
        for (int i=0; i<mesh.numTriangles; i++)
        {
            Face f = new Face(verts[indexData.getInt()],
                              verts[indexData.getInt()],
                              verts[indexData.getInt()]);
            faces[i] = f;
            int ie = i*3;
            edges[ie++] = f.ab;
            edges[ie++] = f.bc;
            edges[ie]   = f.ca;
        }
    }
    
    public Geometry()
    {
    }
}
