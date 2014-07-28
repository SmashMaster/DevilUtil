package com.samrj.devil.ui;

public class KeyEvent
{
    public int key;
    public boolean state;
    public char letter;
    public long time;
    
    public KeyEvent() {}
    
    public KeyEvent(KeyEvent e)
    {
        key = e.key;
        state = e.state;
        letter = e.letter;
        time = e.time;
    }
    
    @Override
    public KeyEvent clone()
    {
        return new KeyEvent(this);
    }
}