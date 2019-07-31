/*
 * Copyright (c) 2019 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.game;

import com.samrj.devil.game.step.TimeStepper;
import com.samrj.devil.game.sync.Sync;
import com.samrj.devil.math.Vec2i;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;

/**
 * Utility game class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public abstract class Game
{
    private static boolean errorCallInit;
    private static boolean initialized;
    private static Thread mainThread;
    
    private static void ensureMainThread()
    {
        if (Thread.currentThread() !=  mainThread)
            throw new IllegalThreadStateException("Not on main thread " + mainThread);
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
            glfwSetErrorCallback(DisplayException::glfwThrow);
            errorCallInit = true;
        }
        
        glfwInit();
        Gamepads.init();
        mainThread = Thread.currentThread();
        initialized = true;
    }
    
    /**
     * Terminates the windowing system. Must be called on the main thread.
     */
    public static final void terminate()
    {
        ensureMainThread();
        initialized = false;
        mainThread = null;
        glfwTerminate();
    }
    
    /**
     * Provides a simple and convenient way to run a game. The game must have a
     * zero-argument constructor.
     * 
     * To use this method, simply call Game.run(YourGameClass::new);
     * 
     * @param constructor Any method which constructs and returns a Game.
     */
    public static final void run(GameConstructor constructor) throws Exception
    {
        Game.init();
        Game instance = constructor.construct();
        instance.run();
        instance.destroy();
        Game.terminate();
    }
    
    private boolean running;
    private long lastFrameTime;
    private long frameStart;
    
    public final long monitor, window;
    public final GLCapabilities capabilities;
    public final Sync sync;
    public final Mouse mouse;
    public final Keyboard keyboard;
    public final TimeStepper stepper;
    
    private final long frameTime;
    
    private boolean onLongFrame;
    private boolean destroyed;
    
    /**
     * Creates a new game object. Initializes the window with the given config.
     * 
     * @param title The title of the window.
     * @param hints The window hints to use.
     * @param config The configuration to use.
     */
    public Game(String title, HintSet hints, GameConfig config)
    {
        if (title == null || config == null) throw new NullPointerException();
        if (!initialized) throw new IllegalStateException("Game.init() not called.");
        ensureMainThread();
        
        // <editor-fold defaultstate="collapsed" desc="Initialize Window">
        {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
            glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
            glfwWindowHint(GLFW_DECORATED, config.borderless ? GL_FALSE : GL_TRUE);
            glfwWindowHint(GLFW_FLOATING, GL_FALSE);
            glfwWindowHint(GLFW_STENCIL_BITS, 0);
            if (config.msaa > 0) glfwWindowHint(GLFW_SAMPLES, config.msaa);
            if (hints != null) hints.glfw();
            
            monitor = config.fullscreen ? glfwGetPrimaryMonitor() : 0;
            window = glfwCreateWindow(config.resolution.x, config.resolution.y, title, monitor, 0);
            
            glfwMakeContextCurrent(window);
            glfwSwapInterval(config.vsync ? 1 : 0);
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            glfwSetWindowSizeCallback(window, (window, width, height) ->
            {
                config.resolution.set(width, height);
                glViewport(0, 0, width, height);
                onResized(width, height);
            });
        }
        
        if (!config.fullscreen) //Center window
        {
            Vec2i windowSize = GLFWUtil.getWindowSize(window);
            GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            
            glfwSetWindowPos(window, (mode.width() - windowSize.x)/2,
                                          (mode.height() - windowSize.y)/2);
        }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Initialize OpenGL Context">
        {
            capabilities = GL.createCapabilities();
            glViewport(0, 0, config.resolution.x, config.resolution.y);
            glDisable(GL_MULTISAMPLE);
        }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Initialize Sync">
        {
            if (!config.vsync && config.fps > 0)
            {
                sync = new Sync(config.fps, config.sleeper);
                frameTime = sync.getFrameTime();
            }
            else
            {
                sync = null;
                GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
                frameTime = Math.round(1_000_000_000.0/mode.refreshRate());
            }
        }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Initialize Input">
        {
            mouse = new Mouse(window, this::onMouseMoved, this::onMouseButton, this::onMouseScroll);
            mouse.setGrabbed(false);
            keyboard = new Keyboard(window, this::onKey, this::onCharacter);
        }
        // </editor-fold>
        stepper = config.stepper;
    }
    
    /**
     * Creates a new game object. Initializes the window with the given config.
     * 
     * @param title The title of the window.
     * @param config The configuration to use.
     */
    public Game(String title, GameConfig config)
    {
        this(title, null, config);
    }
    
    /**
     * Creates a new game window with the default title "Game" and the default
     * configuration. The default config creates a decorated window at 1280p.
     */
    public Game()
    {
        this("Game", new GameConfig());
    }
    
    /**
     * Sets the title of this window.
     * 
     * @param title The title to set to.
     */
    public final void setTitle(String title)
    {
        ensureMainThread();
        glfwSetWindowTitle(window, title);
    }
    
    public final Vec2i getResolution()
    {
        return GLFWUtil.getWindowSize(window);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Overridable Methods">
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
    public void onMouseMoved(float x, float y, float dx, float dy) {};
    
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
    public void onMouseButton(int button, int action, int mods) {};
    
    /**
     * Called whenever the scroll wheel is moved. Always called before step()
     * and render().
     * 
     * @param dx The horizontal scroll offset.
     * @param dy The vertical scroll offset.
     */
    public void onMouseScroll(float dx, float dy) {};
    
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
    public void onKey(int key, int action, int mods) {};
    
    /**
     * Called whenever a character is typed. Always called before step() and 
     * render(). Automatically accounts for modifiers like shift.
     * 
     * @param character The character that was typed.
     * @param codepoint The unicode codepoint that was typed.
     */
    public void onCharacter(char character, int codepoint) {};
    
    /**
     * Steps the simulation by a given amount of time. Called after input and
     * before rendering. The duration and number of time steps depends on the
     * time step method chosen.
     * 
     * @param dt The time step, in seconds.
     */
    public void step(float dt) {};
    
    /**
     * Called once per frame after all input and time steps, should be used for
     * any rendering code with OpenGL.
     */
    public void render() {};
    
    /**
     * Called when the game window is resized.
     */
    public void onResized(int width, int height) {};
    
    /**
     * Called when this game is destroyed. Should release any system resources
     * associated with this game.
     */
    public void onDestroy() {};
    // </editor-fold>
    
    /**
     * Flushes the keyboard/mouse input queues and discards all events. Also
     * releases any held buttons and invalidates the mouse position.
     */
    @Deprecated
    public final void discardInput()
    {
    }
    
    /**
     * Marks the current frame as taking a long time, so that excessively long
     * steps are not called next frame. Pairs well with discardInput() in order
     * to combat jumpy state after long frames, such as after loading screens.
     */
    public final void markLongFrame()
    {
        onLongFrame = true;
    }
    
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
            glfwShowWindow(window);

            long lastFrameStart = System.nanoTime() - frameTime;

            while (running)
            {
                frameStart = System.nanoTime();

                //Input
                glfwPollEvents();
                Gamepads.update();
                if (glfwWindowShouldClose(window)) stop();
                
                //Step
                if (onLongFrame) lastFrameTime = frameTime;
                else lastFrameTime = frameStart - lastFrameStart;
                onLongFrame = false;
                float dt = (float)(lastFrameTime/1_000_000_000.0);
                stepper.step(dt, this::step);
                lastFrameStart = frameStart;

                render();
                
                if (sync != null) sync.sync();
                glfwSwapBuffers(window);
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
        glfwDestroyWindow(window);
    }
    
    /**
     * @return Whether this game has been destroyed.
     */
    public final boolean isDestroyed()
    {
        return destroyed;
    }
    
    @FunctionalInterface
    public interface GameConstructor
    {
        Game construct() throws Exception;
    }
}
