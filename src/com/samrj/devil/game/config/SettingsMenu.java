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

package com.samrj.devil.game.config;

import com.samrj.devil.game.Input;
import com.samrj.devil.gui.*;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.util.Pair;
import org.lwjgl.glfw.GLFWVidMode;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static org.lwjgl.glfw.GLFW.*;

/**
 * GUI settings menu used by Game to edit the config in-game.
 *
 * @author Samuel Johnson (SmashMaster)
 */
public class SettingsMenu
{
    @FunctionalInterface
    public interface Layout
    {
        void apply(Config config, LayoutRows rows);
    }

    public static final float WIDTH = 512.0f, HEIGHT = 640.0f;
    public static final float ROW_HEIGHT = 30;
    public static final float COMBO_WIDTH = 160.0f;
    
    private final Config config;
    private final Layout layout;
    private final Window window;
    
    private Pair<Controls.Source, String> listeningMK = null;
    private Pair<Controls.Source, String> listeningGP = null;
    
    public SettingsMenu(Config config, Layout layout)
    {
        this.config = config;
        this.layout = layout;
        
        window = new Window()
                .setTitle("Settings")
                .setPadding(0.0f)
                .setContent(layout(this))
                .setSizeFromContent();
    }

    public static Form row(String label, Form form)
    {
        return new FixedRow()
                .setAlignment(Align.W.vector())
                .add(0.0f, new Text(label))
                .add(WIDTH/2.0f, form)
                .setSizeFromContent()
                .setHeight(ROW_HEIGHT);
    }

    public static Form combool(Setting<Boolean> setting, String label, String option0, String option1)
    {
        return row(label, new ComboBox()
                .setOptions(option0, option1)
                .setSelection(setting.get() ? 1 : 0)
                .setSelectCallback(box -> setting.set(box.getSelection() == 1))
                .setSize(COMBO_WIDTH));
    }
    
    public static Form combool(Setting<Boolean> setting, String label)
    {
        return combool(setting, label, "Off", "On");
    }

    private static boolean equals(GLFWVidMode a, GLFWVidMode b)
    {
        if (a.width() != b.width()) return false;
        if (a.height() != b.height()) return false;
        if (a.redBits() != b.redBits()) return false;
        if (a.greenBits() != b.greenBits()) return false;
        if (a.blueBits() != b.blueBits()) return false;
        return a.refreshRate() == b.refreshRate();
    }

    public static Form resolution(Setting<Vec2i> setting, Setting<Boolean> fullscreenSetting, String label)
    {
        long monitor = glfwGetPrimaryMonitor();
        ComboBox combo = new ComboBox();
        
        //Buffer position assumed to be zero.
        GLFWVidMode.Buffer allModes = glfwGetVideoModes(monitor);
        if (allModes.position() != 0) throw new IllegalStateException();

        //Current mode is not stored in the above buffer, or we could find its index with pointer arithmetic.
        GLFWVidMode currentMode = glfwGetVideoMode(monitor);

            //Filter modes to ones which match the current bit depth and refresh rate.
        ArrayList<GLFWVidMode> modes = new ArrayList<>();
        for (GLFWVidMode mode : allModes)
            if (mode.redBits() == currentMode.redBits() &&
                mode.blueBits() == currentMode.blueBits() &&
                mode.greenBits() == currentMode.greenBits() &&
                mode.refreshRate() == currentMode.refreshRate()) modes.add(mode);

        //Find the current video mode index.
        int curModeIndex = 0;
        for (int i=0; i<modes.size(); i++)
        {
            GLFWVidMode mode = modes.get(i);

            if (equals(currentMode, mode))
            {
                curModeIndex = i;
                break;
            }
        }

        String[] options = new String[modes.size()];
        for (int i=0; i<modes.size(); i++)
        {
            GLFWVidMode mode = modes.get(i);
            options[i] = mode.width() + "x" + mode.height();
        }

        combo.setOptions(options).setSelection(curModeIndex).setDropDownHeight(384.0f).setSize(COMBO_WIDTH);
        
        combo.setSelectCallback(c ->
        {
            GLFWVidMode newMode = modes.get(c.getSelection());
            setting.set(new Vec2i(newMode.width(), newMode.height()));
        });
        
        return row(label, new ConditionalForm()
                .setForms(new Text("(fullscreen only)"), combo)
                .setCondition(() -> fullscreenSetting.get() ? 1 : 0));
    }

    public static Form number(Setting<Integer> setting, String label, int min, int max)
    {
        int initVal = setting.get();
        
        TextField field = new TextField()
                .setText(Integer.toString(initVal))
                .setSize(64.0f);
        
        Slider slider = new Slider()
                .setValue(Util.linstep(min, max, initVal))
                .setSize(128.0f, 20.0f);
        
        field.setLoseFocusCallback(f ->
        {
            try
            {
                int value = Math.round(Float.parseFloat(f.get()));
                if (value < min) value = min;
                if (value > max) value = max;
                slider.setValue(Util.linstep(min, max, value));
                f.setText(Integer.toString(value));
                setting.set(value);
            }
            catch (NumberFormatException e)
            {
                f.setText(Integer.toString(setting.get()));
            }
        });
        
        field.setConfirmCallback(f -> DUI.focus(null));
        
        slider.setChangeCallback(s ->
        {
            int value = Math.round(Util.lerp(min, max, s.getValue()));
            field.setText(Integer.toString(value));
            setting.set(value);
        });
        
        return row(label, new LayoutColumns(Align.W.vector())
                .add(field)
                .add(slider));
    }
    
    public static Form fpsLimit(Setting<Integer> setting, Setting<Boolean> vsyncSetting, String label)
    {
        return new ConditionalForm()
                .setForms(number(setting, label, 30, 300),
                          row(label, new Text("(overridden by vsync)")))
                .setCondition(() -> vsyncSetting.get() ? 1 : 0);
    }

    private static Form mkBinding(SettingsMenu menu, String target)
    {
        return row(target, new BindingButton(menu, target, false));
    }
    
    private static Form gpBinding(SettingsMenu menu, String target)
    {
        return row(target, new BindingButton(menu, target, true));
    }
    
    private static Form layout(SettingsMenu menu)
    {
        Config config = menu.config;

        ScrollBox scroll = new ScrollBox();
        LayoutRows rows = new LayoutRows();
        
        rows.add(new Text("Display"), Align.N.vector());
        
        rows.add(combool(config.fullscreen, "Fullscreen"));
        rows.add(resolution(config.resolution, config.fullscreen, "Resolution"));
        rows.add(combool(config.vSync, "Vertical Sync"));
        rows.add(fpsLimit(config.fpsLimit, config.vSync, "FPS Limit"));
        rows.add(combool(config.showFPS, "Show FPS"));

        if (menu.layout != null) menu.layout.apply(config, rows);

        rows.add(new Text("Bindings (Mouse & Keyboard)"), Align.N.vector());

        Controls controls = config.controls;
        Map<String, ?> defaultControls = controls.getDefaults();
        
        for (String target : defaultControls.keySet())
            rows.add(mkBinding(menu, target));
        
        rows.add(new Text("Bindings (Gamepad)"), Align.N.vector());

        for (String target : defaultControls.keySet())
            rows.add(gpBinding(menu, target));
        
        return scroll.setContent(rows).setSizeFromContent(HEIGHT);
    }
    
    public boolean isListening()
    {
        return listeningMK != null || listeningGP != null;
    }
    
    private void stopListening()
    {
        if (isListening())
        {
            listeningMK = null;
            listeningGP = null;
        }
    }
    
    public void setVisible(boolean visible)
    {
        if (visible) DUI.show(window.setPosCenterViewport());
        else if (window.isVisible()) DUI.hide(window);
    }
    
    public boolean isVisible()
    {
        return window.isVisible();
    }
    
    private void rebind(Pair<Controls.Source, String> binding, Controls.Source source)
    {
        Controls controls = config.controls;
        if (binding.a != null) controls.unbind(binding.a, binding.b);
        if (source != null) controls.bind(source, binding.b);
        stopListening();
    }
    
    public void mouseButton(int button, int action, int mods)
    {
        if (listeningMK == null || action != GLFW_PRESS) return;
        
        rebind(listeningMK, Controls.getMouseButtonSource(button));
    }
    
    public void mouseScroll(float dx, float dy)
    {
        if (listeningMK == null) return;
        
        if (dx < 0.0f) rebind(listeningMK, Controls.Source.MOUSE_SCROLL_LEFT);
        else if (dx > 0.0f) rebind(listeningMK, Controls.Source.MOUSE_SCROLL_RIGHT);
        else if (dy < 0.0f) rebind(listeningMK, Controls.Source.MOUSE_SCROLL_DOWN);
        else if (dy > 0.0f) rebind(listeningMK, Controls.Source.MOUSE_SCROLL_UP);
    }
    
    public void key(int key, int action, int mods)
    {
        if (action != GLFW_PRESS) return;
        
        switch (key)
        {
            case GLFW_KEY_ESCAPE: stopListening(); break;
            case GLFW_KEY_DELETE:
                if (listeningMK != null) rebind(listeningMK, null);
                else if (listeningGP != null) rebind(listeningGP, null);
                break;
            default:
                if (listeningMK != null)
                    rebind(listeningMK, Controls.getKeySource(key));
                break;
        }
    }
    
    public void gamepadButton(int button, int action)
    {
        if (listeningGP == null || action != GLFW_PRESS) return;
        
        if (button == GLFW_GAMEPAD_BUTTON_START)
        {
            stopListening();
            return;
        }
        
        rebind(listeningGP, Controls.getGamepadButtonSource(button));
    }
    
    public void gamepadAxis(int axis, float value)
    {
        if (listeningGP == null) return;
        
        switch (axis)
        {
            case GLFW_GAMEPAD_AXIS_LEFT_TRIGGER:
                if (value*0.5f + 0.5f > Input.BOOLEAN_THRESHOLD)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_LEFT_TRIGGER);
                break;
            case GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER:
                if (value*0.5f + 0.5f > Input.BOOLEAN_THRESHOLD)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_RIGHT_TRIGGER);
                break;
            case GLFW_GAMEPAD_AXIS_LEFT_X:
                if (value < -0.5f)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_LEFT_STICK_LEFT);
                else if (value > 0.5f)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_LEFT_STICK_RIGHT);
                break;
            case GLFW_GAMEPAD_AXIS_LEFT_Y:
                if (value < -0.5f)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_LEFT_STICK_UP);
                else if (value > 0.5f)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_LEFT_STICK_DOWN);
                break;
            case GLFW_GAMEPAD_AXIS_RIGHT_X:
                if (value < -0.5f)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_RIGHT_STICK_LEFT);
                else if (value > 0.5f)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_RIGHT_STICK_RIGHT);
                break;
            case GLFW_GAMEPAD_AXIS_RIGHT_Y:
                if (value < -0.5f)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_RIGHT_STICK_UP);
                else if (value > 0.5f)
                    rebind(listeningGP, Controls.Source.GAMEPAD_AXIS_RIGHT_STICK_DOWN);
                break;
        }
    }
    
    private static class BindingButton extends Form
    {
        private static final float PADDING = 3.0f;

        private final SettingsMenu menu;
        private final Config config;
        private final String target;
        private final boolean gamepad;
        private Controls.Source source;
        private String text = "";
        private boolean thisListen;
        
        private BindingButton(SettingsMenu menu, String target, boolean gamepad)
        {
            this.menu = menu;
            this.config = menu.config;
            this.target = target;
            this.gamepad = gamepad;
            
            width = 202.0f;
            height = DUI.font().getHeight() + PADDING*2.0f;
        }
        
        @Override
        protected void updateSize() //Convenient method to do this in.
        {
            Controls controls = config.controls;
            Pair<Controls.Source, String> listenPair = gamepad ? menu.listeningGP : menu.listeningMK;
            thisListen = listenPair != null && target.equals(listenPair.b);
            
            if (thisListen)
            {
                source = null;
                text = "(select binding...)";
            }
            else
            {
                Predicate<Controls.Source> filter =
                    gamepad ? src -> src.TYPE == Controls.SourceType.GAMEPAD_BUTTON ||
                                     src.TYPE == Controls.SourceType.GAMEPAD_AXIS :
                              src -> src.TYPE == Controls.SourceType.KEYBOARD ||
                                     src.TYPE == Controls.SourceType.MOUSE_BUTTON ||
                                     src.TYPE == Controls.SourceType.MOUSE_SCROLL;
                
                source = controls.getSources(target).stream().filter(filter).findAny().orElse(null);
                text = source != null ? source.NAME : "";
            }
        }
        
        @Override
        protected Form hover(float x, float y)
        {
            if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
            return this;
        }

        @Override
        protected boolean activate(int button)
        {
            if (button != GLFW_MOUSE_BUTTON_LEFT) return false;
            if (!menu.isListening())
            {
                if (gamepad) menu.listeningGP = new Pair<>(source, target);
                else menu.listeningMK = new Pair<>(source, target);
            }
            return false;
        }
    
        @Override
        protected void render(DUIDrawer drawer)
        {
            float x1 = x0 + width, y1 = y0 + height;

            float outline = DUI.getHoveredForm() == this ? 1.0f : 0.75f;

            drawer.color(0.25f, 0.25f, 0.25f, 1.0f);
            drawer.rectFill(x0, x1, y0, y1);
            drawer.color(outline, outline, outline, 1.0f);
            drawer.rect(x0, x1, y0, y1);
            
            Font font = DUI.font();
            Vec2 aligned = Align.insideBounds(font.getSize(text), x0 + PADDING, x1 - PADDING, y0 + PADDING, y1 - PADDING, Align.C.vector());
            drawer.text(text, font, aligned.x, aligned.y);
        }
    }
}
