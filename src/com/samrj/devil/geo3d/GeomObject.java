package com.samrj.devil.geo3d;

import com.samrj.devil.geo3d.GeoMesh.Face;
import java.util.stream.Stream;

/**
 * Utility class for storing and reversing series of matrix multiplications.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface GeomObject
{
    public enum Type
    {
        VERTEX, EDGE, FACE, VIRTUAL;
    }
    
    public Type getType();
    public Stream<Face> getFaces();
}
