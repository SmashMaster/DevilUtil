package com.samrj.devil.config;

/**
 * Configuration field for 32 bit integer values.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CfgInteger implements CfgField
{
    public int value;
    
    public CfgInteger(int value)
    {
        this.value = value;
    }
    
    @Override
    public void load(String in)
    {
        value = Integer.parseInt(in);
    }
    
    @Override
    public CfgField copy()
    {
        return new CfgInteger(value);
    }
}
