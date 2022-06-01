package com.samrj.devil.game;

import com.samrj.devil.game.config.Config;
import com.samrj.devil.game.config.Controls;
import com.samrj.devil.game.config.Controls.Source;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.util.Pair;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Some things to consider here:
 * 
 *      *Mouse cursor input needs to be handled as a special case. Pass it
 * through to the gamemode as usual, but actually check for bindings to enable
 * that code. Would also like an elegant way to handle mouse input for boolean
 * events. Maybe some form of accumulator.
 *      *Store the state of targets, and combine across all sources. Allow this
 * state to be zeroed out when the game loses focus.
 *      *Callbacks are to be used exclusively for boolean values, and occur when
 * a value crosses a specific threshold. The get() method may be used to check
 * the current value of a target.
 *      *The value provided by get() may exceed 1.0 in many cases.
 */
public final class Input
{
    public static final float BOOLEAN_THRESHOLD = 0.5f;
    
    private final Config config;
    private final Callback callback;
    private final HashMap<String, Integer> indices = new HashMap<>();
    private final String[] targets;
    private final EnumSet<Source>[] active;
    private final float[][] state;
    private final Vec2 leftStick = new Vec2();
    private final Vec2 rightStick = new Vec2();
    
    public Input(Game.State gameState, Config config, Callback callback)
    {
        gameState.requireStarted();

        this.config = config;
        this.callback = callback;

        Map<String, Pair<Source, Source>> defaults = config.controls.getDefaults();

        targets = new String[defaults.size()];
        active = new EnumSet[defaults.size()];
        state = new float[defaults.size()][Source.values().length];

        int i=0;
        for (String target : defaults.keySet())
        {
            targets[i] = target;
            indices.put(target, i);
            active[i] = EnumSet.noneOf(Source.class);
            i++;
        }
    }
    
    public float get(String target)
    {
        float sum = 0.0f;
        int index = indices.get(target);
        for (Source src : active[index]) sum += state[index][src.ordinal()];
        return sum;
    }
    
    public boolean getAsBoolean(String target)
    {
        return get(target) >= BOOLEAN_THRESHOLD;
    }
    
    private void set(Source source, float value)
    {
        if (source == null) return;

        for (String target : config.controls.getTargets(source))
        {
            int index = indices.get(target);
            float prevValue = state[index][source.ordinal()];
            if (value == prevValue) continue;
            
            float prevSum = get(target);
            state[index][source.ordinal()] = value;
            if (prevValue == 0.0f) active[index].add(source);
            if (value == 0.0f) active[index].remove(source);
            float sum = get(target);
            
            if (prevSum < BOOLEAN_THRESHOLD && sum >= BOOLEAN_THRESHOLD) callback.accept(target, true);
            if (prevSum >= BOOLEAN_THRESHOLD && sum < BOOLEAN_THRESHOLD) callback.accept(target, false);
        }
    }
    
    public void key(int key, int action, int mods)
    {
        set(Controls.getKeySource(key), action != GLFW_RELEASE ? 1.0f : 0.0f);
    }
    
    public void mouseButton(int button, int action, int mods)
    {
        set(Controls.getMouseButtonSource(button), action != GLFW_RELEASE ? 1.0f : 0.0f);
    }
    
    private void mouseScrollSet(Source source, float value)
    {
        //Need special handling for mouse scrolling, since it never 'zeroes' out.
        set(source, value);
        set(source, 0.0f);
    }
    
    public void mouseScroll(float dx, float dy)
    {
        if (dx < 0.0f) mouseScrollSet(Source.MOUSE_SCROLL_LEFT, -dx);
        if (dx > 0.0f) mouseScrollSet(Source.MOUSE_SCROLL_RIGHT, dx);
        if (dy < 0.0f) mouseScrollSet(Source.MOUSE_SCROLL_DOWN, -dy);
        if (dy > 0.0f) mouseScrollSet(Source.MOUSE_SCROLL_UP, dy);
    }
    
    public void gamepadButton(int button, int action)
    {
        set(Controls.getGamepadButtonSource(button), action != GLFW_RELEASE ? 1.0f : 0.0f);
    }
    
    private void updateLeftStick()
    {
        Vec2 leftStickDZ = new Vec2();
        Util.deadZone(leftStick, config.gamepadDeadzone.get()/100.0f, leftStickDZ);
        
        if (leftStickDZ.x < 0.0f)
        {
            set(Source.GAMEPAD_AXIS_LEFT_STICK_LEFT, -leftStickDZ.x);
            set(Source.GAMEPAD_AXIS_LEFT_STICK_RIGHT, 0.0f);
        }
        else
        {
            set(Source.GAMEPAD_AXIS_LEFT_STICK_LEFT, 0.0f);
            set(Source.GAMEPAD_AXIS_LEFT_STICK_RIGHT, leftStickDZ.x);
        }
        
        if (leftStickDZ.y < 0.0f)
        {
            set(Source.GAMEPAD_AXIS_LEFT_STICK_DOWN, -leftStickDZ.y);
            set(Source.GAMEPAD_AXIS_LEFT_STICK_UP, 0.0f);
        }
        else
        {
            set(Source.GAMEPAD_AXIS_LEFT_STICK_DOWN, 0.0f);
            set(Source.GAMEPAD_AXIS_LEFT_STICK_UP, leftStickDZ.y);
        }
    }
    
    private void updateRightStick()
    {
        Vec2 rightStickDZ = new Vec2();
        Util.deadZone(rightStick, config.gamepadDeadzone.get()/100.0f, rightStickDZ);
        
        if (rightStickDZ.x < 0.0f)
        {
            set(Source.GAMEPAD_AXIS_RIGHT_STICK_LEFT, -rightStickDZ.x);
            set(Source.GAMEPAD_AXIS_RIGHT_STICK_RIGHT, 0.0f);
        }
        else
        {
            set(Source.GAMEPAD_AXIS_RIGHT_STICK_LEFT, 0.0f);
            set(Source.GAMEPAD_AXIS_RIGHT_STICK_RIGHT, rightStickDZ.x);
        }
        
        if (rightStickDZ.y < 0.0f)
        {
            set(Source.GAMEPAD_AXIS_RIGHT_STICK_DOWN, -rightStickDZ.y);
            set(Source.GAMEPAD_AXIS_RIGHT_STICK_UP, 0.0f);
        }
        else
        {
            set(Source.GAMEPAD_AXIS_RIGHT_STICK_DOWN, 0.0f);
            set(Source.GAMEPAD_AXIS_RIGHT_STICK_UP, rightStickDZ.y);
        }
    }
    
    public void gamepadAxis(int axis, float value)
    {
        switch (axis)
        {
            case GLFW_GAMEPAD_AXIS_LEFT_TRIGGER:
                set(Source.GAMEPAD_AXIS_LEFT_TRIGGER, value*0.5f + 0.5f);
                break;
            case GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER:
                set(Source.GAMEPAD_AXIS_RIGHT_TRIGGER, value*0.5f + 0.5f);
                break;
            case GLFW_GAMEPAD_AXIS_LEFT_X:
                leftStick.x = value;
                updateLeftStick();
                break;
            case GLFW_GAMEPAD_AXIS_LEFT_Y:
                leftStick.y = -value;
                updateLeftStick();
                break;
            case GLFW_GAMEPAD_AXIS_RIGHT_X:
                rightStick.x = value;
                updateRightStick();
                break;
            case GLFW_GAMEPAD_AXIS_RIGHT_Y:
                rightStick.y = -value;
                updateRightStick();
                break;
        }
    }
    
    @FunctionalInterface
    public interface Callback
    {
        void accept(String target, boolean active);
    }
}
