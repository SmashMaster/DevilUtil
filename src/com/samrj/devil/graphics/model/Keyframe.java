package com.samrj.devil.graphics.model;

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
    
    /**
     * See the following source code for a correct implementation of bezier FCurve keyframes:
     * 
     * https://svn.blender.org/svnroot/bf-blender/trunk/blender/source/blender/blenkernel/intern/fcurve.c
     */
    public static final float evaluate(Keyframe left, Keyframe right, float time)
    {
        if (time <= left.coord.x) return left.coord.y;
        if (time >= right.coord.x ||
                left.interpolation == Interpolation.CONSTANT) return right.coord.y;
        
        float frac = (time - left.coord.x)/(right.coord.x - left.coord.x);
        
        if (left.interpolation == Interpolation.LINEAR)
            return left.coord.y + (right.coord.y - left.coord.y)*frac;
        
        //Now calculate bezier curve.
        throw new UnsupportedOperationException();
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
