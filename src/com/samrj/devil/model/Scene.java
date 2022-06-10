package com.samrj.devil.model;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Scene extends DataBlockAnimatable
{
    public final ModelCollection collection;
    
    Scene(Model model, BlendFile.Pointer bScene) throws IOException
    {
        super(model, bScene);

        BlendFile.Pointer masterCollection = bScene.getField("master_collection").dereference();
        String masterName = masterCollection.getField(0).getField("name").asString().substring(2);
        collection = model.collections.require(masterName);
    }

    /**
     * Returns a stream of all objects in this scene's hierarchy.
     */
    public Stream<ModelObject<?>> objectStream()
    {
        return collection.objectStream();
    }
}
