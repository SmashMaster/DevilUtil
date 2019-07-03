package com.samrj.devil.model;

import java.io.IOException;
import org.blender.dna.Base;
import org.blender.dna.BlenderObject;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Scene extends DataBlock
{
    public final ArrayMap<ModelObject<?>> objects;
    
    Scene(Model model, org.blender.dna.Scene bScene) throws IOException
    {
        super(model, bScene.getId());
        
        objects = new ArrayMap<>();
        for (Base base : Blender.list(bScene.getBase(), Base.class))
        {
            BlenderObject bObject = base.getObject().get();
            objects.put(new ModelObject<>(model, this, bObject));
        }
    }
    
    @Override
    void destroy()
    {
        objects.destroy();
    }
}
