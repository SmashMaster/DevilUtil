package com.samrj.devil.model;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class AnimationData
{
    public final DataPointer<Action> action;

    AnimationData(Model model, BlendFile.Pointer pointer)
    {
        BlendFile.Pointer bAction = pointer.getField("action").dereference();
        String actionName = bAction != null ? bAction.getField(0).getField("name").asString().substring(2) : null;
        action = new DataPointer<>(model, DataBlock.Type.ACTION, actionName);
    }
}
