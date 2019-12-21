package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Material extends DataBlock
{
    public final int modelIndex;
    
    public final Vec3 diffuseColor;
    public final float specularIntensity;
    public final float roughness;
    public final float metallic;
    
    Material(Model model, int modelIndex, BlendFile.Pointer bMat) throws IOException
    {
        super(model, bMat);
        this.modelIndex = modelIndex;
        
        float r = bMat.getField("r").asFloat();
        float g = bMat.getField("g").asFloat();
        float b = bMat.getField("b").asFloat();
        
        diffuseColor = new Vec3(r, g, b);
        specularIntensity = bMat.getField("spec").asFloat();
        roughness = bMat.getField("roughness").asFloat();
        metallic = bMat.getField("metallic").asFloat();
    }
}
