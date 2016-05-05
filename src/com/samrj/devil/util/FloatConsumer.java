package com.samrj.devil.util;

/**
 * Functional interface for lambdas which accept a single float value.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
@FunctionalInterface
public interface FloatConsumer
{
    public void accept(float f);
}
