package com.samrj.devil.al;

import org.lwjgl.openal.EXTEfx;

/**
 * Wrapper for an OpenAL EFX filter object.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Filter extends DALObj
{
    final int id;
    
    Filter()
    {
        id = EXTEfx.alGenFilters();
    }
    
    @Override
    void delete()
    {
        EXTEfx.alDeleteFilters(id);
    }
}
