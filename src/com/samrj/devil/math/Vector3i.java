package com.samrj.devil.math;

import com.samrj.devil.buffer.Bufferable;
import com.samrj.devil.buffer.IntBuffer;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Vector3i implements Bufferable<IntBuffer>
{
    // <editor-fold defaultstate="collapsed" desc="Factory Methods">
    public static Vector3i zero()
    {
        return new Vector3i(0, 0, 0);
    }
    // </editor-fold>
    
    public int x, y, z;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Vector3i(int x, int y, int z)
    {
        set(x, y, z);
    }
    
    public Vector3i(Vector3i v)
    {
        this(v.x, v.y, v.z);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutator Methods">
    public Vector3i set(int x, int y, int z)
    {
        this.x = x; this.y = y; this.z = z;
        return this;
    }

    public Vector3i set(Vector3i v)
    {
        return set(v.x, v.y, v.z);
    }
    
    public Vector3i set()
    {
        return set(0, 0, 0);
    }

    public Vector3i add(int x, int y, int z)
    {
        return set(this.x+x, this.y+y, this.z+z);
    }
    
    public Vector3i add(Vector3i v)
    {
        return add(v.x, v.y, v.z);
    }
    
    public Vector3i sub(int x, int y, int z)
    {
        return set(this.x-x, this.y-y, this.z-z);
    }

    public Vector3i sub(Vector3i v)
    {
        return sub(v.x, v.y, v.z);
    }
    
    public Vector3i mult(int s)
    {
        return set(x*s, y*s, z*s);
    }
    
    public Vector3i div(int s)
    {
        return set(x/s, y/s, z/s);
    }
    
    public Vector3i negate()
    {
        return set(-x, -y, -z);
    }
    
    public Vector3i cross(Vector3i v)
    {
        return set(y*v.z - z*v.y,
                   z*v.x - x*v.z,
                   x*v.y - y*v.x);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessor Methods">
    public int squareLength()
    {
        return x*x + y*y + z*z;
    }

    public float length()
    {
        return Util.sqrt(squareLength());
    }
    
    public int chebyLength()
    {
        return Util.max(Math.abs(x),
                        Math.abs(y),
                        Math.abs(z));
    }
    
    public int squareDist(Vector3i v)
    {
        if (v == null) throw new IllegalArgumentException();
        
        final int dx = x - v.x,
                  dy = y - v.y,
                  dz = z - v.z;
        
        return dx*dx + dy*dy + dz*dz;
    }

    public float dist(Vector3i v)
    {
        return Util.sqrt(squareDist(v));
    }
    
    public int chebyDist(Vector3i v)
    {
        return Util.max(Math.abs(v.x - x),
                        Math.abs(v.y - y),
                        Math.abs(v.z - z));
    }
    
    public int dot(Vector3i v)
    {
        return x*v.x + y*v.y + z*v.z;
    }
    
    public boolean isZero()
    {
        return x == 0f &&
               y == 0f &&
               z == 0f;
    }
    
    public boolean equals(float x, float y, float z)
    {
        return this.x == x &&
               this.y == y &&
               this.z == z;
    }
    
    public boolean equals(Vector3i v)
    {
        return equals(v.x, v.y, v.z);
    }
    
    public Vector3f as3f()
    {
        return new Vector3f(x, y, z);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Object Methods">
    /**
     * Returns the hash code of this vector. Guaranteed no collisions in this
     * range:
     * X: [-512, 512]
     * Y: [-256, 256]
     * Z: [-512, 512]
     */
    @Override
    public int hashCode()
    {
        int hash = Math.abs(x);
        if (x < 0) hash ^= (1 << 10);
                   hash ^= Integer.rotateLeft(Math.abs(y), 11);
        if (y < 0) hash ^= (1 << 20);
                   hash ^= Integer.rotateLeft(Math.abs(z), 21);
        if (z < 0) hash ^= (1 << 31);
        
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        return equals((Vector3i)obj);
    }
    
    @Override
    public String toString()
    {
        return "("+x+", "+y+", "+z+")";
    }
    
    @Override
    public Vector3i clone()
    {
        return new Vector3i(this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Bufferable Methods">
    @Override
    public void putIn(IntBuffer buf)
    {
        buf.put(x, y, z);
    }
    
    @Override
    public int size()
    {
        return 3;
    }
    // </editor-fold>
}