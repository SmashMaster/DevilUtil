package com.samrj.devil.ui;

import com.samrj.devil.math.Vec2;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public enum Alignment
{
    NW(-1f,  1f), N(0f,  1f), NE(1f,  1f),
    W (-1f,  0f), C(0f,  0f),  E(1f,  0f),
    SW(-1f, -1f), S(0f, -1f), SE(1f, -1f);
    
    public final float x, y;
    
    private Alignment(float x, float y)
    {
        this.x = x; this.y = y;
    }
    
    public Vec2 dir()
    {
        return new Vec2(x, y);
    }
    
    public void align(Vec2 center, Vec2 radius, Vec2 result)
    {
        result.x = center.x + x*radius.x;
        result.y = center.y + y*radius.y;
    }
    
    public void align(Vec2 center, Vec2 radius)
    {
        align(center, radius, center);
    }
    
    public Alignment opp()
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
