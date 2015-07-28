package com.samrj.devil.config;

/**
 * Interface for configuration fields. Config fields must have a no-argument
 * constructor.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface CfgField
{
    /**
     * Sets the value(s) of this field to the information in the given string.
     * 
     * @param in A string to parse.
     */
    public void load(String in);
    
    /**
     * Copies this field. Must return an object with the same class and values
     * as this.
     * 
     * @return A copy of this.
     */
    public CfgField copy();
}
