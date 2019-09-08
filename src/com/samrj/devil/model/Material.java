package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Material extends DataBlock
{
    public final int modelIndex;
    
    public final Vec3 diffuseColor, specularColor;
    public final float specularHardness;
    public final float specularIOR;
    public final float emit;
    
    public final List<TextureSlot> textures;
    
    Material(Model model, int modelIndex, BlendFile.Pointer bMat) throws IOException
    {
        super(model, bMat);
        this.modelIndex = modelIndex;
        
        float r = bMat.getField("r").asFloat();
        float g = bMat.getField("g").asFloat();
        float b = bMat.getField("b").asFloat();
        float ref = bMat.getField("ref").asFloat();
        float specr = bMat.getField("specr").asFloat();
        float specg = bMat.getField("specg").asFloat();
        float specb = bMat.getField("specb").asFloat();
        float spec = bMat.getField("spec").asFloat();
        
        diffuseColor = new Vec3(r, g, b).mult(ref);
        specularColor = new Vec3(specr, specg, specb).mult(spec);
        specularHardness = bMat.getField("har").asShort();
        specularIOR = bMat.getField("refrac").asFloat();
        emit = bMat.getField("emit").asFloat();
        
        textures = new ArrayList<>();
        BlendFile.Pointer mtex = bMat.getField("mtex"); //MAX_MTEX is 18 in blender
        for (int i=0; i<18; i++)
        {
            BlendFile.Pointer mTex = mtex.add(mtex.getAddressSize()*i).dereference();
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

        TextureSlot(BlendFile.Pointer mTex) throws IOException
        {
            String texName = mTex.getField("tex").dereference().getField(0).getField("name").asString().substring(2);
            
            //Defined in super old versions of DNA_material_types.h in Blender's source code
            int mapMask = mTex.getField("mapto").asShort();
            boolean mapToColor = (mapMask & 1) != 0;
            boolean mapToEmit = (mapMask & 64) != 0;
            boolean mapToSpec = (mapMask & 32) != 0;
            boolean mapToNormal = (mapMask & 2) != 0;
            
            texture = new DataPointer(model, Type.TEXTURE, texName);
            diffuseFactor = mapToColor ? mTex.getField("colfac").asFloat() : 0.0f;
            emitFactor = mapToEmit ? mTex.getField("emitfac").asFloat() : 0.0f;
            specularFactor = mapToSpec ? mTex.getField("specfac").asFloat() : 0.0f;
            normalFactor = mapToNormal ? mTex.getField("norfac").asFloat() : 0.0f;
        }
    }
}
