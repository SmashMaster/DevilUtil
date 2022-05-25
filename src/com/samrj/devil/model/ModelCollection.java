package com.samrj.devil.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ModelCollection extends DataBlock
{
    public final ArrayMap<ModelObject<?>> objects = new ArrayMap<>();
    public final List<DataPointer<ModelCollection>> children = new ArrayList<>();

    ModelCollection(Model model, BlendFile.Pointer bCollection) throws IOException
    {
        super(model, bCollection);

        for (BlendFile.Pointer cObj : bCollection.getField("gobject").asList("CollectionObject"))
        {
            BlendFile.Pointer bObject = cObj.getField("ob").dereference();
            String objName = bObject.getField(0).getField("name").asString().substring(2);
            objects.put(model.objects.require(objName));
        }

        for (BlendFile.Pointer bCollectionChild : bCollection.getField("children").asList("CollectionChild"))
        {
            BlendFile.Pointer childPtr = bCollectionChild.getField("collection").dereference();
            String childName = childPtr.getField(0).getField("name").asString().substring(2);
            children.add(new DataPointer<>(model, Type.COLLECTION, childName));
        }
    }

    /**
     * Returns a stream of all objects in this collection's hierarchy.
     */
    public Stream<ModelObject<?>> objectStream()
    {
        return Stream.concat(objects.stream(), children.stream().map(DataPointer::get).flatMap(ModelCollection::objectStream));
    }
}
