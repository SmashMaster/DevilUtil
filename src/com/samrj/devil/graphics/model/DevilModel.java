package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.res.Resource;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * .DVM file loader. Corresponds with the Blender python exporter.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DevilModel
{
    public static final String readPaddedUTF(DataInputStream in) throws IOException
    {
        if (!in.markSupported()) throw new IOException("Cannot read padded UTF-8 on this platform.");
        in.mark(8);
        int utflen = in.readUnsignedShort() + 2;
        in.reset();
        String out = in.readUTF();
        int padding = (4 - (utflen % 4)) % 4;
        if (in.skipBytes(padding) != padding) throw new IOException("Cannot skip bytes properly on this platform.");
        return out;
    }
    
    public final Vec3 backgroundColor;
    public final Mesh[] meshes;
    public final MeshObject[] meshObjects;
    public final SunLamp[] sunLamps;
    
    DevilModel(BufferedInputStream inputStream) throws IOException
    {
        try
        {
            DataInputStream in = new DataInputStream(inputStream);
            if (!in.readUTF().equals("DevilModel 0.3"))
                throw new IOException("Illegal file format specified.");

            backgroundColor = new Vec3();
            backgroundColor.read(in);
            
            int numMeshes = in.readInt();
            meshes = new Mesh[numMeshes];
            for (int i=0; i<numMeshes; i++)
                meshes[i] = new Mesh(in);
            
            int numMeshObjects = in.readInt();
            meshObjects = new MeshObject[numMeshObjects];
            for (int i=0; i<numMeshes; i++)
                meshObjects[i] = new MeshObject(in, meshes);
            
            int numSunLamps = in.readInt();
            sunLamps = new SunLamp[numSunLamps];
            for (int i=0; i<numSunLamps; i++)
                sunLamps[i] = new SunLamp(in);
        }
        finally
        {
            inputStream.close();
        }
    }
    
    public DevilModel(String path) throws IOException
    {
        this(new BufferedInputStream(Resource.open(path)));
    }
    
    //Slow but simple. Could add pre-computed hash comparison for extra speed,
    //or give each array a hash map.
    
    public Mesh getMesh(String name)
    {
        for (Mesh mesh : meshes) if (mesh.name.equals(name)) return mesh;
        return null;
    }
    
    public MeshObject getMeshObject(String name)
    {
        for (MeshObject obj : meshObjects) if (obj.name.equals(name)) return obj;
        return null;
    }
    
    public SunLamp getSunLamp(String name)
    {
        for (SunLamp lamp : sunLamps) if (lamp.name.equals(name)) return lamp;
        return null;
    }
    
    /**
     * Releases any system resources (native memory) associated with this model.
     */
    public void destroy()
    {
        for (Mesh mesh : meshes) mesh.destroy();
        Arrays.fill(meshes, null);
    }
}
