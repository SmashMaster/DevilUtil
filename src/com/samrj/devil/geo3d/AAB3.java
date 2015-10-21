/*
 * Copyright (c) 2015 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * 3D axis-aligned box.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class AAB3
{
    public static final float radius(AAB3 aab)
    {
        float sx = (aab.x1 - aab.x0)*0.5f;
        float sy = (aab.y1 - aab.y0)*0.5f;
        float sz = (aab.z1 - aab.z0)*0.5f;
        
        return (float)Math.sqrt(sx*sx + sy*sy + sz*sz);
    }
    
    public static final void size(AAB3 aab, Vec3 result)
    {
        result.x = aab.x1 - aab.x0;
        result.y = aab.y1 - aab.y0;
        result.z = aab.z1 - aab.z0;
    }
    
    public static final void center(AAB3 aab, Vec3 result)
    {
        result.x = (aab.x1 + aab.x0)*0.5f;
        result.y = (aab.y1 + aab.y0)*0.5f;
        result.z = (aab.z1 + aab.z0)*0.5f;
    }
    
    public static final void empty(AAB3 result)
    {
        result.x0 = Float.POSITIVE_INFINITY; result.x1 = Float.NEGATIVE_INFINITY;
        result.y0 = Float.POSITIVE_INFINITY; result.y1 = Float.NEGATIVE_INFINITY;
        result.z0 = Float.POSITIVE_INFINITY; result.z1 = Float.NEGATIVE_INFINITY;
    }
    
    public static final void expand(AAB3 aab, Vec3 v, AAB3 result)
    {
        result.x0 = v.x < aab.x0 ? v.x : aab.x0;
        result.x1 = v.x > aab.x1 ? v.x : aab.x1;
        result.y0 = v.y < aab.y0 ? v.y : aab.y0;
        result.y1 = v.y > aab.y1 ? v.y : aab.y1;
        result.z0 = v.z < aab.z0 ? v.z : aab.z0;
        result.z1 = v.z > aab.z1 ? v.z : aab.z1;
    }
    
    public static final Vec3 size(AAB3 aab)
    {
        Vec3 result = new Vec3();
        size(aab, result);
        return result;
    }
    
    public static final Vec3 center(AAB3 aab)
    {
        Vec3 result = new Vec3();
        center(aab, result);
        return result;
    }
    
    public static final AAB3 empty()
    {
        AAB3 result = new AAB3();
        empty(result);
        return result;
    }
    
    public static final AAB3 expand(AAB3 aab, Vec3 v)
    {
        AAB3 result = new AAB3();
        expand(aab, v, result);
        return result;
    }
    
    public float x0, x1;
    public float y0, y1;
    public float z0, z1;
    
    public AAB3()
    {
    }
    
    public AAB3(float x0, float x1, float y0, float y1, float z0, float z1)
    {
        this.x0 = x0; this.x1 = x1;
        this.y0 = y0; this.y1 = y1;
        this.z0 = z0; this.z1 = z1;
    }
    
    public AAB3(AAB3 aab)
    {
        x0 = aab.x0; x1 = aab.x1;
        y0 = aab.y0; y1 = aab.y1;
        z0 = aab.z0; z1 = aab.z1;
    }
    
    public float radius()
    {
        return radius(this);
    }
    
    public Vec3 size()
    {
        return size(this);
    }
    
    public Vec3 center()
    {
        return center(this);
    }
    
    public AAB3 setEmpty()
    {
        empty(this);
        return this;
    }
    
    public AAB3 expand(Vec3 v)
    {
        expand(this, v, this);
        return this;
    }
}
