package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Material extends DataBlock
{
    public final Vec3 diffuseColor, specularColor;
    public final float specularHardness;
    public final float specularIOR;
    public final float emit;
    
    public final List<TextureSlot> textures;
    
    Material(Model model, DataInputStream in) throws IOException
    {
        super(model, in);
        
        diffuseColor = new Vec3(in);
        specularColor = new Vec3(in);
        specularHardness = in.readFloat();
        specularIOR = in.readFloat();
        emit = in.readFloat();
        
        int numTextures = in.readInt();
        ArrayList<TextureSlot> texes = new ArrayList<>(numTextures);
        for (int i=0; i<numTextures; i++) texes.add(new TextureSlot(model, in));
        textures = Collections.unmodifiableList(texes);
    }
}
