package com.samrj.devil.graphics.mesh;

import com.samrj.devil.res.FileRes;
import com.samrj.devil.res.Resource;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Scene
{
    public final Mesh[] meshes;
    
    public Scene(InputStream inputStream) throws IOException
    {
        DataInputStream in = new DataInputStream(inputStream);
        String header = in.readUTF();
        if (!header.equals("DVLMDL")) throw new IOException("Illegal file format specified.");
        
        int numMeshes = in.readInt();
        meshes = new Mesh[numMeshes];
        for (int i=0; i<numMeshes; i++)
            meshes[i] = new Mesh(in);
        
        inputStream.close();
    }
    
    public Scene(Resource path) throws IOException
    {
        this(path.open());
    }
    
    public Scene(File f) throws IOException
    {
        this(FileRes.find(f));
    }
    
    public Scene(String path) throws IOException
    {
        this(Resource.find(path));
    }
}
