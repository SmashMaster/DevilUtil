package com.samrj.devil.geo3d;

import com.samrj.devil.geo3d.GeoMesh.Face;
import java.util.stream.Stream;

public interface GeomObject
{
    public enum Type
    {
        VERTEX, EDGE, FACE, VIRTUAL;
    }
    
    public Type getType();
    public Stream<Face> getFaces();
}
