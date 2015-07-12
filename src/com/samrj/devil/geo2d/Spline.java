package com.samrj.devil.geo2d;

import com.samrj.devil.math.Vec2;

/**
 * Cubic Hermite spline.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Spline
{
    public static interface Node
    {
        public Vec2 getPos();
        public Vec2 getTan();
    }
    
    public static Vec2 getPos(Node a, Node b, float t)
    {
        if (t == 0f) return a.getPos();
        if (t == 1f) return b.getPos();
        
        float tsq = t*t, tcu = tsq*t;
        float f3 = -2f*tcu + 3f*tsq;
        
        Vec2 pos = a.getPos().mult(1f - f3);
        pos.add(   a.getTan().mult(tcu - 2f*tsq + t));
        pos.add(   b.getPos().mult(f3));
        pos.add(   b.getTan().mult(tcu - tsq));
        
        return pos;
    }
    
    public static Vec2 getTan(Node a, Node b, float t)
    {
        if (t == 0f) return a.getTan().normalize();
        if (t == 1f) return b.getTan().normalize();
        
        float tsq = t*t, tsqm3 = tsq*3f;
        float f0 = 6f*(tsq - t);
        
        Vec2 tan = a.getPos().mult(f0);
        tan.add(   a.getTan().mult(tsqm3 - 4f*t + 1f));
        tan.add(   b.getPos().mult(-f0));
        tan.add(   b.getTan().mult(tsqm3 - 2f*t));
        
        return tan.normalize();
    }
    
    /**
     * Estimates a rough bounding box for the given spline. Guaranteed to
     * contain whole curve, not guaranteed to be the minimum bounding box. Nodes
     * must be in cardinal order.
     */
    public static AAB getBounds(Node a, Node b)
    {
        AAB out = AAB.bounds(a.getPos(), b.getPos());
        
        final float MAX_TANGENT_T = 4f/27f;
        
        Vec2 aTan = a.getTan().mult(MAX_TANGENT_T);
        Vec2 bTan = b.getTan().mult(-MAX_TANGENT_T);
        
        if (aTan.x > 0f) out.x1 += aTan.x;
        else             out.x0 += aTan.x;
        
        if (bTan.x > 0f) out.x1 += bTan.x;
        else             out.x0 += bTan.x;
        
        if (aTan.y > 0f) out.y1 += aTan.y;
        else             out.y0 += aTan.y;
        
        if (bTan.y > 0f) out.y1 += bTan.y;
        else             out.y0 += bTan.y;
        
        return out;
    }
    
    private Spline()
    {
    }
}
