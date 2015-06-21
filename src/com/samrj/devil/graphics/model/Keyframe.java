package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector2f;
import java.io.DataInputStream;
import java.io.IOException;

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
    
    public static float bezierT(float x0, float x1, float x2, float x3, float x)
    {
        float a = -x0 + 3.0f*(x1 - x2) + x3;
        float b = 3.0f*(x0 - 2.0f*x1 + x2);
        float c = 3.0f*(-x0 + x1);
        float d = x0 - x;
        
        float del0 = b*b - 3.0f*a*c;
        float del1 = 2.0f*b*b*b - 9.0f*a*b*c + 27.0f*a*a*d;
        
        float bigC = Util.cbrt((del1 + Util.sqrt(del1*del1 - 4.0f*del0*del0*del0))*0.5f);
        float root = -(b + bigC + del0/bigC)/(3.0f*a);
        
//        System.out.println(root);
        
        return root;
    }
    
    public static final float bezierY(float y0, float y1, float y2, float y3, float t)
    {
        float omt = 1.0f - t;
        return omt*(omt*omt*y0 + 3.0f*t*(omt*y1 + t*y2)) + t*t*t*y3;
    }
    
    public static final float bezier(Vector2f p0, Vector2f p1, Vector2f p2, Vector2f p3, float x)
    {
//        System.out.println(p0 + " " + p1 + " " + p2 + " " + p3);
        float t = bezierT(p0.x, p1.x, p2.x, p3.x, x);
        return    bezierY(p0.y, p1.y, p2.y, p3.y, t);
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
        
        float frac = (time - left.coord.x)/(right.coord.x - left.coord.x);
        if (left.interpolation == Interpolation.LINEAR)
            return left.coord.y + (right.coord.y - left.coord.y)*frac;
        
        return bezier(left.coord, left.handleRight, right.handleLeft, right.coord, frac);
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
