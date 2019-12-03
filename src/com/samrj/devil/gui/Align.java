package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;

/**
 * DevilUI alignment class and utility methods.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public enum Align
{
    NW(0.0f, 1.0f), N(0.5f, 1.0f), NE(1.0f, 1.0f),
    W (0.0f, 0.5f), C(0.5f, 0.5f),  E(1.0f, 0.5f),
    SW(0.0f, 0.0f), S(0.5f, 0.0f), SE(1.0f, 0.0f);
    
    public final float x, y;
    
    public static Vec2 insideBounds(Vec2 size, float x0, float x1, float y0, float y1, Vec2 alignment)
    {
        return new Vec2(x0 + (x1 - x0 - size.x)*alignment.x,
                        y0 + (y1 - y0 - size.y)*alignment.y);
    }
    
    public static Vec2 toEdge(Vec2 size, float edgeX, float edgeY, Vec2 alignment)
    {
        return new Vec2(edgeX + (alignment.x - 1.0f)*size.x,
                        edgeY + (alignment.y - 1.0f)*size.y);
    }
    
    private Align(float x, float y)
    {
        this.x = x; this.y = y;
    }
    
    public Vec2 vector()
    {
        return new Vec2(x, y);
    }
    
    public Align opposite()
    {
        switch (this)
        {
            case NW: return SE; /**/ case N: return S; /**/ case NE: return SW;
            case W:  return E;  /**/ case C: return C; /**/  case E: return W;
            case SW: return SW; /**/ case S: return N; /**/ case SE: return NW;
            default: return null;
        }
    }
}
