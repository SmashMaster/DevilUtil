package com.samrj.devil.gui;

/**
 * Utility wrapper for the Nuklear library.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class GUI
{
    private static boolean init;
    
    public static void init()
    {
        if (init) throw new IllegalStateException("Already initialized.");
        init = true;
    }
    
    public static void destroy()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        init = false;
    }
    
    private GUI()
    {
    }
}
