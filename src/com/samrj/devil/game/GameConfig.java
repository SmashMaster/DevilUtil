package com.samrj.devil.game;

import com.samrj.devil.game.step.StepDynamicSplit;
import com.samrj.devil.game.step.TimeStepper;
import com.samrj.devil.game.sync.SleepHybrid;
import com.samrj.devil.game.sync.SleepMethod;
import com.samrj.devil.math.Vec2i;

/**
 * Game configuration class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class GameConfig
{
    public boolean fullscreen = false;
    public boolean borderless = false;
    public final Vec2i resolution = new Vec2i(1280, 720);
    
    public boolean vsync = false;
    public int fps = 60;
    public int msaa = -1;
    
    public SleepMethod sleeper = new SleepHybrid();
    public TimeStepper stepper = new StepDynamicSplit(1.0f/480.0f, 1.0f/120.0f);
}
