/*
 * Copyright (c) 2022 Sam Johnson
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

import com.samrj.devil.al.DAL;
import com.samrj.devil.game.step.StepDynamicSplit;
import com.samrj.devil.game.step.TimeStepper;
import com.samrj.devil.game.sync.SleepHybrid;
import com.samrj.devil.game.sync.SleepMethod;
import com.samrj.devil.game.sync.Sync;
import com.samrj.devil.math.Vec2i;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Configuration;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Improved utility game class. To start up a game, simply call Game.run().
 */
public final class Game
{
    private static final StepCallback NULL_STEP_CALLBACK = (dt) -> {};
    private static final HintSet HINTS = new HintSet();
    
    private static TimeStepper stepper = new StepDynamicSplit(1.0f/480.0f, 1.0f/120.0f);
    private static SleepMethod sleeper = new SleepHybrid();
    private static final Vec2i RESOLUTION = new Vec2i(1280, 720);
    private static boolean vsync = true, fullscreen = false;
    private static int fpsLimit = 60;
    private static String title = "DevilUtil Game";
    
    private static long window = NULL;
    private static GLCapabilities capabilities;
    private static Sync sync;
    private static Mouse mouse;
    private static Keyboard keyboard;
    private static boolean running;
    
    private static int pauseFrameCounter;
    private static long lastFrameTime;
    
    private static InitCallback initCallback;
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
    private static Consumer<Boolean> destroyCallback;
    
    // <editor-fold defaultstate="collapsed" desc="Setup">
    /**
     * Sets whether the context created by this game will be in debug mode. Adds
     * error checking to many DevilUtil libraries, including DGL and DAL.
     * 
     * Defaults to false.
     */
    public static void setDebug(boolean debug)
    {
        if (running) throw new IllegalStateException("Debug must be set before running game.");
        
        Configuration.DEBUG.set(debug);
        Configuration.DEBUG_STACK.set(debug);
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(debug);
        hint(GLFW_OPENGL_DEBUG_CONTEXT, debug ? GLFW_TRUE : GLFW_FALSE);
        DAL.setDebugEnabled(debug);
    }
    
    /**
     * Sets a given GLFW window hint, that will be passed to the window when the
     * game is created. See GLFW documentation for a list of valid hints:
     * 
     * http://www.glfw.org/docs/latest/window.html#window_hints
     */
    public static void hint(int target, int hint)
    {
        if (running) throw new IllegalStateException();
        HINTS.hint(target, hint);
    }
    
    /**
     * Clears all GLFW hints passed to the game so that only default hints are
     * passed.
     */
    public static void clearHints()
    {
        HINTS.clear();
    }
    
    /**
     * Sets the frame split behavior. By default, onStep() may be called
     * multiple times per frame, and has minimum and maximum timesteps that it
     * can pass. This method may be used to change this.
     */
    public static void setStepper(TimeStepper stepper)
    {
        Game.stepper = Objects.requireNonNull(stepper);
    }
    
    /**
     * Sets the frame synchronization behavior. If vsync is not enabled, after a
     * frame has been rendered, the given
     * sleeper will spend some time to wait for the correct time to display the
     * latest frame. If vsync is enabled, the given sleeper will not be used.
     */
    public static void setSleeper(SleepMethod sleeper)
    {
        if (running) sync.setSleeper(sleeper);
        Game.sleeper = Objects.requireNonNull(sleeper);
    }
    
    /**
     * This sets the resolution of the window. If the window has already been
     * created, it will be resized and an onResize event will be generated.
     */
    public static void setResolution(int width, int height)
    {
        if (running)
        {
            glfwSetWindowSize(window, width, height);
            glViewport(0, 0, width, height);
            if (resizeCallback != null) resizeCallback.resize(width, height);
        }
        RESOLUTION.set(width, height);
    }
    
    /**
     * This sets the resolution of the window. If the window has already been
     * created, it will be resized and an onResize event will be generated.
     */
    public static void setResolution(Vec2i resolution)
    {
        setResolution(resolution.x, resolution.y);
    }
    
    /**
     * Returns the resolution at which this game is running.
     */
    public static Vec2i getResolution()
    {
        if (running) return GLFWUtil.getFramebufferSize(window);
        else return new Vec2i(RESOLUTION);
    }
    
    /**
     * Sets whether or not the window will be fullscreen.
     */
    public static void setFullscreen(boolean fullscreen)
    {
        if (running)
        {
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode mode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(window, monitor, 0, 0, RESOLUTION.x, RESOLUTION.y, mode.refreshRate());
        }
        Game.fullscreen = fullscreen;
    }
    
    /**
     * Returns whether the game is running in fullscreen or not.
     */
    public static boolean getFullscreen()
    {
        if (running) return glfwGetWindowMonitor(window) != NULL;
        else return fullscreen;
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
        Game.vsync = vsync;
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
        Game.fpsLimit = fpsLimit;
    }
    
    /**
     * Sets the title of this window.
     */
    public static void setTitle(String title)
    {
        if (running) glfwSetWindowTitle(window, title);
        Game.title = Objects.requireNonNull(title);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Runtime">
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
     * Returns the desired frame time of this game in nanoseconds, or 0 if
     * either the FPS limit and vsync are both disabled, or the game is not
     * running. This is not the same as the actual frame rate of the game.
     */
    public static long getFrameTargetNano()
    {
        if (!running) return 0;
        if (!vsync && fpsLimit <= 0) return 0;
        
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vsync && mode != null) return Math.round(1_000_000_000.0/mode.refreshRate());
        
        return sync.getFrameTime();
    }
    
    /**
     * Returns the length of the previous frame in nanoseconds, or 0 if a frame
     * hasn't yet elapsed.
     */
    public static long getLastFrameNano()
    {
        return lastFrameTime;
    }
    
    /**
     * Pauses the game for the next few frames. The step, render, and input
     * methods are all called normally, except that zero will be passed as
     * the time step until the game is unpaused. This is useful for very long
     * frames, such as loading screens, so that the elapsed time doesn't get
     * passed into step().
     */
    public static void pauseFrames(int frames)
    {
        pauseFrameCounter += frames;
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
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Callbacks">
    /**
     * The given callback is run once, after the window amd OpenGL context have
     * been created and before the game runs. Any previous callback is
     * overwritten.
     */
    public static void onInit(InitCallback callback)
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

    /**
     * The given callback is run at least once per frame, before the frame is
     * rendered. The number of times this callback is run, and the timesteps
     * passed into it are determined by this game's TimeStepper.
     * 
     * The timestep, in seconds as a float, is passed to the given callback.
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
     * 
     * True is passed to the given callback if the game ended due to an
     * exception; otherwise, false is passed.
     */
    public static void onDestroy(Consumer<Boolean> callback)
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
        glfwSetFramebufferSizeCallback(window, (window, width, height) ->
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
        
        //Setup input
        mouse = new Mouse(window, (x, y) ->
        {
            if (mouseCursorCallback != null) mouseCursorCallback.accept(x, y);
        },
        (button, action, mods) ->
        {
            if (mouseButtonCallback != null) mouseButtonCallback.accept(button, action, mods);
        },
        (dx, dy) ->
        {
            if (mouseScrollCallback != null) mouseScrollCallback.accept(dx, dy);
        });
        
        keyboard = new Keyboard(window, (key, action, mods) ->
        {
            if (keyCallback != null) keyCallback.accept(key, action, mods);
        },
        (character, codepoint) ->
        {
            if (characterCallback != null) characterCallback.accept(character, codepoint);
        });
        
        //Create OpenGL context
        capabilities = GL.createCapabilities();
        Vec2i resolution = getResolution(); //Window may have chosen different resolution on creation, if fullscreen.
        glViewport(0, 0, resolution.x, resolution.y);
        
        //Create Sync
        sync = new Sync(fpsLimit, sleeper);
        
        boolean exceptionCaught = false;
        
        try //Main game loop
        {
            running = true;
            if (initCallback != null) initCallback.init();
            glfwShowWindow(window);
            
            long lastFrameStart = System.nanoTime() - getFrameTargetNano();
            while (running)
            {
                long frameStart = System.nanoTime();

                //Input
                if (beforeInputCallback != null) beforeInputCallback.run();
                glfwPollEvents();
                if (afterInputCallback != null) afterInputCallback.run();
                if (glfwWindowShouldClose(window)) stop();

                //Step
                if (pauseFrameCounter > 0)
                {
                    lastFrameTime = 0L;
                    pauseFrameCounter--;
                }
                else lastFrameTime = frameStart - lastFrameStart;
                float dt = (float)(lastFrameTime/1_000_000_000.0);
                stepper.step(dt, stepCallback::step);
                lastFrameStart = frameStart;

                if (renderCallback != null) renderCallback.run();

                if (!vsync && fpsLimit > 0) sync.sync();
                glfwSwapBuffers(window);
            }
        }
        catch (IOException e)
        {
            exceptionCaught = true;
            throw new RuntimeException("Init failed.", e);
        }
        catch (InterruptedException e)
        {
            exceptionCaught = true;
            throw new RuntimeException("Thread interrupted.", e);
        }
        catch (Throwable t)
        {
            exceptionCaught = true;
            throw t;
        }
        finally //Cleanup
        {
            running = false;
            
            try
            {
                if (destroyCallback != null) destroyCallback.accept(exceptionCaught);
            }
            catch (Throwable t)
            {
                //If both try blocks catch an exception, we cannot throw both.
                //We must swallow the second exception to throw the first.
                if (!exceptionCaught) throw t;
            }
            finally
            {
                glfwSetErrorCallback(null).free();
                glfwSetFramebufferSizeCallback(window, null).free();
                mouse.destroy();
                keyboard.destroy();
                GL.setCapabilities(null);

                glfwDestroyWindow(window);
                glfwTerminate();

                window = NULL;
                capabilities = null;
                sync = null;
                mouse = null;
                keyboard = null;
                lastFrameTime = 0;
            }
        }
    }
    
    public static void stop()
    {
        running = false;
    }
    
    private Game()
    {
    }
}
