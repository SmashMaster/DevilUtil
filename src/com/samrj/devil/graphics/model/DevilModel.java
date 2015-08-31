package com.samrj.devil.graphics.model;

import com.samrj.devil.io.Memory;
import com.samrj.devil.res.Resource;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    
    public final Armature armature;
    public final Action[] actions;
    public final Mesh[] meshes;
    
    private final Map<String, Integer> actionIndices, meshIndices;
    
    public DevilModel(InputStream inputStream, Memory memory) throws IOException
    {
        if (memory == null) throw new NullPointerException();
        
        try
        {
            DataInputStream in = new DataInputStream(inputStream);
            if (!in.readUTF().equals("DevilModel")) throw new IOException("Illegal file format specified.");

            boolean hasArmature = in.readInt() != 0;
            if (hasArmature)
            {
                armature = new Armature(in, memory);
                
                int numActions = in.readInt();
                actions = new Action[numActions];
                actionIndices = new HashMap<>(numActions);
                for (int i=0; i<numActions; i++)
                {
                    actions[i] = new Action(in);
                    actionIndices.put(actions[i].name, i);
                }
            }
            else
            {
                armature = null;
                actions = null;
                actionIndices = null;
            }
            
            boolean hasTangents = in.readInt() != 0;
            
            int numMeshes = in.readInt();
            meshes = new Mesh[numMeshes];
            meshIndices = new HashMap<>(numMeshes);
            for (int i=0; i<numMeshes; i++)
            {
                meshes[i] = new Mesh(in, memory, armature, hasTangents);
                meshIndices.put(meshes[i].name, i);
            }
        }
        finally
        {
            inputStream.close();
        }
    }
    
    public DevilModel(String path, Memory memory) throws IOException
    {
        this(Resource.open(path), memory);
    }
    
    public Mesh getMesh(String name)
    {
        int i = meshIndices.get(name);
        return meshes[i];
    }
    
    public Action getAction(String name)
    {
        int i = actionIndices.get(name);
        return actions[i];
    }
    
    /**
     * Releases any system resources (native memory) associated with this model.
     */
    public void destroy()
    {
        if (armature != null)
        {
            armature.destroy();
            Arrays.fill(actions, null);
            actionIndices.clear();
        }
        
        for (Mesh mesh : meshes) mesh.destroy();
        Arrays.fill(meshes, null);
        meshIndices.clear();
    }
}
