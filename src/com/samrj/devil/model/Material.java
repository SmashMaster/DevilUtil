package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.model.nodes.NodesToGLSL;

import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Material extends DataBlockAnimatable
{
    public final int modelIndex;
    
    public final Vec3 baseColor;
    public final float metallic;
    public final float specular;
    public final float roughness;

    public final NodesToGLSL glsl;
    
    Material(Model model, int modelIndex, BlendFile.Pointer bMat) throws IOException
    {
        super(model, bMat);
        this.modelIndex = modelIndex;

        //Default to legacy values if "Use Nodes" not enabled for this material.
        float r = bMat.getField("r").asFloat();
        float g = bMat.getField("g").asFloat();
        float b = bMat.getField("b").asFloat();

        baseColor = new Vec3(r, g, b);
        metallic = bMat.getField("metallic").asFloat();
        specular = bMat.getField("spec").asFloat();
        roughness = bMat.getField("roughness").asFloat();

        glsl = NodesToGLSL.of(bMat);
    }
}
