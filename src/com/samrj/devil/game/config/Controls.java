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

import com.samrj.devil.game.Game;
import com.samrj.devil.json.Json;
import com.samrj.devil.json.JsonObject;
import com.samrj.devil.util.Pair;

import java.util.*;
import java.util.function.BiConsumer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Re-mappable controls class, allows actions to be bound to the mouse, keyboard, or a gamepad.
 *
 * @author Samuel Johnson (SmashMaster)
 */
public final class Controls
{
    public enum SourceType
    {
        KEYBOARD(false), MOUSE_BUTTON(false), MOUSE_SCROLL(false),
        GAMEPAD_BUTTON(true), GAMEPAD_AXIS(true);

        public final boolean isGamepad;

        SourceType(boolean isGamepad)
        {
            this.isGamepad = isGamepad;
        }
    }
    
    public enum Source
    {
        KEY_SPACE(SourceType.KEYBOARD, GLFW_KEY_SPACE, "Space"),
        KEY_APOSTROPHE(SourceType.KEYBOARD, GLFW_KEY_APOSTROPHE, "'"),
        KEY_COMMA(SourceType.KEYBOARD, GLFW_KEY_COMMA, ","),
        KEY_MINUS(SourceType.KEYBOARD, GLFW_KEY_MINUS, "-"),
        KEY_PERIOD(SourceType.KEYBOARD, GLFW_KEY_PERIOD, "."),
        KEY_SLASH(SourceType.KEYBOARD, GLFW_KEY_SLASH, "/"),
        KEY_0(SourceType.KEYBOARD, GLFW_KEY_0, "0"),
        KEY_1(SourceType.KEYBOARD, GLFW_KEY_1, "1"),
        KEY_2(SourceType.KEYBOARD, GLFW_KEY_2, "2"),
        KEY_3(SourceType.KEYBOARD, GLFW_KEY_3, "3"),
        KEY_4(SourceType.KEYBOARD, GLFW_KEY_4, "4"),
        KEY_5(SourceType.KEYBOARD, GLFW_KEY_5, "5"),
        KEY_6(SourceType.KEYBOARD, GLFW_KEY_6, "6"),
        KEY_7(SourceType.KEYBOARD, GLFW_KEY_7, "7"),
        KEY_8(SourceType.KEYBOARD, GLFW_KEY_8, "8"),
        KEY_9(SourceType.KEYBOARD, GLFW_KEY_9, "9"),
        KEY_SEMICOLON(SourceType.KEYBOARD, GLFW_KEY_SEMICOLON, ";"),
        KEY_EQUAL(SourceType.KEYBOARD, GLFW_KEY_EQUAL, "="),
        KEY_A(SourceType.KEYBOARD, GLFW_KEY_A, "A"),
        KEY_B(SourceType.KEYBOARD, GLFW_KEY_B, "B"),
        KEY_C(SourceType.KEYBOARD, GLFW_KEY_C, "C"),
        KEY_D(SourceType.KEYBOARD, GLFW_KEY_D, "D"),
        KEY_E(SourceType.KEYBOARD, GLFW_KEY_E, "E"),
        KEY_F(SourceType.KEYBOARD, GLFW_KEY_F, "F"),
        KEY_G(SourceType.KEYBOARD, GLFW_KEY_G, "G"),
        KEY_H(SourceType.KEYBOARD, GLFW_KEY_H, "H"),
        KEY_I(SourceType.KEYBOARD, GLFW_KEY_I, "I"),
        KEY_J(SourceType.KEYBOARD, GLFW_KEY_J, "J"),
        KEY_K(SourceType.KEYBOARD, GLFW_KEY_K, "K"),
        KEY_L(SourceType.KEYBOARD, GLFW_KEY_L, "L"),
        KEY_M(SourceType.KEYBOARD, GLFW_KEY_M, "M"),
        KEY_N(SourceType.KEYBOARD, GLFW_KEY_N, "N"),
        KEY_O(SourceType.KEYBOARD, GLFW_KEY_O, "O"),
        KEY_P(SourceType.KEYBOARD, GLFW_KEY_P, "P"),
        KEY_Q(SourceType.KEYBOARD, GLFW_KEY_Q, "Q"),
        KEY_R(SourceType.KEYBOARD, GLFW_KEY_R, "R"),
        KEY_S(SourceType.KEYBOARD, GLFW_KEY_S, "S"),
        KEY_T(SourceType.KEYBOARD, GLFW_KEY_T, "T"),
        KEY_U(SourceType.KEYBOARD, GLFW_KEY_U, "U"),
        KEY_V(SourceType.KEYBOARD, GLFW_KEY_V, "V"),
        KEY_W(SourceType.KEYBOARD, GLFW_KEY_W, "W"),
        KEY_X(SourceType.KEYBOARD, GLFW_KEY_X, "X"),
        KEY_Y(SourceType.KEYBOARD, GLFW_KEY_Y, "Y"),
        KEY_Z(SourceType.KEYBOARD, GLFW_KEY_Z, "Z"),
        KEY_LEFT_BRACKET(SourceType.KEYBOARD, GLFW_KEY_LEFT_BRACKET, "["),
        KEY_BACKSLASH(SourceType.KEYBOARD, GLFW_KEY_BACKSLASH, "\\"),
        KEY_RIGHT_BRACKET(SourceType.KEYBOARD, GLFW_KEY_RIGHT_BRACKET, "]"),
        KEY_GRAVE_ACCENT(SourceType.KEYBOARD, GLFW_KEY_GRAVE_ACCENT, "`"),
        KEY_WORLD_1(SourceType.KEYBOARD, GLFW_KEY_WORLD_1, "Non-US 1"),
        KEY_WORLD_2(SourceType.KEYBOARD, GLFW_KEY_WORLD_2, "Non-US 2"),
        KEY_ESCAPE(SourceType.KEYBOARD, GLFW_KEY_ESCAPE, "Escape"),
        KEY_ENTER(SourceType.KEYBOARD, GLFW_KEY_ENTER, "Enter"),
        KEY_TAB(SourceType.KEYBOARD, GLFW_KEY_TAB, "Tab"),
        KEY_BACKSPACE(SourceType.KEYBOARD, GLFW_KEY_BACKSPACE, "Backspace"),
        KEY_INSERT(SourceType.KEYBOARD, GLFW_KEY_INSERT, "Insert"),
        KEY_DELETE(SourceType.KEYBOARD, GLFW_KEY_DELETE, "Delete"),
        KEY_RIGHT(SourceType.KEYBOARD, GLFW_KEY_RIGHT, "Right Arrow"),
        KEY_LEFT(SourceType.KEYBOARD, GLFW_KEY_LEFT, "Left Arrow"),
        KEY_DOWN(SourceType.KEYBOARD, GLFW_KEY_DOWN, "Down Arrow"),
        KEY_UP(SourceType.KEYBOARD, GLFW_KEY_UP, "Up Arrow"),
        KEY_PAGE_UP(SourceType.KEYBOARD, GLFW_KEY_PAGE_UP, "Page Up"),
        KEY_PAGE_DOWN(SourceType.KEYBOARD, GLFW_KEY_PAGE_DOWN, "Page Down"),
        KEY_HOME(SourceType.KEYBOARD, GLFW_KEY_HOME, "Home"),
        KEY_END(SourceType.KEYBOARD, GLFW_KEY_END, "End"),
        KEY_CAPS_LOCK(SourceType.KEYBOARD, GLFW_KEY_CAPS_LOCK, "Caps Lock"),
        KEY_SCROLL_LOCK(SourceType.KEYBOARD, GLFW_KEY_SCROLL_LOCK, "Scroll Lock"),
        KEY_NUM_LOCK(SourceType.KEYBOARD, GLFW_KEY_NUM_LOCK, "Num Lock"),
        KEY_PRINT_SCREEN(SourceType.KEYBOARD, GLFW_KEY_PRINT_SCREEN, "Print Screen"),
        KEY_PAUSE(SourceType.KEYBOARD, GLFW_KEY_PAUSE, "Pause"),
        KEY_F1(SourceType.KEYBOARD, GLFW_KEY_F1, "F1"),
        KEY_F2(SourceType.KEYBOARD, GLFW_KEY_F2, "F2"),
        KEY_F3(SourceType.KEYBOARD, GLFW_KEY_F3, "F3"),
        KEY_F4(SourceType.KEYBOARD, GLFW_KEY_F4, "F4"),
        KEY_F5(SourceType.KEYBOARD, GLFW_KEY_F5, "F5"),
        KEY_F6(SourceType.KEYBOARD, GLFW_KEY_F6, "F6"),
        KEY_F7(SourceType.KEYBOARD, GLFW_KEY_F7, "F7"),
        KEY_F8(SourceType.KEYBOARD, GLFW_KEY_F8, "F8"),
        KEY_F9(SourceType.KEYBOARD, GLFW_KEY_F9, "F9"),
        KEY_F10(SourceType.KEYBOARD, GLFW_KEY_F10, "F10"),
        KEY_F11(SourceType.KEYBOARD, GLFW_KEY_F11, "F11"),
        KEY_F12(SourceType.KEYBOARD, GLFW_KEY_F12, "F12"),
        KEY_F13(SourceType.KEYBOARD, GLFW_KEY_F13, "F13"),
        KEY_F14(SourceType.KEYBOARD, GLFW_KEY_F14, "F14"),
        KEY_F15(SourceType.KEYBOARD, GLFW_KEY_F15, "F15"),
        KEY_F16(SourceType.KEYBOARD, GLFW_KEY_F16, "F16"),
        KEY_F17(SourceType.KEYBOARD, GLFW_KEY_F17, "F17"),
        KEY_F18(SourceType.KEYBOARD, GLFW_KEY_F18, "F18"),
        KEY_F19(SourceType.KEYBOARD, GLFW_KEY_F19, "F19"),
        KEY_F20(SourceType.KEYBOARD, GLFW_KEY_F20, "F20"),
        KEY_F21(SourceType.KEYBOARD, GLFW_KEY_F21, "F21"),
        KEY_F22(SourceType.KEYBOARD, GLFW_KEY_F22, "F22"),
        KEY_F23(SourceType.KEYBOARD, GLFW_KEY_F23, "F23"),
        KEY_F24(SourceType.KEYBOARD, GLFW_KEY_F24, "F24"),
        KEY_F25(SourceType.KEYBOARD, GLFW_KEY_F25, "F25"),
        KEY_KP_0(SourceType.KEYBOARD, GLFW_KEY_KP_0, "Keypad 0"),
        KEY_KP_1(SourceType.KEYBOARD, GLFW_KEY_KP_1, "Keypad 1"),
        KEY_KP_2(SourceType.KEYBOARD, GLFW_KEY_KP_2, "Keypad 2"),
        KEY_KP_3(SourceType.KEYBOARD, GLFW_KEY_KP_3, "Keypad 3"),
        KEY_KP_4(SourceType.KEYBOARD, GLFW_KEY_KP_4, "Keypad 4"),
        KEY_KP_5(SourceType.KEYBOARD, GLFW_KEY_KP_5, "Keypad 5"),
        KEY_KP_6(SourceType.KEYBOARD, GLFW_KEY_KP_6, "Keypad 6"),
        KEY_KP_7(SourceType.KEYBOARD, GLFW_KEY_KP_7, "Keypad 7"),
        KEY_KP_8(SourceType.KEYBOARD, GLFW_KEY_KP_8, "Keypad 8"),
        KEY_KP_9(SourceType.KEYBOARD, GLFW_KEY_KP_9, "Keypad 9"),
        KEY_KP_DECIMAL(SourceType.KEYBOARD, GLFW_KEY_KP_DECIMAL, "Keypad ."),
        KEY_KP_DIVIDE(SourceType.KEYBOARD, GLFW_KEY_KP_DIVIDE, "Keypad /"),
        KEY_KP_MULTIPLY(SourceType.KEYBOARD, GLFW_KEY_KP_MULTIPLY, "Keypad *"),
        KEY_KP_SUBTRACT(SourceType.KEYBOARD, GLFW_KEY_KP_SUBTRACT, "Keypad -"),
        KEY_KP_ADD(SourceType.KEYBOARD, GLFW_KEY_KP_ADD, "Keypad +"),
        KEY_KP_ENTER(SourceType.KEYBOARD, GLFW_KEY_KP_ENTER, "Keypad Enter"),
        KEY_KP_EQUAL(SourceType.KEYBOARD, GLFW_KEY_KP_EQUAL, "Keypad ="),
        KEY_LEFT_SHIFT(SourceType.KEYBOARD, GLFW_KEY_LEFT_SHIFT, "Left Shift"),
        KEY_LEFT_CONTROL(SourceType.KEYBOARD, GLFW_KEY_LEFT_CONTROL, "Left Control"),
        KEY_LEFT_ALT(SourceType.KEYBOARD, GLFW_KEY_LEFT_ALT, "Left Alt"),
        KEY_LEFT_SUPER(SourceType.KEYBOARD, GLFW_KEY_LEFT_SUPER, "Left Super"),
        KEY_RIGHT_SHIFT(SourceType.KEYBOARD, GLFW_KEY_RIGHT_SHIFT, "Right Shift"),
        KEY_RIGHT_CONTROL(SourceType.KEYBOARD, GLFW_KEY_RIGHT_CONTROL, "Right Control"),
        KEY_RIGHT_ALT(SourceType.KEYBOARD, GLFW_KEY_RIGHT_ALT, "Right Alt"),
        KEY_RIGHT_SUPER(SourceType.KEYBOARD, GLFW_KEY_RIGHT_SUPER, "Right Super"),
        KEY_MENU(SourceType.KEYBOARD, GLFW_KEY_MENU, "Menu"),
        MOUSE_BUTTON_LEFT(SourceType.MOUSE_BUTTON, GLFW_MOUSE_BUTTON_1, "Left Mouse Button"),
        MOUSE_BUTTON_RIGHT(SourceType.MOUSE_BUTTON, GLFW_MOUSE_BUTTON_2, "Right Mouse Button"),
        MOUSE_BUTTON_MIDDLE(SourceType.MOUSE_BUTTON, GLFW_MOUSE_BUTTON_3, "Middle Mouse Button"),
        MOUSE_BUTTON_4(SourceType.MOUSE_BUTTON, GLFW_MOUSE_BUTTON_4, "Mouse 4"),
        MOUSE_BUTTON_5(SourceType.MOUSE_BUTTON, GLFW_MOUSE_BUTTON_5, "Mouse 5"),
        MOUSE_BUTTON_6(SourceType.MOUSE_BUTTON, GLFW_MOUSE_BUTTON_6, "Mouse 6"),
        MOUSE_BUTTON_7(SourceType.MOUSE_BUTTON, GLFW_MOUSE_BUTTON_7, "Mouse 7"),
        MOUSE_BUTTON_8(SourceType.MOUSE_BUTTON, GLFW_MOUSE_BUTTON_8, "Mouse 8"),
        MOUSE_SCROLL_LEFT(SourceType.MOUSE_SCROLL, "Scroll Left"),
        MOUSE_SCROLL_RIGHT(SourceType.MOUSE_SCROLL, "Scroll Right"),
        MOUSE_SCROLL_DOWN(SourceType.MOUSE_SCROLL, "Scroll Down"),
        MOUSE_SCROLL_UP(SourceType.MOUSE_SCROLL, "Scroll Up"),
        GAMEPAD_BUTTON_CROSS(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_CROSS, "A / Cross"),
        GAMEPAD_BUTTON_CIRCLE(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_CIRCLE, "B / Circle"),
        GAMEPAD_BUTTON_SQUARE(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_SQUARE, "X / Square"),
        GAMEPAD_BUTTON_TRIANGLE(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_TRIANGLE, "Y / Triangle"),
        GAMEPAD_BUTTON_LEFT_BUMPER(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, "Left Bumper"),
        GAMEPAD_BUTTON_RIGHT_BUMPER(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, "Right Bumper"),
        GAMEPAD_BUTTON_BACK(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_BACK, "Back"),
        GAMEPAD_BUTTON_START(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_START, "Start"),
        GAMEPAD_BUTTON_GUIDE(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_GUIDE, "Guide"),
        GAMEPAD_BUTTON_LEFT_THUMB(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_LEFT_THUMB, "Left Thumbstick"),
        GAMEPAD_BUTTON_RIGHT_THUMB(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, "Right Thumbstick"),
        GAMEPAD_BUTTON_DPAD_UP(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_DPAD_UP, "D-pad Up"),
        GAMEPAD_BUTTON_DPAD_RIGHT(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, "D-pad Right"),
        GAMEPAD_BUTTON_DPAD_DOWN(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_DPAD_DOWN, "D-pad Down"),
        GAMEPAD_BUTTON_DPAD_LEFT(SourceType.GAMEPAD_BUTTON, GLFW_GAMEPAD_BUTTON_DPAD_LEFT, "D-pad Left"),
        GAMEPAD_AXIS_LEFT_STICK_LEFT(SourceType.GAMEPAD_AXIS, "Left Stick Left"),
        GAMEPAD_AXIS_LEFT_STICK_RIGHT(SourceType.GAMEPAD_AXIS, "Left Stick Right"),
        GAMEPAD_AXIS_LEFT_STICK_DOWN(SourceType.GAMEPAD_AXIS, "Left Stick Down"),
        GAMEPAD_AXIS_LEFT_STICK_UP(SourceType.GAMEPAD_AXIS, "Left Stick Up"),
        GAMEPAD_AXIS_RIGHT_STICK_LEFT(SourceType.GAMEPAD_AXIS, "Right Stick Left"),
        GAMEPAD_AXIS_RIGHT_STICK_RIGHT(SourceType.GAMEPAD_AXIS, "Right Stick Right"),
        GAMEPAD_AXIS_RIGHT_STICK_DOWN(SourceType.GAMEPAD_AXIS, "Right Stick Down"),
        GAMEPAD_AXIS_RIGHT_STICK_UP(SourceType.GAMEPAD_AXIS, "Right Stick Up"),
        GAMEPAD_AXIS_LEFT_TRIGGER(SourceType.GAMEPAD_AXIS, "Left Trigger"),
        GAMEPAD_AXIS_RIGHT_TRIGGER(SourceType.GAMEPAD_AXIS, "Right Trigger");
        
        public final SourceType TYPE;
        public final int ID;
        public final String NAME;
        
        Source(SourceType type, int id, String name)
        {
            TYPE = type;
            ID = id;
            NAME = name;
        }
        
        Source(SourceType type, String name)
        {
            this(type, 0, name);
        }
    }
    
    private static final HashMap<Integer, Source> KEY_SOURCES = new HashMap<>();
    private static final HashMap<Integer, Source> MOUSE_BUTTON_SOURCES = new HashMap<>();
    private static final HashMap<Integer, Source> GAMEPAD_BUTTON_SOURCES = new HashMap<>();
    
    static
    {
        for (Source source : Source.values()) switch (source.TYPE)
        {
            case KEYBOARD: KEY_SOURCES.put(source.ID, source); break;
            case MOUSE_BUTTON: MOUSE_BUTTON_SOURCES.put(source.ID, source); break;
            case GAMEPAD_BUTTON: GAMEPAD_BUTTON_SOURCES.put(source.ID, source); break;
        }
    }
    
    public static Source getKeySource(int key)
    {
        return KEY_SOURCES.get(key);
    }
    
    public static Source getMouseButtonSource(int button)
    {
        return MOUSE_BUTTON_SOURCES.get(button);
    }
    
    public static Source getGamepadButtonSource(int button)
    {
        return GAMEPAD_BUTTON_SOURCES.get(button);
    }

    private final Game.State gameState;

    private final LinkedHashMap<String, Pair<Source, Source>> defaults = new LinkedHashMap<>(); //Preserves insertion order.

    private final EnumMap<Source, HashSet<String>> srcTgt = new EnumMap(Source.class);
    private final HashMap<String, EnumSet<Source>> tgtSrc = new HashMap<>();
    
    Controls(Game.State gameState)
    {
        this.gameState = gameState;
    }

    /**
     * Adds a default binding, which are used when the controls cannot be loaded from the config file. This order this
     * is called determines the order the controls show up in the settings menu. Both sources may be null to indicate
     * a control that is unbinded by default.
     *
     * If a control is not defined, it cannot be bound to.
     */
    public void addControl(String target, Source mouseKeyboard, Source gamepad)
    {
        gameState.requireNew();

        if (target == null) throw new NullPointerException();
        if (mouseKeyboard != null && mouseKeyboard.TYPE.isGamepad) throw new IllegalStateException("Expected mouse & keyboard source, got " + mouseKeyboard);
        if (gamepad != null && !gamepad.TYPE.isGamepad) throw new IllegalStateException("Expected gamepad source, got " + gamepad);

        defaults.put(target, new Pair<>(mouseKeyboard, gamepad));
    }

    /**
     * Removes a default binding if it exists.
     */
    public Pair<Source, Source> removeControl(String target)
    {
        return defaults.remove(target);
    }

    /**
     * Returns a map view of the default bindings for these Controls. If new defaults are added, the change is
     * reflected by the returned collection. The returned collection is unmodifiable.
     */
    public Map<String, Pair<Source, Source>> getDefaults()
    {
        return Collections.unmodifiableMap(defaults);
    }

    void bind(Source source, String target)
    {
        gameState.requireStarted();

        if (!defaults.containsKey(target)) throw new IllegalArgumentException("No such control: " + target);

        HashSet<String> tgts = srcTgt.get(source);
        if (tgts == null)
        {
            tgts = new HashSet<>(4);
            srcTgt.put(source, tgts);
        }
        tgts.add(target);

        EnumSet<Source> srcs = tgtSrc.get(target);
        if (srcs == null)
        {
            srcs = EnumSet.noneOf(Source.class);
            tgtSrc.put(target, srcs);
        }
        srcs.add(source);
    }

    void unbind(Source source, String target)
    {
        gameState.requireStarted();

        HashSet<String> tgts = srcTgt.get(source);
        if (tgts != null) tgts.remove(target);
        if (tgts.isEmpty()) srcTgt.remove(source);

        EnumSet<Source> srcs = tgtSrc.get(target);
        if (srcs != null) srcs.remove(source);
        if (srcs.isEmpty()) tgtSrc.remove(target);
    }

    void setToDefaults()
    {
        gameState.requireStarted(); //Defaults should be complete.
        srcTgt.clear();
        tgtSrc.clear();
        for (Map.Entry<String, Pair<Source, Source>> e : defaults.entrySet())
        {
            String target = e.getKey();
            Pair<Source, Source> sources = e.getValue();
            if (sources.a != null) bind(sources.a, target);
            if (sources.b != null) bind(sources.b, target);
        }
    }

    void load(JsonObject obj, String name)
    {
        try
        {
            JsonObject obj2 = obj.require(name).asObject();
            for (JsonObject.Member member : obj2) try
            {
                String srcName = member.getName();
                String target = member.getValue().asString();

                Source source = Source.valueOf(srcName);
                bind(source, target);
            }
            catch (Throwable t)
            {
                if (gameState.isDebugEnabled()) System.err.println("Error in bindings: " + t);
            }
        }
        catch (Throwable t)
        {
            if (gameState.isDebugEnabled()) System.err.println("Error reading controls: " + t);
            setToDefaults();
        }
    }

    void save(JsonObject obj, String name)
    {
        JsonObject obj2 = Json.object();
        forEach((src, tgt) -> obj2.add(src.name(), tgt));
        obj.set(name, obj2);
    }

    public boolean isBound(Source source, String target)
    {
        HashSet<String> tgts = srcTgt.get(source);
        if (tgts == null) return false;
        return tgts.contains(target);
    }
    
    public void forEach(BiConsumer<Source, String> callback)
    {
        tgtSrc.forEach((tgt, sources) -> sources.forEach(src -> callback.accept(src, tgt)));
    }
    
    public Set<String> getTargets(Source source)
    {
        if (!srcTgt.containsKey(source)) return Collections.EMPTY_SET;
        return Collections.unmodifiableSet(srcTgt.get(source));
    }
    
    public Set<Source> getSources(String target)
    {
        if (!tgtSrc.containsKey(target)) return Collections.EMPTY_SET;
        return Collections.unmodifiableSet(tgtSrc.get(target));
    }
}
