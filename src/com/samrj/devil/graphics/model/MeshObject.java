package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * DevilModel mesh object. A mesh object is an instance of a mesh which has a
 * position, scale, and rotation. Many objects can share the same mesh with
 * different positions.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class MeshObject
{
    public final String name;
    public final Mesh mesh;
    public final Vec3 scale;
    public final Quat rotation;
    public final Vec3 position;
    public final String[] groups;
    
    MeshObject(DataInputStream in, Mesh[] meshes) throws IOException
    {
        name = Model.readPaddedUTF(in);
        mesh = meshes[in.readInt()];
        scale = new Vec3(in);
        rotation = new Quat(in);
        position = new Vec3(in);
        int numGroups = in.readInt();
        groups = new String[numGroups];
        for (int i=0; i<numGroups; i++) groups[i] = Model.readPaddedUTF(in);
    }
}
