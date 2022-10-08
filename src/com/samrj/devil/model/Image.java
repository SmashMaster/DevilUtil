package com.samrj.devil.model;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Image extends DataBlock
{
    public final Path relativePath;
    public final Path path;

    Image(Model model, BlendFile.Pointer bImage) throws IOException
    {
        super(model, bImage);

        BlendFile.Pointer filepathPtr = bImage.getField("name"); //This field renamed to filepath in new versions of Blender?
        String filepath = filepathPtr != null ? filepathPtr.asString() : null;
        if (filepath != null && filepath.startsWith("//")) //Only support relative paths for now.
        {
            relativePath = Path.of(filepath.substring(2).replace('\\', '/')).normalize();
            path = model.path.getParent().resolve(relativePath).normalize();
        }
        else
        {
            relativePath = null;
            path = null;
        }
    }
}
