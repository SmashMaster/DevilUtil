package com.samrj.devil.ui;

import com.samrj.devil.math.Vector2f;

public class MouseEvent
{
    public final Vector2f pos = new Vector2f();
    public final Vector2f dp = new Vector2f();
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