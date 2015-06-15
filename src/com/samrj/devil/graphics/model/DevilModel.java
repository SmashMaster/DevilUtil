package com.samrj.devil.graphics.model;

import com.samrj.devil.res.FileRes;
import com.samrj.devil.res.Resource;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class DevilModel
{
    public static enum Type
    {
        STATIC("ST"), MULTITEXTURED("MT"), ANIMATED("AN");
        
        public final String id;
        
        private Type(String identifier)
        {
            id = identifier;
        }
    }
    
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
    
    public final Mesh[] meshes;
    
    public DevilModel(InputStream inputStream) throws IOException
    {
        try
        {
            DataInputStream in = new DataInputStream(inputStream);
            if (!in.readUTF().equals("DevilModel")) throw new IOException("Illegal file format specified.");
            if (!in.readUTF().equals(Type.STATIC.id)) throw new IOException("I haven't programmed that yet.");

            int numMeshes = in.readInt();
            meshes = new Mesh[numMeshes];
            for (int i=0; i<numMeshes; i++)
                meshes[i] = new Mesh(in);
        }
        finally
        {
            inputStream.close();
        }
    }
    
    public DevilModel(Resource path) throws IOException
    {
        this(path.open());
    }
    
    public DevilModel(File f) throws IOException
    {
        this(FileRes.find(f));
    }
    
    public DevilModel(String path) throws IOException
    {
        this(Resource.find(path));
    }
}
