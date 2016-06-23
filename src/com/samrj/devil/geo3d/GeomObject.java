package com.samrj.devil.geo3d;

public interface GeomObject
{
    public enum Type
    {
        VERTEX, EDGE, FACE;
    }
    
    public Type getType();
}
