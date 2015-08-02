package com.samrj.devil.config;

/**
 * Configuration field for 32 bit floating point values.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CfgFloat implements CfgField
{
    public float value;
    
    public CfgFloat(float value)
    {
        this.value = value;
    }
    
    @Override
    public void load(String in)
    {
        value = Float.parseFloat(in);
    }
    
    @Override
    public CfgField copy()
    {
        return new CfgFloat(value);
    }
}
