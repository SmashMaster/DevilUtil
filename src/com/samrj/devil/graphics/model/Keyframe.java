package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Vector2f;
import java.io.DataInputStream;
import java.io.IOException;

public class Keyframe
{
    /**
     * When evaluating Bezier curves, keyframe handles need to be adjusted so that
     * no loops are created. See the following source code for details:
     * 
     * https://svn.blender.org/svnroot/bf-blender/trunk/blender/source/blender/blenkernel/intern/fcurve.c
     */
    public static final float evaluate(Keyframe left, Keyframe right, float time)
    {
        throw new UnsupportedOperationException();
    }
    
    public static enum Interpolation
    {
        CONSTANT, LINEAR, BEZIER;
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
