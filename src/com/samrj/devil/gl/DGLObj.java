package com.samrj.devil.gl;

/**
 * Abstract class for DevilGL objects which use system resources and must be
 * explicitly deleted, but should only be deleted by DevilGL.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class DGLObj
{
    private final Throwable debugLeakTrace;
    
    DGLObj()
    {
        debugLeakTrace = DGL.isDebugEnabled() ?
                new Throwable("DevilUtil (DGL) - " + this.getClass().getSimpleName() + " leaked!") :
                null;
    }
    
    abstract void delete();
    
    void debugLeakTrace()
    {
        debugLeakTrace.printStackTrace();
    }
}
