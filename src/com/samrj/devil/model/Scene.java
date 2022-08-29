package com.samrj.devil.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Scene extends DataBlockAnimatable
{
    public final List<ViewLayer> viewLayers;
    
    Scene(Model model, BlendFile.Pointer bScene) throws IOException
    {
        super(model, bScene);

        ArrayList viewLayersList = new ArrayList<>();
        for (BlendFile.Pointer bViewLayer : bScene.getField("view_layers").asList("ViewLayer"))
            viewLayersList.add(new ViewLayer(bViewLayer));
        viewLayers = Collections.unmodifiableList(viewLayersList);
    }

    public Stream<ModelObject<?>> objectStream()
    {
        return viewLayers.get(0).objects.stream().map(DataPointer::get);
    }

    public final class ViewLayer
    {
        public final String name;
        public final List<DataPointer<ModelObject>> objects;

        private ViewLayer(BlendFile.Pointer bViewLayer)
        {
            name = bViewLayer.getField("name").asString();

            ArrayList<DataPointer<ModelObject>> objectsList = new ArrayList<>();

            BlendFile.Pointer objList = bViewLayer.getField("object_bases");
            if (objList != null) for (BlendFile.Pointer bObjectBase : objList.asList("Base"))
            {
                BlendFile.Pointer bObj = bObjectBase.getField("object").dereference();
                String objName = bObj.getField(0).getField("name").asString().substring(2);
                objectsList.add(new DataPointer<>(model, Type.OBJECT, objName));
            }

            objects = Collections.unmodifiableList(objectsList);
        }

        @Override
        public String toString()
        {
            return name + ":(" + objects.size() + " objects)";
        }
    }
}
