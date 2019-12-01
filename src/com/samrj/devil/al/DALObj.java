package com.samrj.devil.al;

/**
 * Abstract class for DevilAL objects.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class DALObj
{
    private final Throwable debugLeakTrace;
    
    DALObj()
    {
        debugLeakTrace = DAL.isDebugEnabled() ?
                new Throwable("DevilUtil (DAL) - " + this.getClass().getSimpleName() + " leaked!") :
                null;
    }
    
    abstract void delete();
    
    void debugLeakTrace()
    {
        debugLeakTrace.printStackTrace();
    }
}
