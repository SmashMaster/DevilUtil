package com.samrj.devil.ui;

import com.samrj.devil.math.Vec2;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class MouseEvent
{
    public final Vec2 pos = new Vec2();
    public final Vec2 dp = new Vec2();
    public int button;
    public boolean state;
    public int dWheel;
    public long time;
    public boolean isDirect = true;
    
    public MouseEvent() {}
    
    public MouseEvent(MouseEvent e)
    {
        pos.set(e.pos);
        dp.set(e.dp);
        button = e.button;
        state = e.state;
        dWheel = e.dWheel;
        time = e.time;
        isDirect = e.isDirect;
    }
    
    @Override
    public MouseEvent clone()
    {
        return new MouseEvent(this);
    }
}
