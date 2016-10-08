package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

public final class Material extends DataBlock
{
    public final Vec3 diffuseColor, specularColor;
    public final float specularHardness;
    public final float specularIOR;
    public final float emit;
    
    public final TextureSlot[] textures;
    
    Material(Model model, DataInputStream in) throws IOException
    {
        super(model, in);
        
        diffuseColor = new Vec3(in);
        specularColor = new Vec3(in);
        specularHardness = in.readFloat();
        specularIOR = in.readFloat();
        emit = in.readFloat();
        
        textures = new TextureSlot[in.readInt()];
        for (int i=0; i<textures.length; i++)
            textures[i] = new TextureSlot(model, in);
    }
}
