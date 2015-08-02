package com.samrj.devil.config;

/**
 * Configuration field for boolean values.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CfgBoolean implements CfgField
{
    public boolean value;
    
    public CfgBoolean(boolean value)
    {
        this.value = value;
    }
    
    @Override
    public void load(String in)
    {
        value = Boolean.parseBoolean(in);
    }
    
    @Override
    public CfgField copy()
    {
        return new CfgBoolean(value);
    }
}
