package com.samrj.devil.game;

import com.samrj.devil.config.CfgBoolean;
import com.samrj.devil.config.CfgInteger;
import com.samrj.devil.config.CfgResolution;
import com.samrj.devil.config.Configuration;
import com.samrj.devil.display.DisplayException;
import com.samrj.devil.display.GLFWUtil;
import com.samrj.devil.display.HintSet;
import com.samrj.devil.display.VideoMode;
import com.samrj.devil.game.step.StepDynamicSplit;
import com.samrj.devil.game.step.TimeStepper;
import com.samrj.devil.game.sync.SleepHybrid;
import com.samrj.devil.game.sync.Sync;
import com.samrj.devil.math.Vec2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
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
    private static boolean errorCallInit;
    private static boolean initialized;
    private static Thread mainThread;
    
    private static Configuration defaultConfig()
    {
        Configuration config = new Configuration();
        config.addField("fullscreen", new CfgBoolean(false));
        config.addField("borderless", new CfgBoolean(false));
        config.addField("res", new CfgResolution(1280, 720));
        config.addField("vsync", new CfgBoolean(false));
        config.addField("fps", new CfgInteger(60));
        return config;
    }
    
    private static void ensureMainThread()
    {
        if (Thread.currentThread() !=  mainThread)
            throw new IllegalThreadStateException("Not on main thread.");
    }
    
    /**
     * Initializes the windowing system. Whichever thread calls this becomes
     * the main thread, and only classes on the main thread may construct and
     * run games.
     */
    public static final void init()
    {
        if (initialized) throw new IllegalStateException("Already initialized.");
        
        if (!errorCallInit)
        {
            GLFWErrorCallback errorCallback = GLFW.GLFWErrorCallback(DisplayException::glfwThrow);
            GLFW.glfwSetErrorCallback(errorCallback);
            errorCallInit = true;
        }
        
        GLFW.glfwInit();
        mainThread = Thread.currentThread();
        initialized = true;
    }
    
    /**
     * Terminates the windowing system. Must be called on the main thread.
     */
    public static final void terminate()
    {
        if (!initialized) throw new IllegalStateException("Not initialized.");
        ensureMainThread();
        
        initialized = false;
        mainThread = null;
        GLFW.glfwTerminate();
    }
    
    private boolean running;
    private long lastFrameTime;
    private long frameStart;
    
    public final Configuration config;
    public final long monitor, window;
    public final GLContext context;
    public final Sync sync;
    public final Mouse mouse;
    public final Keyboard keyboard;
    public final TimeStepper stepper;
    
    private final long frameTime;
    private final EventBuffer eventBuffer;
    
    private boolean destroyed;
    
    /**
     * Creates a new game object. Initializes the window with the given config.
     * 
     * The configuration must have the following fields:
     * 
     * fullscreen (boolean)
     * borderless (boolean)
     * res (resolution)
     * vsync (boolean)
     * fps (integer)
     * msaa (integer)
     * 
     * It may have other fields as well, but they will not affect window
     * creation.
     * 
     * @param title The title of the window.
     * @param hints The window hints to use.
     * @param config The configuration to use.
     * @throws OpenGLException If there is an OpenGL error.
     */
    public Game(String title, HintSet hints, Configuration config)
    {
        if (title == null || config == null) throw new NullPointerException();
        if (!initialized) throw new IllegalStateException("Game.init() not called.");
        ensureMainThread();
        this.config = config;
        
        boolean fullscreen = config.getBoolean("fullscreen");
        boolean borderless = config.getBoolean("borderless");
        CfgResolution res = config.getField("res");
        boolean vsync = config.getBoolean("vsync");
        int fps = config.getInt("fps");
        
        // <editor-fold defaultstate="collapsed" desc="Initialize Window">
        {
            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, borderless ? GL11.GL_FALSE : GL11.GL_TRUE);
            GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, GL11.GL_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 0);
            if (hints != null) hints.glfw();
            
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GL11.GL_TRUE);

            monitor = fullscreen ? GLFW.glfwGetPrimaryMonitor() : 0;
            window = GLFW.glfwCreateWindow(res.width, res.height, title, monitor, 0);
            
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
        stepper = new StepDynamicSplit(1.0f/480.0f, 1.0f/120.0f);
        
        context.checkGLError();
        frameStart = System.nanoTime();
    }
    
    /**
     * Creates a new game object. Initializes the window with the given config.
     * 
     * @param title The title of the window.
     * @param config The configuration to use.
     */
    public Game(String title, Configuration config)
    {
        this(title, null, config);
    }
    
    /**
     * Creates a new game window with the default title "Game" and the default
     * configuration. The default config creates a decorated window at 1280p.
     * 
     * @throws OpenGLException If there is an OpenGL error.
     */
    public Game()
    {
        this("Game", defaultConfig());
    }
    
    /**
     * Sets the title of this window.
     * 
     * @param title The title to set to.
     */
    public final void setTitle(String title)
    {
        ensureMainThread();
        GLFW.glfwSetWindowTitle(window, title);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Abstract Methods">
    /**
     * Called whenever the mouse is moved. Always called before step() and
     * render(). The coordinates are relative to the bottom left corner of the
     * display.
     * 
     * @param x The x position of the mouse.
     * @param y The y position of the mouse.
     * @param dx The amount the x position has changed since the last call.
     * @param dy The amount the y position has changed since the last call.
     */
    public abstract void onMouseMoved(float x, float y, float dx, float dy);
    
    /**
     * Called whenever a mouse button is pressed. Always called before step()
     * and render(). The key modifier bit field is defined by GLFW:
     * 
     * http://www.glfw.org/docs/latest/group__mods.html
     * 
     * @param button The GLFW enum representing which button was affected.
     * @param action One of GLFW_PRESS or GLFW_RELEASE.
     * @param mods Bit field describing which modifier keys were held down.
     */
    public abstract void onMouseButton(int button, int action, int mods);
    
    /**
     * Called whenever the scroll wheel is moved. Always called before step()
     * and render().
     * 
     * @param dx The horizontal scroll offset.
     * @param dy The vertical scroll offset.
     */
    public abstract void onMouseScroll(float dx, float dy);
    
    /**
     * Called whenever a key is pressed. Always called before step() and
     * render(). The key modifier bit field is defined by GLFW:
     * 
     * http://www.glfw.org/docs/latest/group__mods.html
     * 
     * @param key The GLFW enum representing which key was affected.
     * @param action One of GLFW_PRESS, GLFW_RELEASE or GLFW_REPEAT.
     * @param mods Bit field describing which modifier keys were held down.
     */
    public abstract void onKey(int key, int action, int mods);
    
    /**
     * Steps the simulation by a given amount of time. Called after input and
     * before rendering. The duration and number of time steps depends on the
     * time step method chosen.
     * 
     * @param dt The time step, in seconds.
     */
    public abstract void step(float dt);
    
    /**
     * Called once per frame after all input and time steps, should be used for
     * any rendering code with OpenGL.
     */
    public abstract void render();
    
    /**
     * Called when this game is destroyed. Should release any system resources
     * associated with this game.
     */
    public abstract void onDestroy();
    // </editor-fold>
    
    /**
     * Runs the game, showing the window and beginning the game loop. Must be
     * called on the main thread, and the game cannot be destroyed.
     */
    public final void run()
    {
        if (!initialized) throw new IllegalStateException("Game.init() not called.");
        ensureMainThread();
        if (destroyed) throw new IllegalStateException("Game has been destroyed.");
        
        try
        {
            running = true;
            if (!context.isCurrent()) context.makeCurrent(window);
            GLFW.glfwShowWindow(window);

            long lastFrameStart = frameStart - frameTime;

            while (running)
            {
                frameStart = System.nanoTime();

                //Input
                GLFW.glfwPollEvents();
                eventBuffer.flushEvents();
                if (GLFW.glfwWindowShouldClose(window) == GL11.GL_TRUE) stop();
                
                //Step
                lastFrameTime = frameStart - lastFrameStart;
                float dt = (float)(lastFrameTime/1_000_000_000.0);
                stepper.step(this::step, dt);
                lastFrameStart = frameStart;

                render();
                
                GL11.glFinish();
                if (sync != null) sync.sync();
                GLFW.glfwSwapBuffers(window);
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Thread interrupted.", e);
        }
        finally
        {
            stop();
        }
    }
    
    /**
     * Stops this game. May be called from any thread.
     */
    public final void stop()
    {
        running = false;
    }
    
    /**
     * @return How long, in nanoseconds, the duration of the previous frame.
     */
    public final long lastFrameTime()
    {
        return lastFrameTime;
    }
    
    /**
     * @return The time, as measured by System.nanoTime(), when this frame
     *         started. This is also the time that the previous frame ended.
     */
    public final long frameStart()
    {
        return frameStart;
    }
    
    /**
     * Destroys this game and window, and releases any resources associated
     * resources.
     */
    public final void destroy()
    {
        ensureMainThread();
        if (destroyed) return;
        destroyed = true;
        
        onDestroy();
        context.destroy();
        GLFW.glfwDestroyWindow(window);
    }
    
    /**
     * @return Whether this game has been destroyed.
     */
    public final boolean isDestroyed()
    {
        return destroyed;
    }
}
