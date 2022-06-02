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

import com.samrj.devil.game.step.StepFixedAccum;
import com.samrj.devil.game.step.TimeStepper;
import com.samrj.devil.game.sync.SleepHybrid;
import com.samrj.devil.game.sync.SleepMethod;
import com.samrj.devil.game.sync.Sync;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A game loop that runs without a window. Useful for background processes like servers. Can make multiple instances of
 * this, unlike GameWindow or Game.
 */
public final class BackgroundProcess implements Runnable
{
    private static final StepCallback NULL_STEP_CALLBACK = (dt) -> {};

    private TimeStepper stepper = new StepFixedAccum(1.0f/20.0f);
    private SleepMethod sleeper = new SleepHybrid();
    private int fpsLimit = 20;

    private Sync sync;
    private boolean running;

    private long lastFrameTime;

    private InitCallback initCallback;
    private StepCallback stepCallback = NULL_STEP_CALLBACK;
    private Consumer<Boolean> destroyCallback;

    public BackgroundProcess()
    {
    }

    /**
     * Sets the frame split behavior. By default, onStep() will be called once per frame. This method may be used to
     * change this.
     */
    public void setStepper(TimeStepper stepper)
    {
        this.stepper = Objects.requireNonNull(stepper);
    }

    /**
     * Sets the frame synchronization behavior. By default, the process will wait after each frame.
     */
    public void setSleeper(SleepMethod sleeper)
    {
        if (running) sync.setSleeper(sleeper);
        this.sleeper = Objects.requireNonNull(sleeper);
    }

    /**
     * Sets the maximum frames per second that the process is allowed to run at. Set to 0 or lower to disable: not
     * recommended as this will be CPU-intensive with little benefit.
     *
     * Defaults to 20 frames per second.
     */
    public void setFPSLimit(int fpsLimit)
    {
        if (running) sync.setFPS(fpsLimit);
        this.fpsLimit = fpsLimit;
    }

    /**
     * Returns the desired frame time of this process in nanoseconds, or 0 if the FPS limit is disabled, or the game is
     * not running. This is not the same as the actual frame rate of the game.
     */
    public long getFrameTargetNano()
    {
        if (!running) return 0;
        if (fpsLimit <= 0) return 0;
        return sync.getFrameTime();
    }

    /**
     * Returns the length of the previous frame in nanoseconds, or 0 if a frame hasn't yet elapsed.
     */
    public long getLastFrameNano()
    {
        return lastFrameTime;
    }

    /**
     * The given callback is called once before the process runs. Any previous callback is overwritten.
     */
    public void onInit(InitCallback callback)
    {
        initCallback = callback;
    }

    /**
     * The given callback is run at least once per frame. The number of times this callback is called, and the timesteps
     * passed into it are determined by this process's TimeStepper.
     *
     * The timestep, in seconds as a float, is passed to the given callback.
     */
    public void onStep(StepCallback callback)
    {
        if (callback == null) stepCallback = NULL_STEP_CALLBACK;
        else stepCallback = callback;
    }

    /**
     * The given callback is run once, when this process has finished running, but
     * before the associated window and OpenGL context have been destroyed. This
     * should be used to free native resources.
     *
     * True is passed to the given callback if the game ended due to an
     * exception; otherwise, false is passed.
     */
    public void onDestroy(Consumer<Boolean> callback)
    {
        destroyCallback = callback;
    }
    // </editor-fold>

    /**
     * Runs this process using the currently set callbacks and settings.
     */
    @Override
    public void run()
    {
        if (running) throw new IllegalStateException();

        sync = new Sync(fpsLimit, sleeper);
        boolean exceptionCaught = false;

        try //Main loop
        {
            running = true;
            if (initCallback != null) initCallback.init();

            long lastFrameStart = System.nanoTime() - getFrameTargetNano();
            while (running)
            {
                long frameStart = System.nanoTime();

                //Step
                lastFrameTime = frameStart - lastFrameStart;
                float dt = (float)(lastFrameTime/1_000_000_000.0);
                stepper.step(dt, stepCallback::step);
                lastFrameStart = frameStart;

                if (fpsLimit > 0) sync.sync();
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
                sync = null;
                lastFrameTime = 0;
            }
        }
    }

    public void stop()
    {
        running = false;
    }
}
