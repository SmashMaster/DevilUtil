/*
 * Copyright (c) 2020 Sam Johnson
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
import com.samrj.devil.game.config.Config;
import com.samrj.devil.game.config.Controls;
import com.samrj.devil.game.config.SettingsMenu;
import com.samrj.devil.game.config.Vec2iSetting;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.graphics.EZDraw;
import com.samrj.devil.gui.DUI;
import com.samrj.devil.gui.Font;
import com.samrj.devil.gui.Window;
import com.samrj.devil.math.Vec2i;
import org.lwjgl.glfw.GLFWVidMode;

import java.io.IOException;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Wrapper for GameWindow that handles config file loading, re-mappable controls, a command console, and more.
 *
 * @author Samuel Johnson (SmashMaster)
 */
public final class Game<T extends GameMode>
{
    public static <T extends GameMode> Builder<T> builder()
    {
        return new Builder();
    }

    @FunctionalInterface
    public interface GameModeSupplier<T extends GameMode>
    {
        T apply(Game engine) throws IOException;
    }

    @FunctionalInterface
    public interface FontSupplier
    {
        Font get() throws IOException;
    }

    public static final class Builder<T extends GameMode>
    {
        public final State state;
        public final Config config;

        private boolean isNew = true;
        private boolean debug;
        private String title = "DevilUtil Game";
        private GameModeSupplier<T> modeSupplier = e -> null;
        private FontSupplier fontSupplier = () -> null;
        private SettingsMenu.Layout settingsLayout;
        private String consoleControlName;
        private Console.Callback consoleCallback;
        private Font font;
        private Game<T> game;

        private Builder()
        {
            state = new State(this);
            config = new Config(state);
        }

        public Builder glfwHint(int target, int hint)
        {
            state.requireNew();
            GameWindow.hint(target, hint);
            return this;
        }

        public Builder setDebug(boolean debug)
        {
            state.requireNew();
            this.debug = debug;
            return this;
        }

        public Builder setTitle(String title)
        {
            state.requireNew();
            this.title = Objects.requireNonNull(title);
            return this;
        }

        public Builder setFont(FontSupplier fontSupplier)
        {
            state.requireNew();
            this.fontSupplier = Objects.requireNonNull(fontSupplier);
            return this;
        }

        public Builder setSettingsLayout(SettingsMenu.Layout layout)
        {
            state.requireNew();
            settingsLayout = layout;
            return this;
        }

        public Builder setConsoleControl(String name, Controls.Source defaultBinding)
        {
            //This whole method is clunky. Could also be merged with setConsoleCallback.
            state.requireNew();
            if (name == null || defaultBinding == null) throw new NullPointerException();
            if (consoleControlName != null) config.controls.removeControl(name);
            config.controls.addControl(name, defaultBinding.TYPE.isGamepad ? null : defaultBinding, defaultBinding.TYPE.isGamepad ? defaultBinding : null);
            consoleControlName = name;
            return this;
        }

        public Builder setConsoleCallback(Console.Callback callback)
        {
            state.requireNew();
            consoleCallback = callback;
            return this;
        }

        public Builder setGameMode(GameModeSupplier<T> modeSupplier)
        {
            state.requireNew();
            this.modeSupplier = Objects.requireNonNull(modeSupplier);
            return this;
        }

        public void launch()
        {
            state.requireNew();
            isNew = false;

            config.load();
            GameWindow.setResolution(config.resolution.get());
            GameWindow.setFullscreen(config.fullscreen.get());
            GameWindow.setVsync(config.vSync.get());
            GameWindow.setFPSLimit(config.fpsLimit.get());
            GameWindow.setTitle(title);

            GameWindow.onInit(() ->
            {
                glfwMaximizeWindow(GameWindow.getWindow());

                glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                glClearDepth(1.0);
                DGL.init();
                DAL.init();

                DUI.init();
                font = fontSupplier.get();
                if (font == null) throw new NullPointerException("No font chosen.");
                DUI.setFont(fontSupplier.get());

                EZDraw.init(65536*4);

                game = new Game<>(this);

                GameWindow.onResize(game::resize);
                GameWindow.beforeInput(game::beforeInput);
                GameWindow.afterInput(game::afterInput);
                GameWindow.onMouseMoved(game::mouseMoved);
                GameWindow.onMouseButton(game::mouseButton);
                GameWindow.onMouseScroll(game::mouseScroll);
                GameWindow.onKey(game::key);
                GameWindow.onCharacter(game::character);
                GameWindow.onStep(game::step);
                GameWindow.onRender(game::render);

                game.mode = modeSupplier.apply(game);
            });

            GameWindow.onDestroy(crashed ->
            {
                config.save(); //Save the config even if we've crashed.
                DAL.detachAllSounds();
                if (font != null)
                {
                    font.destroy();
                    font = null;
                }
                game = null;
                EZDraw.destroy();
                DUI.destroy();

                //Most resources will leak in many crash situations. We should
                //see the source of the crash, not be spammed by leak messages.
                if (state.isDebugEnabled() && crashed)
                {
                    DAL.setDebugEnabled(false);
                    DGL.setDebugLeakTracking(false);
                }

                DAL.destroy();
                DGL.destroy();
            });

            GameWindow.run();
        }
    }

    public static final class State
    {
        private final Builder builder;

        private State(Builder builder)
        {
            this.builder = builder;
        }

        /**
         * Throws an IllegalStateException if the engine has begun loading resources, and we cannot change things like the
         * sets of controls, settings, entity & component types. These values should be immutable when startup begins.
         *
         * TODO: Rename this.
         */
        public void requireNew()
        {
            if (!builder.isNew) throw new IllegalStateException();
        }

        public void requireStarted()
        {
            if (builder.isNew) throw new IllegalStateException();
        }

        public boolean isDebugEnabled()
        {
            return builder.debug;
        }
    }

    public final Config config;
    public final Input input;
    public final Console<T> console;

    private final String consoleControlName;
    private final SettingsMenu settings;
    private final FPSCounter fpsCounter;
    private final Vec2i resolution;

    private boolean windowChangeRequested, sizeDirty, settingsRequested;
    private float frameTime;

    private Gamepad gamepad;
    private String gamepadName;

    private float prevMouseX, prevMouseY;
    private boolean prevMouseValid;

    public T mode;

    private Game(Builder<T> builder)
    {
        builder.state.requireStarted();
        this.config = builder.config;

        resolution = new Vec2i(config.get("resolution", Vec2iSetting.class).get());

        config.fullscreen.onChanged(val -> windowChangeRequested = true);
        config.resolution.onChanged(val -> windowChangeRequested = true);
        config.vSync.onChanged(GameWindow::setVsync);
        config.fpsLimit.onChanged(GameWindow::setFPSLimit);

        input = new Input(builder.state, config, this::input);
        settings = new SettingsMenu(config, builder.settingsLayout);
        console = new Console(this, builder.consoleCallback);
        consoleControlName = builder.consoleControlName;
        fpsCounter = new FPSCounter(resolution, config);
    }

    public void getResolution(Vec2i result)
    {
        result.set(resolution);
    }

    public Vec2i getResolution()
    {
        Vec2i result = new Vec2i();
        result.set(resolution);
        return result;
    }

    void resize(int width, int height)
    {
        resolution.set(width, height);
        sizeDirty = true;
    }

    private boolean modeHasFocus()
    {
        return mode != null && DUI.getTopWindow() == null;
    }

    void beforeInput()
    {
        if (gamepad == null || !gamepad.isPresent())
        {
            if (gamepad != null) gamepad.zeroOut(); //Zero out old gamepad before getting new one.

            gamepad = Gamepad.prefer(gamepadName);
            if (gamepad != null)
            {
                gamepadName = gamepad.name;
                gamepad.addAxisCallback(this::gamepadAxis);
                gamepad.addButtonCallback(this::gamepadButton);
            }
        }

        boolean modeHasFocus = modeHasFocus();
        if (mode != null)
        {
            mode.setHasFocus(modeHasFocus);
            mode.beforeInput();
        }
        GameWindow.getMouse().setGrabbed(settings.isListening() || modeHasFocus);
    }

    void mouseMoved(float x, float y)
    {
        if (settings.isListening()) return;

        if (!modeHasFocus())
        {
            DUI.mouseMoved(x, y);
            prevMouseValid = false;
        }

        if (modeHasFocus() && GameWindow.getMouse().isGrabbed())
        {
            if (prevMouseValid)
            {
                float dx = x - prevMouseX;
                float dy = y - prevMouseY;
                mode.mouseAxis(dx, dy);
            }

            prevMouseX = x; prevMouseY = y;
            prevMouseValid = true;
        }
    }

    void mouseButton(int button, int action, int mods)
    {
        if (settings.isListening())
        {
            settings.mouseButton(button, action, mods);
            return;
        }

        if (!modeHasFocus()) DUI.mouseButton(button, action, mods);

        input.mouseButton(button, action, mods);
    }

    void mouseScroll(float dx, float dy)
    {
        if (settings.isListening())
        {
            settings.mouseScroll(dx, dy);
            return;
        }

        if (!modeHasFocus()) DUI.mouseScroll(dx, dy);

        input.mouseScroll(dx, dy);
    }

    void key(int key, int action, int mods)
    {
        if (settings.isListening())
        {
            settings.key(key, action, mods);
            return;
        }

        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
        {
            Window topWindow = DUI.getTopWindow();
            if (topWindow != null) DUI.hide(topWindow);
            else settings.setVisible(!settings.isVisible());
        }

        if (!modeHasFocus()) DUI.key(key, action, mods);
        if (console.hasFocus()) console.key(key, action, mods);
        input.key(key, action, mods);
    }

    void character(char character, int codepoint)
    {
        if (!modeHasFocus()) DUI.character(character, codepoint);
    }

    private void gamepadButton(int button, int action)
    {
        if (settings.isListening())
        {
            settings.gamepadButton(button, action);
            return;
        }

        if (button == GLFW_GAMEPAD_BUTTON_START && action == GLFW_PRESS && settings.isVisible())
            settings.setVisible(false);

        input.gamepadButton(button, action);
    }

    private void gamepadAxis(int axis, float value)
    {
        if (settings.isListening())
        {
            settings.gamepadAxis(axis, value);
            return;
        }

        input.gamepadAxis(axis, value);
    }

    private void input(String target, boolean active)
    {
        if (active && target.equals(consoleControlName)) console.toggleVisible();

        if (mode != null) mode.input(target, active);
    }

    void afterInput()
    {
        if (gamepad != null) gamepad.update();
        console.afterInput();
    }

    void step(float dt)
    {
        if (mode != null) mode.step(dt);
        frameTime += dt;
    }

    void render()
    {
        if (windowChangeRequested)
        {
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode vidMode = glfwGetVideoMode(monitor);

            //Default to native resolution when switching to fullscreen.
            if (config.fullscreen.get() && !GameWindow.getFullscreen())
                config.resolution.set(new Vec2i(vidMode.width(), vidMode.height()));

            long window = GameWindow.getWindow();
            boolean fullscreen = config.fullscreen.get();
            Vec2i res = config.resolution.get();
            if (fullscreen != GameWindow.getFullscreen())
            {
                glfwSetWindowMonitor(window, fullscreen ? monitor : NULL, 0, 0, res.x, res.y, GLFW_DONT_CARE);

                if (!fullscreen) //Hack to ensure the window is decorated and maximized.
                {
                    glfwIconifyWindow(window);
                    glfwMaximizeWindow(window);
                }
            }
            else glfwSetWindowSize(window, res.x, res.y);

            GameWindow.setVsync(config.vSync.get());

            windowChangeRequested = false;
        }

        if (sizeDirty && resolution.x > 0 && resolution.y > 0)
        {
            if (mode != null) mode.resize(resolution.x, resolution.y);
            fpsCounter.resize(resolution.x, resolution.y);
            sizeDirty = false;
        }

        if (mode == null)
        {
            DGL.bindFBO(null);
            DGL.useProgram(null);
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT);
        }
        else mode.render(frameTime);

        glViewport(0, 0, resolution.x, resolution.y);
        fpsCounter.render();
        if (mode != null) mode.renderHUD();
        DUI.render();

        frameTime = 0.0f;

        if (settingsRequested)
        {
            settings.setVisible(true);
            settingsRequested = false;
        }
    }
}
