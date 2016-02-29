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

package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Vec2;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Samuel Johnson (SmashMaster)
 */
public class FCurve
{
    public static enum Interpolation
    {
        CONSTANT, LINEAR, BEZIER;
    }
    
    /**
     * The total length of the handles is not allowed to be more
     * than the horizontal distance between left and right.
     */
    private static void validate(Keyframe keyLeft, Keyframe keyRight)
    {
        float width = keyRight.co.x - keyLeft.co.x;
        
        Vec2 dHandleLeft = Vec2.sub(keyLeft.right, keyLeft.co);
        Vec2 dHandleRight = Vec2.sub(keyRight.left, keyRight.co);
        
        float leftLength = dHandleLeft.length();
        float rightLength = dHandleRight.length();
        float totalLength = leftLength + rightLength;
        
        if (totalLength > width)
        {
            float adjFactor = width/totalLength;
            dHandleLeft.mult(adjFactor);
            dHandleRight.mult(adjFactor);
            
            keyLeft.right.set(keyLeft.co).add(dHandleLeft);
            keyRight.left.set(keyRight.co).add(dHandleRight);
        }
    }
    
    private static List<Float> bezierT(float x0, float x1, float x2, float x3, float x)
    {
        float c3 = -x0 + 3.0f*(x1 - x2) + x3;
        float c2 = 3.0f*(x0 - 2.0f*x1 + x2);
        float c1 = 3.0f*(-x0 + x1);
        float c0 = x0 - x;

        List<Float> out = new ArrayList<>(3);

        if (c3 != 0.0f)
        {
            float a = c2/c3;
            float b = c1/c3;
            float c = c0/c3;
            a = a/3;

            float p = b/3.0f - a*a;
            float q = (2*a*a*a - a*b + c)/2.0f;
            float d = q*q + p*p*p;

            if (d > 0.0f)
            {
                float t = (float)Math.sqrt(d);
                float o = (float)Math.cbrt(-q + t) + (float)Math.cbrt(-q - t) - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);
            }
            else if (d == 0.0f)
            {
                float t = (float)Math.cbrt(-q);
                float o = 2*t - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);

                o = -t - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);
            }
            else
            {
                //Oh god please why
                float phi = (float)Math.acos(-q / (float)Math.sqrt(-(p*p*p)));
                float t = (float)Math.sqrt(-p);
                p = (float)Math.cos(phi/3.0f);
                q = (float)Math.sqrt(3.0f - 3.0f*p*p);
                float o = 2.0f*t*p - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);

                o = -t*(p + q) - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);

                o = -t*(p - q) - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);
            }
        }
        else
        {
            float a = c2;
            float b = c1;
            float c = c0;

            if (a != 0.0f)
            {
                float p = b*b - 4.0f*a*c; //Discriminant

                if (p > 0.0f)
                {
                    p = (float)Math.sqrt(p);
                    float o = (-b - p)/(2.0f*a);
                    if (o >= 0.0f && o <= 1.0f) out.add(o);

                    o = (-b + p)/(2.0f*a);
                    if (o >= 0.0f && o <= 1.0f) out.add(o);
                }
                else if (p == 0.0f)
                {
                    float o = -b/(2.0f*a);
                    if (o >= 0.0f && o <= 1.0f) out.add(o);
                }
            }
            else if (b != 0.0f)
            {
                float o = -c/b;
                if (o >= 0.0f && o <= 1.0f) out.add(o);
            }
            else if (c == 0.0f) out.add(0.0f);
        }

        return out;
    }
    
    private static float bezierY(float y0, float y1, float y2, float y3, float t)
    {
        float omt = 1.0f - t;
        return omt*(omt*omt*y0 + 3.0f*t*(omt*y1 + t*y2)) + t*t*t*y3;
    }
    
    private static float bezier(Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3, float x)
    {
        List<Float> tSolutions = bezierT(p0.x, p1.x, p2.x, p3.x, x);
        float t;
        if (tSolutions.isEmpty()) t = (x - p0.x)/(p3.x - p0.x); //Revert to lerp
        else t = tSolutions.get(0);
        
        return bezierY(p0.y, p1.y, p2.y, p3.y, t);
    }
    
    /**
     * See the following source code for a correct implementation of bezier FCurve keyframes:
     * 
     * https://svn.blender.org/svnroot/bf-blender/trunk/blender/source/blender/blenkernel/intern/fcurve.c
     */
    private static float evaluate(Keyframe left, Keyframe right, float time)
    {
        if (time <= left.co.x || left.interpolation == Interpolation.CONSTANT)
            return left.co.y;
        if (time >= right.co.x) return right.co.y;
        
        if (left.interpolation == Interpolation.LINEAR)
            return left.co.y + (right.co.y - left.co.y)*(time - left.co.x)/(right.co.x - left.co.x);
        
        return bezier(left.co, left.right, right.left, right.co, time);
    }
    
    public final String boneName;
    public final Transform.Property property;
    public final int propertyIndex;
    public final Keyframe[] keyframes;
    public final float minX, maxX;
    private final TreeMap<Float, Integer> keyInds;
    
    FCurve(DataInputStream in) throws IOException
    {
        boolean hasBone = in.readShort() != 0;
        property = Transform.propFromID(in.readShort());
        boneName = hasBone ? IOUtil.readPaddedUTF(in) : null;
        propertyIndex = in.readInt();
        keyframes = IOUtil.arrayFromStream(in, Keyframe.class, Keyframe::new);
        
        float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
        keyInds = new TreeMap<>();
        for (int i=0; i<keyframes.length; i++)
        {
            float x = keyframes[i].co.x;
            if (x < min) min = x;
            if (x > max) max = x;
            keyInds.put(x, i);
        }
        minX = min; maxX = max;
        
        for (int i=0; i<keyframes.length - 1; i++)
            validate(keyframes[i], keyframes[i + 1]);
    }
    
    public float evaluate(float time)
    {
        Entry<Float, Integer> e0 = keyInds.floorEntry(time);
        if (e0 == null) return keyframes[0].co.y; //Before first
        
        int i0 = e0.getValue();
        Keyframe k0 = keyframes[i0];
        if (i0 == keyframes.length - 1) return k0.co.y; //After last
        
        Keyframe k1 = keyframes[i0 + 1];
        return evaluate(k0, k1, time);
    }
    
    public void apply(Pose pose, float time)
    {
        pose.setProperty(boneName, property, propertyIndex, evaluate(time));
    }
    
    public class Keyframe
    {
        public final Interpolation interpolation;
        public final Vec2 co, left, right;
        
        Keyframe(DataInputStream in) throws IOException
        {
            switch (in.readInt())
            {
                case 0: interpolation = Interpolation.CONSTANT; break;
                case 1: interpolation = Interpolation.LINEAR; break;
                case 2: interpolation = Interpolation.BEZIER; break;
                default: interpolation = Interpolation.LINEAR;
            }
            
            co = new Vec2(in);
            left = new Vec2(in);
            right = new Vec2(in);
        }
    }
}
