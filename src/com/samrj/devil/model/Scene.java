package com.samrj.devil.model;

import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Scene extends DataBlock
{
    public final ArrayMap<ModelObject<?>> objects;
    
    Scene(Model model, BlendFile.Pointer bScene) throws IOException
    {
        super(model, bScene);
        
        objects = new ArrayMap<>();
        for (BlendFile.Pointer base : bScene.getField("base").asList("Base"))
        {
            BlendFile.Pointer bObject = base.getField("object").dereference();
            objects.put(new ModelObject<>(model, this, bObject));
        }
    }
    
    @Override
    void destroy()
    {
        objects.destroy();
    }
}
