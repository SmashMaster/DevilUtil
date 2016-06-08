package com.samrj.devil.gl;

/**
 * General graphics performance/resource usage profiling class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Profiler
{
    private static long usedVRAM;
    
    /**
     * Adds the given number of bits to the internal VRAM usage counter. Is
     * called automatically for all DevilUtil classes. May be negative to
     * indicate VRAM no longer in use.
     * 
     * @param bits A number of bits to add.
     */
    public static void addUsedVRAM(long bits)
    {
        usedVRAM += bits;
    }
    
    /**
     * Removes the given number of bits from the internal VRAM usage counter. Is
     * called automatically for all DevilUtil classes.
     * 
     * @param bits A number of bits to add.
     */
    public static void removeUsedVRAM(long bits)
    {
        if (bits < 0) throw new IllegalArgumentException();
        usedVRAM -= bits;
    }
    
    /**
     * Returns approximately how many bits of video memory are currently being
     * used by DevilUtil.
     */
    public static long getUsedVRAM()
    {
        return usedVRAM;
    }
    
    private Profiler()
    {
    }
}
