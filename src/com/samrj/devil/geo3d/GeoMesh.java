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

import com.samrj.devil.geo3d.GeoMesh.Vertex;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.model.Mesh;
import com.samrj.devil.model.ModelObject;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * "Polygon soup" set of unoptimized triangles.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <VERT> The type of vertex this mesh contains.
 */
public class GeoMesh<VERT extends Vertex> implements Geometry
{
    public final List<VERT> verts;
    public final List<Edge> edges;
    public final List<Face> faces;
    public final Box3 bounds;
    
    public GeoMesh(Mesh mesh, Function<Vec3, VERT> constructor, Mat4 transform)
    {
        ByteBuffer vBuffer = mesh.vertexData;
        vBuffer.rewind();
        verts = new ArrayList<>(mesh.numVertices);
        Vec3 p = new Vec3();
        for (int i=0; i<mesh.numVertices; i++)
        {
            p.read(vBuffer);
            p.mult(transform);
            verts.add(constructor.apply(p));
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
            edges.add(new Edge(f.a, f.b));
            edges.add(new Edge(f.b, f.c));
            edges.add(new Edge(f.c, f.a));
        }
        iBuffer.rewind();
        
        optimize(0.0f);
        
        bounds = Box3.empty();
        for (Vertex vert : verts) bounds.expand(vert.p);
    }
    
    public GeoMesh(Mesh mesh, Function<Vec3, VERT> constructor)
    {
        this(mesh, constructor, Mat4.identity());
    }
    
    public GeoMesh(ModelObject<Mesh> object, Function<Vec3, VERT> constructor)
    {
        this(object.data.get(), constructor, object.transform.toMatrix());
    }
    
    private void replace(List<Edge> edges, List<Face> faces, VERT ov, VERT nv)
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
            
            Deque<VERT> unchecked = new LinkedList<>();
            unchecked.addAll(verts);
            verts.clear();

            while (!unchecked.isEmpty())
            {
                VERT vertex = unchecked.pop();
                float sum = 0.0f;

                Iterator<VERT> it = unchecked.iterator();
                while (it.hasNext())
                {
                    VERT v = it.next();
                    if (v.p.squareDist(vertex.p) <= vertWeldDist && v.canWeld(vertex))
                    {
                        vertex.p.mult(sum).add(v.p).div(++sum);
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
                if (Geo3DUtil.area(f.a.p, f.b.p, f.c.p) < 0.0001f) it.remove();
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
    
    @Override
    public Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp)
    {
        return faces.stream().map(f -> Geo3DUtil.raycast(p0, dp, f))
                .filter(e -> e != null);
    }
    
    @Override
    public Stream<IsectResult> intersectUnsorted(ConvexShape shape)
    {
        return Stream.concat(Stream.concat(
                faces.stream().map(f -> shape.isect(f)),
                edges.stream().map(e -> shape.isect(e))),
                verts.stream().map(v -> shape.isect(v)))
                    .filter(e -> e != null);
    }
    
    @Override
    public Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp)
    {
        return Stream.concat(Stream.concat(
                faces.stream().map(f -> shape.sweep(dp, f)),
                edges.stream().map(e -> shape.sweep(dp, e))),
                verts.stream().map(v -> shape.sweep(dp, v)))
                    .filter(e -> e != null);
    }
    
    public static class Vertex<SELF_TYPE extends Vertex> implements GeomObject
    {
        public final Vec3 p = new Vec3();
        
        public Vertex(Vec3 p)
        {
            Vec3.copy(p, this.p);
        }
        
        public boolean canWeld(SELF_TYPE vert)
        {
            return true;
        }

        @Override
        public final Type getType()
        {
            return Type.VERTEX;
        }
    }
    
    public final class Edge implements GeomObject
    {
        public VERT a, b;
        
        private Edge(VERT a, VERT b)
        {
            this.a = a; this.b = b;
        }
        
        public boolean equals(VERT a, VERT b)
        {
            return (this.a == a && this.b == b) ||
                   (this.a == b && this.b == a);
        }

        public boolean equals(Edge edge)
        {
            return equals(edge.a, edge.b);
        }
        
        @Override
        public final Type getType()
        {
            return Type.EDGE;
        }
    }
    
    public final class Face implements GeomObject
    {
        public VERT a, b, c;
        
        private Face(VERT a, VERT b, VERT c)
        {
            this.a = a; this.b = b; this.c = c;
        }
        
        @Override
        public final Type getType()
        {
            return Type.FACE;
        }
    }
}
