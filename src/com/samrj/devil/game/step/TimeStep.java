package com.samrj.devil.game.step;

/**
 * Functional interface for whatever happens during a time step.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
@FunctionalInterface
public interface TimeStep
{
    void step(float dt);
}
