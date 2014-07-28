package com.samrj.devil.ui;

import com.samrj.devil.math.Vector2f;

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
    
    public Vector2f dir()
    {
        return new Vector2f(x, y);
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