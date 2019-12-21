package com.samrj.devil.model;

import java.io.IOException;
import java.util.ArrayDeque;

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
        
        ArrayDeque<BlendFile.Pointer> collectionStack = new ArrayDeque<>();
        collectionStack.push(bScene.getField("master_collection").dereference());
        
        while (!collectionStack.isEmpty())
        {
            BlendFile.Pointer collection = collectionStack.pop();
            
            for (BlendFile.Pointer cObj : collection.getField("gobject").asList("CollectionObject"))
            {
                BlendFile.Pointer bObject = cObj.getField("ob").dereference();
                String objName = bObject.getField(0).getField("name").asString().substring(2);
                objects.put(model.objects.require(objName));
            }
            
            for (BlendFile.Pointer child : collection.getField("children").asList("CollectionChild"))
                collectionStack.push(child.getField("collection").dereference());
        }
    }
    
    @Override
    void destroy()
    {
        objects.destroy();
    }
}
