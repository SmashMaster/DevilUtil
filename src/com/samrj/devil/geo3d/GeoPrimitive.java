package com.samrj.devil.geo3d;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface GeoPrimitive
{
    public enum Type
    {
        VERTEX, EDGE, TRIANGLE, VIRTUAL;
    }
    
    public Type getType();
}
