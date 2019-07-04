package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.blender.dna.MTex;
import org.cakelab.blender.nio.CPointer;

public final class Material extends DataBlock
{
    public final int modelIndex;
    
    public final Vec3 diffuseColor, specularColor;
    public final float specularHardness;
    public final float specularIOR;
    public final float emit;
    
    public final List<TextureSlot> textures;
    
    Material(Model model, int modelIndex, org.blender.dna.Material bMat) throws IOException
    {
        super(model, bMat.getId());
        this.modelIndex = modelIndex;
        
        diffuseColor = new Vec3(bMat.getR(), bMat.getG(), bMat.getB()).mult(bMat.getRef());
        specularColor = new Vec3(bMat.getSpecr(), bMat.getSpecg(), bMat.getSpecb()).mult(bMat.getSpec());
        specularHardness = bMat.getHar();
        specularIOR = bMat.getRefrac();
        emit = bMat.getEmit();
        
        textures = new ArrayList<>();
        CPointer<MTex>[] mTexs = bMat.getMtex().toArray();
        for (int i=0; i<18; i++) //MAX_MTEX is 18 in blender
        {
            MTex mTex = mTexs[i].get();
            if (mTex == null) break;
            
            textures.add(new TextureSlot(mTex));
        }
    }
    
    public class TextureSlot
    {
        public final DataPointer<Texture> texture;
        public final float diffuseFactor;
        public final float emitFactor;
        public final float specularFactor;
        public final float normalFactor;

        TextureSlot(MTex mTex) throws IOException
        {
            String texName = mTex.getTex().get().getId().getName().asString().substring(2);
            
            //Defined in super old versions of DNA_material_types.h in Blender's source code
            int mapMask = mTex.getMapto();
            boolean mapToColor = (mapMask & 1) != 0;
            boolean mapToEmit = (mapMask & 64) != 0;
            boolean mapToSpec = (mapMask & 32) != 0;
            boolean mapToNormal = (mapMask & 2) != 0;
            
            texture = new DataPointer(model, Type.TEXTURE, texName);
            diffuseFactor = mapToColor ? mTex.getColfac() : 0.0f;
            emitFactor = mapToEmit ? mTex.getEmitfac() : 0.0f;
            specularFactor = mapToSpec ? mTex.getSpecfac() : 0.0f;
            normalFactor = mapToNormal ? mTex.getNorfac() : 0.0f;
        }
    }
}
