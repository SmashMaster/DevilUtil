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

import com.samrj.devil.graphics.model.Mesh;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * "Polygon soup" set of unoptimized triangles.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class GeoMesh
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
    
    public final List<Vec3> verts;
    public final List<Edge> edges;
    public final List<Face> faces;
    
    public GeoMesh(Mesh mesh)
    {
        ByteBuffer vBuffer = mesh.vertexData;
        vBuffer.rewind();
        verts = new ArrayList<>(mesh.numVertices);
        for (int i=0; i<mesh.numVertices; i++)
        {
            Vec3 p = new Vec3();
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
                              verts.get(iBuffer.getInt()), mesh.materials != null ? mesh.materials[i] : 0);
            faces.add(f);
            edges.add(f.ab);
            edges.add(f.bc);
            edges.add(f.ca);
        }
        iBuffer.rewind();
        
        optimize(0.0f);
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
                if (Geo3DUtil.area(f.a, f.b, f.c) < 0.0001f) it.remove();
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
    
    public Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp)
    {
        return faces.stream().map(f -> Geo3DUtil.raycast(p0, dp, f))
                .filter(e -> e != null);
    }
    
    public Stream<RaycastResult> raycast(Vec3 p0, Vec3 dp)
    {
        return raycastUnsorted(p0, dp)
                .sorted((a, b) -> Util.compare(a.time, b.time));
    }
    
    public RaycastResult raycastFirst(Vec3 p0, Vec3 dp)
    {
        return raycastUnsorted(p0, dp)
                .reduce((a, b) -> a.time < b.time ? a : b)
                .orElse(null);
    }
    
    public Stream<IsectResult> intersectUnsorted(ConvexShape shape)
    {
        return Stream.concat(Stream.concat(
                faces.stream().map(f -> shape.isect(f)),
                edges.stream().map(e -> shape.isect(e))),
                verts.stream().map(v -> shape.isect(v)))
                    .filter(e -> e != null);
    }
    
    public Stream<IsectResult> intersect(ConvexShape shape)
    {
        return intersectUnsorted(shape)
                .sorted((a, b) -> Util.compare(b.depth, a.depth, 0.0f));
    }
    
    public IsectResult intersectDeepest(ConvexShape shape)
    {
        return intersectUnsorted(shape)
                .reduce((a, b) -> a.depth > b.depth ? a : b)
                .orElse(null);
    }
    
    public Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp)
    {
        return Stream.concat(Stream.concat(
                faces.stream().map(f -> shape.sweep(dp, f)),
                edges.stream().map(e -> shape.sweep(dp, e))),
                verts.stream().map(v -> shape.sweep(dp, v)))
                    .filter(e -> e != null);
    }
    
    public Stream<SweepResult> sweep(ConvexShape shape, Vec3 dp)
    {
        return sweepUnsorted(shape, dp)
                .sorted((a, b) -> Util.compare(a.time, b.time, 0.0f));
    }
    
    public SweepResult sweepFirst(ConvexShape shape, Vec3 dp)
    {
        return sweepUnsorted(shape, dp)
                .reduce((a, b) -> a.time < b.time ? a : b)
                .orElse(null);
    }
    
    public class Edge
    {
        public Vec3 a, b;
        
        private Edge(Vec3 a, Vec3 b)
        {
            this.a = a; this.b = b;
        }
        
        public boolean equals(Vec3 a, Vec3 b)
        {
            return (this.a == a && this.b == b) ||
                   (this.a == b && this.b == a);
        }

        public boolean equals(Edge edge)
        {
            return equals(edge.a, edge.b);
        }
    }
    
    public class Face
    {
        public Vec3 a, b, c;
        public Edge ab, bc, ca;
        public final int material;
        
        private Face(Vec3 a, Vec3 b, Vec3 c, int material)
        {
            this.a = a; this.b = b; this.c = c;
            ab = new Edge(a, b);
            bc = new Edge(b, c);
            ca = new Edge(c, a);
            this.material = material;
        }
    }
}
