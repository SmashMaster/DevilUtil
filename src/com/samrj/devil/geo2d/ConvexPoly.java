package com.samrj.devil.geo2d;

import com.samrj.devil.math.Range;
import com.samrj.devil.math.Vector2f;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ConvexPoly
{
    /**
     * Set of vertices in this polygon, in clockwise winding order.
     */
    public final Vector2f[] verts;
    
    public ConvexPoly(Vector2f... verts)
    {
        if (verts.length <= 3) throw new IllegalArgumentException();
        
        this.verts = new Vector2f[verts.length];
        for (int i=0; i<verts.length; i++) this.verts[i] = verts[i].copy();
        
        if (!isConvex()) throw new ConcavePolyException();
    }
    
    public ConvexPoly(Box box)
    {
        this(box.vertices());
    }
    
    public ConvexPoly(ConvexPoly poly)
    {
        this.verts = new Vector2f[poly.verts.length];
        for (int i=0; i<verts.length; i++) verts[i] = poly.verts[i].copy();
    }
    
    private boolean isConvex()
    {
        Vector2f prev = null, d2 = null;
        for (int i=0; i<verts.length; i++)
        {
            Vector2f v = verts[i].copy();
            this.verts[i] = v;
            
            Vector2f d1 = (prev != null) ? v.csub(prev) : null;
            if (d2 != null && d1.cross(d2) < 0f) return false;
            
            d2 = d1;
            prev = v;
        }
        
        Vector2f v = this.verts[verts.length - 1];
        Vector2f d1 = v.csub(prev);
        if (d1.cross(d2) < 0f) return false;
        
        return true;
    }
    
    public ConvexPoly translate(Vector2f v)
    {
        for (Vector2f vert : verts) vert.add(v);
        return this;
    }
    
    public Range project(Vector2f tan)
    {
        Range out = new Range();
        for (Vector2f vert : verts) out.expand(vert.dot(tan));
        return out;
    }
    
    public Seg edge(int index)
    {
        if (index < 0 || index >= verts.length) throw new IllegalArgumentException();
        
        return new Seg(
            (index == 0) ? verts[verts.length - 1] : verts[index - 1],
            verts[index]);
    }
    
    public Seg[] edges()
    {
        Seg[] out = new Seg[verts.length];
        for (int i=0; i<verts.length; i++) out[i] = edge(i);
        return out;
    }
    
    public boolean touches(ConvexPoly poly)
    {
        for (int i=0; i<verts.length + poly.verts.length; i++)
        {
            Seg edge = (i < verts.length) ? edge(i) : poly.edge(i - verts.length);
            
            Vector2f nrm = edge.normal();
            Range thisRange = this.project(nrm);
            Range polyRange = poly.project(nrm);
            
            if (!thisRange.touches(polyRange)) return false;
        }
        
        return true;
    }
    
    public int numVertices()
    {
        return verts.length;
    }
}
