package com.samrj.devil.geo2d;

import com.samrj.devil.math.Range;
import com.samrj.devil.math.Vec2;

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
    public final Vec2[] verts;
    
    public ConvexPoly(Vec2... verts)
    {
        if (verts.length <= 3) throw new IllegalArgumentException();
        
        this.verts = new Vec2[verts.length];
        for (int i=0; i<verts.length; i++) this.verts[i] = new Vec2(verts[i]);
        
        if (!isConvex()) throw new ConcavePolyException();
    }
    
    public ConvexPoly(Box box)
    {
        this(box.vertices());
    }
    
    public ConvexPoly(ConvexPoly poly)
    {
        this.verts = new Vec2[poly.verts.length];
        for (int i=0; i<verts.length; i++) verts[i] = new Vec2(poly.verts[i]);
    }
    
    private boolean isConvex()
    {
        Vec2 prev = null, d2 = null;
        for (int i=0; i<verts.length; i++)
        {
            Vec2 v = new Vec2(verts[i]);
            this.verts[i] = v;
            
            Vec2 d1 = (prev != null) ? Vec2.sub(v, prev) : null;
            if (d2 != null && d1.cross(d2) < 0f) return false;
            
            d2 = d1;
            prev = v;
        }
        
        Vec2 v = this.verts[verts.length - 1];
        Vec2 d1 = Vec2.sub(v, prev);
        if (d1.cross(d2) < 0f) return false;
        
        return true;
    }
    
    public ConvexPoly translate(Vec2 v)
    {
        for (Vec2 vert : verts) vert.add(v);
        return this;
    }
    
    public Range project(Vec2 tan)
    {
        Range out = new Range();
        for (Vec2 vert : verts) out.expand(vert.dot(tan));
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
            
            Vec2 nrm = edge.normal();
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
