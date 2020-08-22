package com.samrj.devil.util;


/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
@FunctionalInterface
public interface TriConsumer<T, U, V>
{
    public void accept(T a, U b, V c);
}
