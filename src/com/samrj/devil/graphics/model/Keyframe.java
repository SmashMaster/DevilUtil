package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector2f;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Keyframe
{
    public static enum Interpolation
    {
        CONSTANT, LINEAR, BEZIER;
    }
    
    /**
     * The total length of the handles is not allowed to be more
     * than the horizontal distance between left and right.
     */
    public static final void validate(Keyframe left, Keyframe right)
    {
        float width = right.coord.x - left.coord.x;
        
        Vector2f dHandleLeft = left.handleRight.csub(left.coord);
        Vector2f dHandleRight = right.handleLeft.csub(right.coord);
        
        float handleLeftLength = dHandleLeft.length();
        float handleRightLength = dHandleRight.length();
        float totalLength = handleLeftLength + handleRightLength;
        
        if (totalLength > width)
        {
            float adjFactor = width/totalLength;
            dHandleLeft.mult(adjFactor);
            dHandleRight.mult(adjFactor);
            
            left.handleRight.set(left.coord).add(dHandleLeft);
            right.handleLeft.set(right.coord).add(dHandleRight);
        }
    }
    
    public static List<Float> bezierT(float x0, float x1, float x2, float x3, float x)
    {
        float c3 = -x0 + 3.0f*(x1 - x2) + x3;
        float c2 = 3.0f*(x0 - 2.0f*x1 + x2);
        float c1 = 3.0f*(-x0 + x1);
        float c0 = x0 - x;

        List<Float> out = new ArrayList<>(3);

        if (c3 != 0.0f)
        {
            float a = c2 / c3;
            float b = c1 / c3;
            float c = c0 / c3;
            a = a / 3;

            float p = b / 3 - a * a;
            float q = (2 * a * a * a - a * b + c) / 2;
            float d = q * q + p * p * p;

            if (d > 0.0f)
            {
                float t = Util.sqrt(d);
                float o = Util.cbrt(-q + t) + Util.cbrt(-q - t) - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);
            }
            else if (d == 0.0f)
            {
                float t = Util.cbrt(-q);
                float o = 2 * t - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);

                o = -t - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);
            }
            else
            {
                //Oh god please why
                float phi = Util.acos(-q / Util.sqrt(-(p * p * p)));
                float t = Util.sqrt(-p);
                p = Util.cos(phi / 3);
                q = Util.sqrt(3 - 3 * p * p);
                float o = 2 * t * p - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);

                o = -t * (p + q) - a;
                if (o >= 0.0f && o <= 1.0f) out.add(o);

                o = -t * (p - q) - a;
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
                /* discriminant */
                float p = b * b - 4 * a * c;

                if (p > 0.0f)
                {
                    p = Util.sqrt(p);
                    float o = (-b - p) / (2 * a);
                    if (o >= 0.0f && o <= 1.0f) out.add(o);

                    o = (-b + p) / (2 * a);
                    if (o >= 0.0f && o <= 1.0f) out.add(o);
                }
                else if (p == 0.0f)
                {
                    float o = -b / (2 * a);
                    if (o >= 0.0f && o <= 1.0f) out.add(o);
                }
            }
            else if (b != 0.0f)
            {
                float o = -c / b;
                if (o >= 0.0f && o <= 1.0f) out.add(o);
            }
            else if (c == 0.0f) out.add(0.0f);
        }

        return out;
    }
    
    public static final float bezierY(float y0, float y1, float y2, float y3, float t)
    {
        float omt = 1.0f - t;
        return omt*(omt*omt*y0 + 3.0f*t*(omt*y1 + t*y2)) + t*t*t*y3;
    }
    
    public static final float bezier(Vector2f p0, Vector2f p1, Vector2f p2, Vector2f p3, float x)
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
    public static final float evaluate(Keyframe left, Keyframe right, float time)
    {
        if (time <= left.coord.x || left.interpolation == Interpolation.CONSTANT)
            return left.coord.y;
        if (time >= right.coord.x) return right.coord.y;
        
        if (left.interpolation == Interpolation.LINEAR)
            return left.coord.y + (right.coord.y - left.coord.y)*(time - left.coord.x)/(right.coord.x - left.coord.x);
        
        return bezier(left.coord, left.handleRight, right.handleLeft, right.coord, time);
    }
    
    /**
     * Interpolation affects the curve to the right of the keyframe. Therefore,
     * the last keyframe's interpolation type is meaningless.
     */
    public final Interpolation interpolation;
    public final Vector2f coord, handleLeft, handleRight;
    
    public Keyframe(DataInputStream in) throws IOException
    {
        switch (in.readInt())
        {
            case 0: interpolation = Interpolation.CONSTANT; break;
            case 1: interpolation = Interpolation.LINEAR; break;
            case 2: interpolation = Interpolation.BEZIER; break;
            default: throw new IllegalArgumentException();
        }
        
        coord = DevilModel.readVector2f(in);
        handleLeft = DevilModel.readVector2f(in);
        handleRight = DevilModel.readVector2f(in);
    }
}
