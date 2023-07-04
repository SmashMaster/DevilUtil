package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Basic geometry class which stores unordered, unstructured mesh data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GeoMesh
{
    public final List<? extends Vec3> verts;
    public final int[] edgeIndices;
    public final int[] faceIndices;
    public final List<Edge3> edges;
    public final List<Triangle3> faces;
    
    private final Box3 bounds = Box3.infinite();
    private boolean boundsDirty = true;

    public GeoMesh(List<? extends Vec3> verts, int[] edgeIndices, int[] faceIndices)
    {
        this.verts = Objects.requireNonNull(verts);
        this.edgeIndices = Objects.requireNonNull(edgeIndices);
        this.faceIndices = Objects.requireNonNull(faceIndices);

        edges = new ArrayList<>(edgeIndices.length/2);
        for (int i=0; i<edgeIndices.length;)
            edges.add(new Edge3Indexed(edgeIndices[i++], edgeIndices[i++]));

        faces = new ArrayList<>(faceIndices.length/3);
        for (int i=0; i<faceIndices.length;)
            faces.add(new Tri3Indexed(faceIndices[i++], faceIndices[i++], faceIndices[i++]));
    }

    public GeoMesh(List<Vec3> verts, List<Edge3> edges, List<Triangle3> faces)
    {
        edgeIndices = null;
        faceIndices = null;
        this.verts = verts != null ? verts : List.of();
        this.edges = edges != null ? edges : List.of();
        this.faces = faces != null ? faces : List.of();
    }

    public void getBounds(Box3 result)
    {
        if (boundsDirty) updateBounds();
        result.set(bounds);
    }

    public Box3 getBounds()
    {
        Box3 result = new Box3();
        getBounds(result);
        return result;
    }

    /**
     * Overrides the bounds of this mesh, and marks them as up-to-date. Useful for storing bounds calculations, so they
     * don't need to be repeated.
     */
    public void overrideBounds(Box3 box)
    {
        bounds.set(box);
        boundsDirty = false;
    }

    public boolean areBoundsDirty()
    {
        return boundsDirty;
    }

    public void markBoundsDirty()
    {
        boundsDirty = true;
    }

    public void updateBounds()
    {
        bounds.setEmpty();
        for (Vec3 vert : verts) bounds.expand(vert);
        if (faceIndices == null)
        {
            for (Triangle3 face : faces) bounds.expand(face);
            for (Edge3 edge : edges) bounds.expand(edge);
        }
        boundsDirty = false;
    }

    private class Edge3Indexed implements Edge3
    {
        private final int a, b;

        private Edge3Indexed(int a, int b)
        {
            this.a = a;
            this.b = b;
        }

        @Override
        public Vec3 a()
        {
            return verts.get(a);
        }

        @Override
        public Vec3 b()
        {
            return verts.get(b);
        }
    }

    private class Tri3Indexed implements Triangle3
    {
        private final int a, b, c;

        private Tri3Indexed(int a, int b, int c)
        {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public Vec3 a()
        {
            return verts.get(a);
        }

        @Override
        public Vec3 b()
        {
            return verts.get(b);
        }

        @Override
        public Vec3 c()
        {
            return verts.get(c);
        }
    }
}
