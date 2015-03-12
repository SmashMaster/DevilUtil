package com.samrj.devil.math;

/**
 * Mutable 32 bit, 3 component vector with method chaining for local mutators.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public class MutableVector3f extends Vector3f
{
    public float x, y, z;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors/Overriden Vector3f methods">
    /**
     * Initializes this to the given coordinates.
     */
    public MutableVector3f(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
    }
    
    /**
     * Initializes this to the given vector.
     */
    public MutableVector3f(Vector3f v)
    {
        this(v.x(), v.y(), v.z());
    }
    
    /**
     * Initializes this to the null vector.
     */
    public MutableVector3f()
    {
        this(0.0f, 0.0f, 0.0f);
    }

    @Override
    public float x()
    {
        return x;
    }

    @Override
    public float y()
    {
        return y;
    }
    
    @Override
    public float z()
    {
        return z;
    }
    
    @Override
    public ImmutableVector3f toImmutable()
    {
        return new ImmutableVector3f(x, y, z);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutators">
    /**
     * Sets the x component of this vector to the given value and returns this.
     */
    public MutableVector3f setX(float x)
    {
        this.x = x;
        return this;
    }
    
    /**
     * Sets the y component of this vector to the given value and returns this.
     */
    public MutableVector3f setY(float y)
    {
        this.y = y;
        return this;
    }
    
    /**
     * Sets the z component of this vector to the given value and returns this.
     */
    public MutableVector3f setZ(float z)
    {
        this.z = z;
        return this;
    }
    
    /**
     * Sets this vector to the null vector and returns this.
     */
    public MutableVector3f set()
    {
        this.x = 0.0f; this.y = 0.0f; this.z = 0.0f;
        return this;
    }
    
    /**
     * Sets this vector to the given coordinates and returns this.
     */
    public MutableVector3f set(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
        return this;
    }
    
    /**
     * Sets this vector to the given vector and returns this.
     */
    public MutableVector3f set(Vector3f v)
    {
        this.x = v.x(); this.y = v.y(); this.z = v.z();
        return this;
    }
    
    /**
     * Adds the given coordinates to this vector and returns this.
     */
    public MutableVector3f addLocal(float x, float y, float z)
    {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }
    
    /**
     * Adds the given vector to and returns this.
     */
    public MutableVector3f addLocal(Vector3f v)
    {
        x += v.x();
        y += v.y();
        z += v.z();
        return this;
    }
    
    /**
     * Subtracts the given coordinates from this vector and returns this.
     */
    public MutableVector3f subLocal(float x, float y, float z)
    {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }
    
    /**
     * Subtracts the given vector from and returns this.
     */
    public MutableVector3f subLocal(Vector3f v)
    {
        x -= v.x();
        y -= v.y();
        z -= v.z();
        return this;
    }
    
    /**
     * Multiplies this vector by the given value and returns this.
     */
    public MutableVector3f multLocal(float f)
    {
        x *= f;
        y *= f;
        z *= f;
        return this;
    }
    
    /**
     * Negates this vector and returns this.
     */
    public MutableVector3f negateLocal()
    {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }
    
    /**
     * Sets this vector to the cross product of this and the given vector and
     * returns this.
     */
    public MutableVector3f crossLocal(Vector3f v)
    {
        return set(y*v.z() - z*v.y(),
                   z*v.x() - x*v.z(),
                   x*v.y() - y*v.x());
    }
    
    /**
     * Divides this vector by the given value and returns this.
     */
    public MutableVector3f divLocal(float f)
    {
        x /= f;
        y /= f;
        z /= f;
        return this;
    }
    
    /**
     * Sets this vector to the average of this and the given vector, and returns
     * this.
     */
    public MutableVector3f avgLocal(Vector3f v)
    {
        x = (x + v.x())*0.5f;
        y = (y + v.y())*0.5f;
        z = (z + v.z())*0.5f;
        return this;
    }
    
    /**
     * Normalizes this vector and returns this.
     */
    public MutableVector3f normLocal()
    {
        final float sqLen = x*x + y*y + z*z;
        if (sqLen != 0.0f)
        {
            final float factor = 1.0f/(float)Math.sqrt(sqLen);
            x *= factor;
            y *= factor;
            z *= factor;
        }
        return this;
    }
    
    /**
     * Sets the length of this vector to the given value and returns this.
     */
    public MutableVector3f multNormLocal(float f)
    {
        final float sqLen = x*x + y*y + z*z;
        if (sqLen != 0.0f)
        {
            final float factor = f/(float)Math.sqrt(sqLen);
            x *= factor;
            y *= factor;
            z *= factor;
        }
        return this;
    }
    
    /**
     * Sets this to the vector projection of this onto the given vector and
     * returns this.
     */
    public MutableVector3f vecProjLocal(Vector3f v)
    {
        final float factor = dot(v)/v.squareLength();
        x = v.x()*factor;
        y = v.y()*factor;
        z = v.z()*factor;
        return this;
    }
    
    /**
     * Sets this to the vector projection of this onto the given vector and
     * returns this. Assumes that the given vector is normalized. Fast.
     */
    public MutableVector3f vecProjUnitLocal(Vector3f v)
    {
        final float factor = dot(v);
        x = v.x()*factor;
        y = v.y()*factor;
        z = v.z()*factor;
        return this;
    }
    
    /**
     * Reflects this vector about the given unit vector and returns this.
     */
    public MutableVector3f reflectUnitLocal(Vector3f n)
    {
        final float m = 2f*dot(n);
        
        this.x = m*n.x() - x;
        this.y = m*n.y() - y;
        this.z = m*n.z() - z;
        return this;
    }
    
    /**
     * Refracts this vector about the given unit vector and returns this.
     * 
     * @param n The normal vector to refract about.
     * @param eta The ratio of two indices of refraction.
     */
    public MutableVector3f refractUnitLocal(Vector3f n, float eta)
    {
        final float dot = dot(n);
        final float k = 1.0f - eta*eta*(1.0f - dot*dot);
        
        if (k < 0.0f) return set();
        final float factor = (dot*eta + (float)Math.sqrt(k));
        
        x = x*eta - factor*n.x();
        y = y*eta - factor*n.y();
        z = z*eta - factor*n.z();
        return this;
    }
    // </editor-fold>
}
