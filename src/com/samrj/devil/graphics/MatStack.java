package com.samrj.devil.graphics;

import com.samrj.devil.math.Mat4;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Utility class for storing and reversing series of matrix multiplications.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class MatStack
{
    public final Mat4 mat;
    private final Deque<Mat4> stack;
    
    public MatStack()
    {
        mat = Mat4.identity();
        stack = new LinkedList<>();
    }
    
    public MatStack identity()
    {
        mat.setIdentity();
        return this;
    }
    
    public MatStack push()
    {
        stack.push(new Mat4(mat));
        return this;
    }
    
    public MatStack pop()
    {
        Mat4.copy(stack.pop(), mat);
        return this;
    }
}
