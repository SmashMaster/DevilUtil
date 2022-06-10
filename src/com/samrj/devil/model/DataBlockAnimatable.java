package com.samrj.devil.model;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class DataBlockAnimatable extends DataBlock
{
    public final AnimationData animationData;

    DataBlockAnimatable(Model model, BlendFile.Pointer pointer)
    {
        super(model, pointer);

        BlendFile.Pointer adtPtr = pointer.getField("adt").dereference();
        animationData = adtPtr != null ? new AnimationData(model, adtPtr) : null;
    }
}
