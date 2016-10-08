package com.samrj.devil.model;

import com.samrj.devil.model.DataBlock.Type;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class TextureSlot
{
    public final DataPointer<Texture> texture;
    public final float diffuseFactor;
    public final float emitFactor;
    public final float specularFactor;
    public final float normalFactor;
    
    TextureSlot(Model model, DataInputStream in) throws IOException
    {
        texture = new DataPointer(model, Type.TEXTURE, in.readInt());
        diffuseFactor = in.readFloat();
        emitFactor = in.readFloat();
        specularFactor = in.readFloat();
        normalFactor = in.readFloat();
    }
}
