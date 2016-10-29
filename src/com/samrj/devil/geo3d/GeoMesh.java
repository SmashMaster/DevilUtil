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
import com.samrj.devil.math.Vec3;
import com.samrj.devil.model.Mesh;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * "Polygon soup" set of unoptimized triangles.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <VERT> The type of vertex this mesh contains.
 */
public class GeoMesh<VERT extends Vertex<VERT>> implements Geometry
{
    public final List<VERT> verts;
    public final List<Edge> edges;
    public final List<Face> faces;
    public final Box3 bounds = new Box3();
    
    public GeoMesh(Mesh mesh, Supplier<VERT> constructor)
    {
        verts = new ArrayList<>(mesh.numVertices);
        faces = new ArrayList<>(mesh.numTriangles);
        edges = new ArrayList<>(mesh.numTriangles*3);
        insert(mesh, constructor);
        updateBounds();
    }
    
    public GeoMesh()
    {
        verts = new ArrayList<>();
        edges = new ArrayList<>();
        faces = new ArrayList<>();
        updateBounds();
    }
    
    public final void insert(Mesh mesh, Supplier<VERT> constructor)
    {
        List<VERT> tempVerts = new ArrayList<>(mesh.numVertices);
        for (int i=0; i<mesh.numVertices; i++) tempVerts.add(constructor.get());
        
        Vertex first = tempVerts.get(0);
        ByteBuffer vBuffer = mesh.vertexData;
        
        for (int i=0; i<first.getNumAttributes(); i++)
        {
            vBuffer.position(first.getAttributeOffset(mesh, i));
            for (Vertex vert : tempVerts) vert.read(vBuffer, i);
        }
        
        ByteBuffer iBuffer = mesh.indexData;
        iBuffer.rewind();
        List<Face> tempFaces = new ArrayList<>(mesh.numTriangles);
        List<Edge> tempEdges = new ArrayList<>(mesh.numTriangles*3);
        
        for (int i=0; i<mesh.numTriangles; i++)
        {
            Vertex a = tempVerts.get(iBuffer.getInt());
            Vertex b = tempVerts.get(iBuffer.getInt());
            Vertex c = tempVerts.get(iBuffer.getInt());
            
            Face f = new Face(a, b, c);
            f.ab = new Edge(a, b);
            f.bc = new Edge(b, c);
            f.ca = new Edge(c, a);
            
            a.faces.add(f);
            b.faces.add(f);
            c.faces.add(f);
            
            f.ab.faces.add(f);
            f.bc.faces.add(f);
            f.ca.faces.add(f);
            
            tempEdges.add(f.ab);
            tempEdges.add(f.bc);
            tempEdges.add(f.ca);
            tempFaces.add(f);
        }
        iBuffer.rewind();
        
        verts.addAll(tempVerts);
        faces.addAll(tempFaces);
        edges.addAll(tempEdges);
    }
    
    private void replace(List<Edge> edges, List<Face> faces, Vertex ov, Vertex nv)
    {
        nv.faces.addAll(ov.faces);
        
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
     * Optimizes this geometry, removing degenerate vertices, edges, and faces.
     * Also calculates adjacency.
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
                        it.remove();
                        vertex.p.mult(sum).add(v.p).div(++sum);
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
                    if (edge.equals(e))
                    {
                        it.remove();
                        edge.faces.addAll(e.faces);
                    }
                }

                edges.add(edge);
            }
        }
    }
    
    public final void updateBounds()
    {
        bounds.setEmpty();
        for (Vertex vert : verts) bounds.expand(vert.p);
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
        public static final int POSITION = 0;
        
        public final Vec3 p = new Vec3();
        public final Set<Face> faces = Collections.newSetFromMap(new IdentityHashMap<>(3));
        
        public Vertex()
        {
        }
        
        public boolean canWeld(SELF_TYPE vert)
        {
            return true;
        }
        
        public int getNumAttributes()
        {
            return 1;
        }
        
        /**
         * Returns the buffer offset of the given attribute in bytes.
         */
        public int getAttributeOffset(Mesh mesh, int attributeIndex)
        {
            switch (attributeIndex)
            {
                case POSITION: return mesh.positionOffset;
                default: throw new IllegalArgumentException();
            }
        }
        
        public void read(ByteBuffer data, int attributeIndex)
        {
            switch (attributeIndex)
            {
                case POSITION: p.read(data); break;
                default: throw new IllegalArgumentException();
            }
        }

        @Override
        public final Type getType()
        {
            return Type.VERTEX;
        }

        @Override
        public Stream<Face> getFaces()
        {
            return faces.stream();
        }
    }
    
    public static final class Edge implements GeomObject
    {
        public Vertex a, b;
        public final Set<Face> faces = Collections.newSetFromMap(new IdentityHashMap<>(2));
        
        private Edge(Vertex a, Vertex b)
        {
            this.a = a; this.b = b;
        }
        
        public boolean equals(Edge edge)
        {
            return (this.a == edge.a && this.b == edge.b) ||
                   (this.a == edge.b && this.b == edge.a);
        }
        
        @Override
        public final Type getType()
        {
            return Type.EDGE;
        }

        @Override
        public Stream<Face> getFaces()
        {
            return faces.stream();
        }
    }
    
    public static final class Face implements Triangle3, GeomObject
    {
        public Vertex a, b, c;
        public Edge ab, bc, ca;
        
        private Face(Vertex a, Vertex b, Vertex c)
        {
            this.a = a; this.b = b; this.c = c;
        }
        
        @Override
        public Vec3 a()
        {
            return a.p;
        }

        @Override
        public Vec3 b()
        {
            return b.p;
        }

        @Override
        public Vec3 c()
        {
            return c.p;
        }
        
        @Override
        public final Type getType()
        {
            return Type.FACE;
        }

        @Override
        public Stream<Face> getFaces()
        {
            return Stream.of(this);
        }
        
        public Vec3 getNormal()
        {
            return Vec3.sub(c.p, a.p).cross(Vec3.sub(b.p, a.p)).normalize();
        }
    }
}
