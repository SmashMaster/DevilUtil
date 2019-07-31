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

import com.samrj.devil.game.step.StepDynamicSplit;
import com.samrj.devil.game.step.TimeStepper;
import com.samrj.devil.game.sync.SleepHybrid;
import com.samrj.devil.game.sync.SleepMethod;
import com.samrj.devil.game.sync.Sync;
import com.samrj.devil.math.Vec2i;
import java.util.Objects;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Improved utility game class. To start up a game, simply call Game2.run().
 */
public final class Game2
{
    private static final StepCallback NULL_STEP_CALLBACK = (dt) -> {};
    
    public static final HintSet HINTS = new HintSet();
    
    private static TimeStepper stepper = new StepDynamicSplit(1.0f/480.0f, 1.0f/120.0f);
    private static SleepMethod sleeper = new SleepHybrid();
    private static final Vec2i RESOLUTION = new Vec2i(1280, 720);
    private static boolean vsync = true, fullscreen = false;
    private static int fpsLimit = 60;
    private static String title = "DevilUtil Game";
    private static boolean mouseGrabbed = false;
    
    private static long window = NULL;
    private static GLCapabilities capabilities;
    private static Sync sync;
    private static Mouse mouse;
    private static Keyboard keyboard;
    private static boolean running;
    
    private static Runnable initCallback;
    private static ResizeCallback resizeCallback;
    private static Mouse.CursorCallback mouseCursorCallback;
    private static Mouse.ButtonCallback mouseButtonCallback;
    private static Mouse.ScrollCallback mouseScrollCallback;
    private static Keyboard.KeyCallback keyCallback;
    private static Keyboard.CharacterCallback characterCallback;
    private static Runnable beforeInputCallback;
    private static Runnable afterInputCallback;
    private static StepCallback stepCallback = NULL_STEP_CALLBACK;
    private static Runnable renderCallback;
    private static Runnable destroyCallback;
    
    // <editor-fold defaultstate="collapsed" desc="Setters">
    /**
     * Sets the frame split behavior. By default, onStep() may be called
     * multiple times per frame, and has minimum and maximum timesteps that it
     * can pass. This method may be used to change this.
     */
    public static void setStepper(TimeStepper stepper)
    {
        Game2.stepper = Objects.requireNonNull(stepper);
    }
    
    /**
     * Sets the frame synchronization behavior. If vsync is not enabled, after a
     * frame has been rendered, the given
     * sleeper will spend some time to wait for the correct time to display the
     * latest frame. If vsync is enabled, the given sleeper will not be used.
     */
    public static void setSleeper(SleepMethod sleeper)
    {
        Game2.sleeper = Objects.requireNonNull(sleeper);
        if (running) sync.setSleeper(sleeper);
    }
    
    /**
     * This sets the resolution of the window. If the window has already been
     * created, it will be resized and an onResize event will be generated.
     */
    public static void setResolution(int width, int height)
    {
        RESOLUTION.set(width, height);
        if (running)
        {
            glfwSetWindowSize(window, width, height);
            glViewport(0, 0, width, height);
            if (resizeCallback != null) resizeCallback.resize(width, height);
        }
    }
    
    /**
     * Sets whether or not the window will be fullscreen.
     */
    public static void setFullscreen(boolean fullscreen)
    {
        if (running) throw new UnsupportedOperationException("Not implemented yet.");
        Game2.fullscreen = fullscreen;
    }
    
    /**
     * This sets whether or not vsync is enabled. If it is enabled, each new
     * frame will be displayed precisely when the monitor refreshes, preventing
     * screen tearing. This may introduce some latency or reduce frame rates.
     * 
     * Defaults to true.
     */
    public static void setVsync(boolean vsync)
    {
        if (running) glfwSwapInterval(vsync ? 1 : 0);
        Game2.vsync = vsync;
    }
    
    /**
     * Sets the maximum frames per second that the game is allowed to render at.
     * This is not used if vsync is enabled. Set to 0 or lower to disable.
     * 
     * Defaults to 60.
     */
    public static void setFPSLimit(int fpsLimit)
    {
        if (running) sync.setFPS(fpsLimit);
        Game2.fpsLimit = fpsLimit;
    }
    
    /**
     * Sets the title of this window.
     */
    public static void setTitle(String title)
    {
        if (running) glfwSetWindowTitle(window, title);
        Game2.title = Objects.requireNonNull(title);
    }
    
    /**
     * Sets whether the mouse is grabbed (invisible and locked to the window's
     * boundaries) or not.
     */
    public static void setMouseGrabbed(boolean mouseGrabbed)
    {
        if (running) glfwSetInputMode(window, GLFW_CURSOR, mouseGrabbed ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
        Game2.mouseGrabbed = mouseGrabbed;
    }
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters">
    /**
     * Returns the GLFW handle to this window, or 0 if the game is not running.
     */
    public static long getWindow()
    {
        return window;
    }
    
    /**
     * Returns this game's OpenGL capabilities, or null if it's not running.
     */
    public static GLCapabilities getGLCapabilities()
    {
        return capabilities;
    }
    
    /**
     * Returns the frame time of this game in nanoseconds, or 0 if either
     * the FPS limit and vsync are both disabled, or the game is not running.
     */
    public static long getFrameNanos()
    {
        if (!running) return 0;
        if (!vsync && fpsLimit <= 0) return 0;
        
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vsync && mode != null) return Math.round(1_000_000_000.0/mode.refreshRate());
        
        return sync.getFrameTime();
    }
    
    /**
     * Returns the current resolution of this game's window, or null if it's
     * not running.
     */
    public static Vec2i getResolution()
    {
        return running ? GLFWUtil.getWindowSize(window) : null;
    }
    
    /**
     * Returns the game's mouse, or null if the game is not running.
     */
    public static Mouse getMouse()
    {
        return mouse;
    }
    
    /**
     * Returns the game's keyboard, or null if the game is not running.
     */
    public static Keyboard getKeyboard()
    {
        return keyboard;
    }
    
    /**
     * Returns the game's Gamepads, or null if the game is not running.
     */
    public static Gamepads getGamepads()
    {
        return null;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Callbacks">
    
    /**
     * The given callback is run once, after the window amd OpenGL context have
     * been created and before the game runs. Any previous callback is
     * overwritten.
     */
    public static void onInit(Runnable callback)
    {
        initCallback = callback;
    }
    
    @FunctionalInterface
    public static interface ResizeCallback
    {
        public void resize(int width, int height);
    }
    
    /**
     * The given callback is run any time the window has been resized. Any
     * previous callback is overwritten.
     */
    public static void onResize(ResizeCallback callback)
    {
        resizeCallback = callback;
    }
    
    /**
     * The given callback is run any time the mouse is moved. Any previous
     * callback is overwritten.
     */
    public static void onMouseMoved(Mouse.CursorCallback callback)
    {
        mouseCursorCallback = callback;
    }
    
    /**
     * The given callback is run any time a mouse button's state changes. Any
     * previous callback is overwritten.
     */
    public static void onMouseButton(Mouse.ButtonCallback callback)
    {
        mouseButtonCallback = callback;
    }
    
    /**
     * The given callback is run any time the mouse wheel is scrolled. Any
     * previous callback is overwritten.
     */
    public static void onMouseScroll(Mouse.ScrollCallback callback)
    {
        mouseScrollCallback = callback;
    }
    
    /**
     * The given callback is run any time a key's state changes. Any previous
     * callback is overwritten.
     */
    public static void onKey(Keyboard.KeyCallback callback)
    {
        keyCallback = callback;
    }
    
    /**
     * The given callback is run any time the keyboard outputs a unicode
     * character. This does not correspond with keystrokes in a one-to-one
     * manner. Any previous callback is overwritten.
     */
    public static void onCharacter(Keyboard.CharacterCallback callback)
    {
        characterCallback = callback;
    }
    
    /**
     * The given callback is run once per frame, at the start of every frame,
     * before input has been polled.
     */
    public static void beforeInput(Runnable callback)
    {
        beforeInputCallback = callback;
    }
    
    /**
     * The given callback is run once per frame, after input is polled and
     * before the step callback is run.
     */
    public static void afterInput(Runnable callback)
    {
        afterInputCallback = callback;
    }
    
    @FunctionalInterface
    public static interface StepCallback
    {
        public void step(float dt);
    }
    
    /**
     * The given callback is run at least once per frame, before the frame is
     * rendered. The number of times this callback is run, and the timesteps
     * passed into it are determined by this game's TimeStepper.
     */
    public static void onStep(StepCallback callback)
    {
        if (callback == null) stepCallback = NULL_STEP_CALLBACK;
        else stepCallback = callback;
    }
    
    /**
     * The given callback is run once at the end of each frame, before the frame
     * buffer is swapped.
     */
    public static void onRender(Runnable callback)
    {
        renderCallback = callback;
    }
    
    /**
     * The given callback is run once, when this game has finished running, but
     * before the associated window and OpenGL context have been destroyed. This
     * should be used to free native resources.
     */
    public static void onDestroy(Runnable callback)
    {
        destroyCallback = callback;
    }
    // </editor-fold>
    
    /**
     * Creates a window and runs the game using the currently set callbacks and
     * settings.
     */
    public static void run()
    {
        if (running) throw new IllegalStateException();
        
        glfwSetErrorCallback(DisplayException::glfwThrow);
        glfwInit();
        
        //Create window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        HINTS.glfw();
        
        long monitor = fullscreen ? glfwGetPrimaryMonitor() : 0L;
        window = glfwCreateWindow(RESOLUTION.x, RESOLUTION.y, title, monitor, 0);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(vsync ? 1 : 0);
        glfwSetInputMode(window, GLFW_CURSOR, mouseGrabbed ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
        glfwSetWindowSizeCallback(window, (window, width, height) ->
        {
            glViewport(0, 0, width, height);
            RESOLUTION.set(width, height);
            if (resizeCallback != null) resizeCallback.resize(width, height);
        });
        
        //Center window
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidMode != null && !fullscreen)
        {
            Vec2i windowSize = GLFWUtil.getWindowSize(window);
            glfwSetWindowPos(window, (vidMode.width() - windowSize.x)/2, (vidMode.height() - windowSize.y)/2);
        }
        
        //Create OpenGL context
        capabilities = GL.createCapabilities();
        glViewport(0, 0, RESOLUTION.x, RESOLUTION.y);
        
        //Create Sync
        sync = new Sync(fpsLimit, sleeper);
        
        //Run
        running = true;
        glfwShowWindow(window);
        
        try
        {
            if (initCallback != null) initCallback.run();
            
            long lastFrameStart = System.nanoTime() - getFrameNanos();
            while (running)
            {
                long frameStart = System.nanoTime();

                //Input
                if (beforeInputCallback != null) beforeInputCallback.run();
                glfwPollEvents();
                Gamepads.update();
                if (afterInputCallback != null) afterInputCallback.run();
                if (glfwWindowShouldClose(window)) stop();

                //Step
                long lastFrameTime = frameStart - lastFrameStart;
                float dt = (float)(lastFrameTime/1_000_000_000.0);
                stepper.step(dt, stepCallback::step);
                lastFrameStart = frameStart;

                if (renderCallback != null) renderCallback.run();

                if (sync != null) sync.sync();
                glfwSwapBuffers(window);
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (destroyCallback != null) destroyCallback.run();
            glfwDestroyWindow(window);
            glfwTerminate();
        }
    }
    
    public static void stop()
    {
        running = false;
    }
    
    private Game2()
    {
    }
}
