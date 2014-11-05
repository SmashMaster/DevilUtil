package com.samrj.devil.util;

import com.samrj.devil.ui.Element;
import com.samrj.devil.ui.Input;
import com.samrj.devil.ui.KeyEvent;
import com.samrj.devil.ui.MouseEvent;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

public abstract class QuickGame implements Element
{
    private DisplayMode displayMode;
    private PixelFormat pixelFormat;
    private ContextAttribs contextAttribs;
    private boolean running = false;
    
    public QuickGame(DisplayMode dm, PixelFormat pf, ContextAttribs ca)
    {
        if (dm == null) throw new NullPointerException();
        if (pf == null) pf = new PixelFormat(0, 0, 0);
        if (ca == null) ca = new ContextAttribs();
        
        displayMode = dm;
        pixelFormat = pf;
        contextAttribs = ca;
    }
    
    public QuickGame(int resX, int resY, PixelFormat pf)
    {
        this(new DisplayMode(resX, resY), pf, null);
    }
    
    public QuickGame(int resX, int resY)
    {
        this(resX, resY, null);
    }
    
    public abstract void init();
    @Override public abstract void in(MouseEvent in);
    @Override public abstract void in(KeyEvent in);
    public abstract void step(float dt);
    public abstract void render();
    public abstract void destroy();
    
    public final void run() throws LWJGLException
    {
        Display.setDisplayMode(displayMode);
        Display.setVSyncEnabled(false);
        Display.create(pixelFormat, contextAttribs);
        
        init();
        
        running = true;
        while (running)
        {
            Display.processMessages();
            if (Display.isCloseRequested())
            {
                stop();
                break;
            }
            
            Input.step(this);
            step(1f/60f);
            render();
            
            Display.update(false);
            Display.sync(60);
        }
        
        destroy();
        
        Display.destroy();
    }
    
    public final void stop()
    {
        running = false;
    }
}
