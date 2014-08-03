package com.samrj.devil.math;

import com.samrj.devil.buffer.Bufferable;
import com.samrj.devil.buffer.IntBuffer;
import org.lwjgl.opengl.GL11;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Vector2i implements Bufferable<IntBuffer>
{
    public int x, y;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Vector2i(int x, int y)
    {
        set(x, y);
    }
    
    public Vector2i(Vector2i v)
    {
        set(v);
    }
    
    public Vector2i()
    {
        set();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutator Methods">
    public Vector2i set(int x, int y)
    {
        this.x = x; this.y = y;
        return this;
    }

    public Vector2i set(Vector2i v)
    {
        return set(v.x, v.y);
    }
    
    public Vector2i set()
    {
        return set(0, 0);
    }

    public Vector2i add(int x, int y)
    {
        return set(this.x+x, this.y+y);
    }
    
    public Vector2i add(Vector2i v)
    {
        return add(v.x, v.y);
    }
    
    public Vector2i sub(int x, int y)
    {
        return set(this.x-x, this.y-y);
    }

    public Vector2i sub(Vector2i v)
    {
        return sub(v.x, v.y);
    }
    
    public Vector2i mult(int s)
    {
        return set(x*s, y*s);
    }
    
    public Vector2i div(int s)
    {
        return set(x/s, y/s);
    }
    
    public Vector2i rotateCW()  {return set(y, -x);}
    public Vector2i rotateCCW() {return set(-y, x);}
    public Vector2i reflectX()  {return set(-x, y);}
    public Vector2i reflectY()  {return set(x, -y);}
    public Vector2i negate()    {return set(-x, -y);}
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessor Methods">
    public int squareLength()
    {
        return x*x + y*y;
    }

    public float length()
    {
        return Util.sqrt(squareLength());
    }
    
    public int chebyLength()
    {
        return Math.max(Math.abs(x),
                        Math.abs(y));
    }
    
    public int squareDist(Vector2i v)
    {
        if (v == null) throw new IllegalArgumentException();
        
        final int dx = x - v.x,
                  dy = y - v.y;
        
        return dx*dx + dy*dy;
    }

    public float dist(Vector2i v)
    {
        return Util.sqrt(squareDist(v));
    }
    
    public int chebyDist(Vector2i v)
    {
        return Math.max(Math.abs(v.x - x),
                        Math.abs(v.y - y));
    }
    
    public int dot(Vector2i v)
    {
        return x*v.x + y*v.y;
    }
    
    public int cross(Vector2i v)
    {
        return x*v.y - v.x*y;
    }
    
    public float angle()
    {
        return Util.atan2(y, x);
    }
    
    public boolean isZero()
    {
        return x == 0f &&
               y == 0f;
    }
    
    public boolean equals(int x, int y)
    {
        return this.x == x &&
               this.y == y;
    }
    
    public boolean equals(Vector2i v)
    {
        return equals(v.x, v.y);
    }
    
    public void glVertex()
    {
        GL11.glVertex2i(x, y);
    }
    
    public void glTranslate()
    {
        GL11.glTranslatef(x, y, 0f);
    }
    
    public org.jbox2d.common.Vec2 asB2D()
    {
        return new org.jbox2d.common.Vec2(x, y);
    }
    
    public Vector2d as2D()
    {
        return new Vector2d(this);
    }
    
    public Vector2f as2f()
    {
        return new Vector2f(this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Object Methods">
    /**
     * Returns the hash code of this vector. Guaranteed no collisions in this
     * range:
     * X: [-16384, 16384]
     * Y: [-16384, 16384]
     */
    @Override
    public int hashCode()
    {
        int hash = Math.abs(x);
        if (x < 0) hash ^= (1 << 15);
                   hash ^= Integer.rotateLeft(Math.abs(y), 16);
        if (y < 0) hash ^= (1 << 31);
        
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        return equals((Vector2i)obj);
    }
    
    @Override
    public String toString()
    {
        return "("+x+", "+y+")";
    }
    
    @Override
    public Vector2i clone()
    {
        return new Vector2i(this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Bufferable Methods">
    @Override
    public void putIn(IntBuffer buf)
    {
        buf.put(x, y);
    }
    
    @Override
    public int size()
    {
        return 2;
    }
    // </editor-fold>
}