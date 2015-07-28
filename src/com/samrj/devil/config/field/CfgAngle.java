package com.samrj.devil.config.field;

import com.samrj.devil.config.CfgField;
import com.samrj.devil.math.Util;

/**
 * Configuration field for 32 bit floating point angles. Angles are parsed in
 * degrees but stored in radians.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CfgAngle extends CfgFloat
{
    public CfgAngle(float value)
    {
        super(value);
    }
    
    @Override
    public void load(String in)
    {
        value = Util.toRadians(Float.parseFloat(in));
    }
    
    @Override
    public CfgField copy()
    {
        return new CfgAngle(value);
    }
}
