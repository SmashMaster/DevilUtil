package com.samrj.devil.game;

import com.samrj.devil.config.CfgBoolean;
import com.samrj.devil.config.CfgInteger;
import com.samrj.devil.config.Configuration;
import com.samrj.devil.config.CfgResolution;
import com.samrj.devil.display.GLFWUtil;
import com.samrj.devil.display.VideoMode;
import com.samrj.devil.game.sync.SleepHybrid;
import com.samrj.devil.game.sync.Sync;
import com.samrj.devil.math.Vec2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.OpenGLException;

/**
 * Utility game class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Game
{
    private static Configuration defaultConfig()
    {
        Configuration config = new Configuration();
        config.addField("fullscreen", new CfgBoolean(false));
        config.addField("borderless", new CfgBoolean(false));
        config.addField("res", new CfgResolution(1280, 720));
        config.addField("vsync", new CfgBoolean(false));
        config.addField("fps", new CfgInteger(60));
        config.addField("msaa", new CfgInteger(0));
        return config;
    }
    
    private boolean running;
    private long lastFrameTime;
    
    public final Configuration config;
    public final long monitor, window;
    public final GLContext context;
    public final Mouse mouse;
    public final Keyboard keyboard;
    
    private final Sync sync;
    private final long frameTime;
    private final EventBuffer eventBuffer;
    
    public Game(Configuration config) throws OpenGLException
    {
        if (config == null) throw new NullPointerException();
        this.config = config;
        
        boolean fullscreen = config.getBoolean("fullscreen");
        boolean borderless = config.getBoolean("borderless");
        CfgResolution res = config.getField("res");
        boolean vsync = config.getBoolean("vsync");
        int fps = config.getInt("fps");
        int msaa = config.getInt("msaa");
        
        // <editor-fold defaultstate="collapsed" desc="Initialize Window">
        {
            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, borderless ? GL11.GL_FALSE : GL11.GL_TRUE);
            GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, GL11.GL_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 0);
            GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, msaa);
            
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GL11.GL_TRUE);

            monitor = fullscreen ? GLFW.glfwGetPrimaryMonitor() : 0;
            window = GLFW.glfwCreateWindow(res.width, res.height, "Nocturne", monitor, 0);

            GLFW.glfwMakeContextCurrent(window);
            GLFW.glfwSwapInterval(vsync ? 1 : 0);
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        }
        
        if (!fullscreen) //Center window
        {
            Vec2i windowSize = GLFWUtil.getWindowSize(window);
            VideoMode videoMode = GLFWUtil.getPrimaryMonitorVideoMode();
            
            GLFW.glfwSetWindowPos(window, (videoMode.width - windowSize.x)/2,
                                          (videoMode.height - windowSize.y)/2);
        }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Initialize OpenGL Context">
        {
            context = GLContext.createFromCurrent();
            
            GL11.glViewport(0, 0, res.width, res.height);
            if (msaa > 0) GL11.glEnable(GL13.GL_MULTISAMPLE);
        }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Initialize Sync">
        {
            if (!vsync && fps > 0)
            {
                sync = new Sync(fps, new SleepHybrid());
                frameTime = sync.getFrameTime();
            }
            else
            {
                sync = null;
                VideoMode mode = GLFWUtil.getPrimaryMonitorVideoMode();
                frameTime = Math.round(1_000_000_000.0/mode.refreshRate);
            }
        }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Initialize Input">
        {
            mouse = new Mouse(window)
            {
                @Override
                public void onMoved(float x, float y, float dx, float dy)
                {
                    Game.this.onMouseMoved(x, y, dx, dy);
                }
                
                @Override
                public void onButton(int button, int action, int mods)
                {
                    Game.this.onMouseButton(button, action, mods);
                }
                
                @Override
                public void onScroll(float dx, float dy)
                {
                    Game.this.onMouseScroll(dx, dy);
                }
            };
            mouse.setGrabbed(false);
            keyboard = new Keyboard()
            {
                @Override
                public void onKey(int key, int action, int mods)
                {
                    Game.this.onKey(key, action, mods);
                }
            };
            eventBuffer = new EventBuffer(window, mouse, keyboard);
        }
        // </editor-fold>
        
        context.checkGLError();
    }
    
    public Game() throws OpenGLException
    {
        this(defaultConfig());
    }
    
    public abstract void onMouseMoved(float x, float y, float dx, float dy);
    public abstract void onMouseButton(int button, int action, int mods);
    public abstract void onMouseScroll(float dx, float dy);
    public abstract void onKey(int key, int action, int mods);
    public abstract void step(float dt);
    public abstract void render();
    
    public final void run()
    {
        running = true;
        GLFW.glfwShowWindow(window);
        
        long lastFrameStart = System.nanoTime() - frameTime;
        
        while (running) try
        {
            long frameStart = System.nanoTime();
            
            {//Input
                GLFW.glfwPollEvents();
                eventBuffer.flushEvents();
                if (GLFW.glfwWindowShouldClose(window) == GL11.GL_TRUE) stop();
            }
            
            // <editor-fold defaultstate="collapsed" desc="Time Step">
            {
                final float segmentLength = 1.0f/120.0f;

                lastFrameTime = frameStart - lastFrameStart;
                float dt = (float)(lastFrameTime/1_000_000_000.0);
                
                if (dt <= segmentLength) step(dt);
                else
                {
                    float remainder = dt % segmentLength;
                    int numSegments = Math.round((dt - remainder)/segmentLength);

                    if (remainder > segmentLength/4.0f)
                    {
                        for (int s=0; s<numSegments; s++) step(segmentLength);
                        step(remainder);
                    }
                    else
                    {
                        float segdt = dt/numSegments;
                        for (int s=0; s<numSegments; s++) step(segdt);
                    }
                }
                
                lastFrameStart = frameStart;
            }
            // </editor-fold>
            render();
            
            if (sync != null) sync.sync();
            GLFW.glfwSwapBuffers(window);
        }
        catch (InterruptedException e) {stop();}
    }
    
    public final void stop()
    {
        running = false;
    }
    
    public final long lastFrameTime()
    {
        return lastFrameTime;
    }
    
    public abstract void onDestroy();
    
    public final void destroy()
    {
        onDestroy();
        context.destroy();
        GLFW.glfwDestroyWindow(window);
    }
}
