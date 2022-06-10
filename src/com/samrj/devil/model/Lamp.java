package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;

import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Lamp extends DataBlockAnimatable
{
    public enum Type
    {
        POINT, SUN;
    }
    
    public final Vec3 color;
    public final Type type;
    public final float radius;
    
    Lamp(Model model, BlendFile.Pointer bLamp) throws IOException
    {
        super(model, bLamp);
        
        float r = bLamp.getField("r").asFloat();
        float g = bLamp.getField("g").asFloat();
        float b = bLamp.getField("b").asFloat();
        float energy = bLamp.getField("energy").asFloat();
        
        color = new Vec3(r, g, b).mult(energy);
        type = Type.values()[bLamp.getField("type").asShort()];
        radius = type == Type.POINT ? bLamp.getField("dist").asFloat() : -1.0f;
    }
}
