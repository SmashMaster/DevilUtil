package com.samrj.devil.config.field;

import com.samrj.devil.config.CfgField;

/**
 * Configuration field for resolution values--a pair of integer values separated
 * by a comma.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CfgResolution implements CfgField
{
    public int width, height;
    public float aspectRatio;
    
    public CfgResolution(int width, int height)
    {
        this.width = width; this.height = height;
        aspectRatio = (float)height/width;
    }

    @Override
    public void load(String in)
    {
        String[] pair = in.split(",");
        if (pair.length != 2) return;

        width = Integer.parseInt(pair[0]);
        height = Integer.parseInt(pair[1]);
        aspectRatio = (float)height/width;
    }
    
    @Override
    public CfgField copy()
    {
        return new CfgResolution(width, height);
    }
}
