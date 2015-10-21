package com.samrj.devil.gl;

/**
 * Abstract class for DevilGL objects which use system resources and must be
 * explicitly deleted, but should only be deleted by DevilGL.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class DGLObj
{
    abstract void delete();
}
