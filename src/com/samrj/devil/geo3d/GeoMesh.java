package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

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
    public final Iterable<? extends Vec3> verts;
    public final Iterable<? extends Edge3> edges;
    public final Iterable<? extends Triangle3> faces;
    
    private final Box3 bounds = Box3.infinite();
    private boolean boundsDirty = true;

    public GeoMesh(Iterable<? extends Vec3> verts, Iterable<? extends Edge3> edges, Iterable<? extends Triangle3> faces)
    {
        this.verts = Objects.requireNonNull(verts);
        this.edges = Objects.requireNonNull(edges);
        this.faces = Objects.requireNonNull(faces);
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
        for (Triangle3 face : faces) bounds.expand(face);
        for (Edge3 edge : edges) bounds.expand(edge);
        for (Vec3 vert : verts) bounds.expand(vert);
        boundsDirty = false;
    }
}
