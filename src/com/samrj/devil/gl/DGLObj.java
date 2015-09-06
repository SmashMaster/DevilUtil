/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.samrj.devil.gl;

/**
 * Abstract class for DevilGL objects which use system resources and must be
 * explicitly deleted, but should only be deleted by DevilGL.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
abstract class DGLObj
{
    abstract void delete();
}
