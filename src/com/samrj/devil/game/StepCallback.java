package com.samrj.devil.game;

/**
 * Callback used for Game and BackgroundProcess.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
@FunctionalInterface
public interface StepCallback
{
    public void step(float dt);
}
