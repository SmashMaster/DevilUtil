package com.samrj.devil.math;

/**
 * Basic 2D integer vector class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vec2i
{
    public int x, y;
    
    public Vec2i()
    {
    }
    
    public Vec2i(int x, int y)
    {
        this.x = x; this.y = y;
    }
    
    public Vec2i(Vec2i v)
    {
        x = v.x; y = v.y;
    }
    
    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
}
