package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Lamp extends DataBlock
{
    public enum Type
    {
        POINT, SUN;
    }
    
    public final Vec3 color;
    public final Type type;
    public final float radius;
    
    Lamp(Model model, org.blender.dna.Lamp bLamp) throws IOException
    {
        super(model, bLamp.getId().getName().asString().substring(2));
        
        color = new Vec3(bLamp.getR(), bLamp.getG(), bLamp.getB()).mult(bLamp.getEnergy());
        type = Type.values()[bLamp.getType()];
        radius = type == Type.POINT ? bLamp.getDist() : -1.0f;
    }
}
