package com.samrj.devil.game;

import com.samrj.devil.util.IntList;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Simple gamepad class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Gamepad
{
    /**
     * Returns an array of all gamepad IDs that are currently present.
     */
    public static int[] getAll()
    {
        IntList out = new IntList();
        for (int id=0; id<=GLFW_JOYSTICK_LAST; id++) if (glfwJoystickIsGamepad(id))
            out.add(id);
        return out.toArray();
    }
    
    /**
     * Returns the first present Gamepad, or null if none could be found.
     */
    public static Gamepad getFirst()
    {
        for (int id=0; id<=GLFW_JOYSTICK_LAST; id++) if (glfwJoystickIsGamepad(id))
            return new Gamepad(id);
        return null;
    }
    
    /**
     * Finds the first Gamepad which is present and matches the given name.
     */
    public static Gamepad search(String name)
    {
        for (int id=0; id<=GLFW_JOYSTICK_LAST; id++) if (glfwJoystickIsGamepad(id))
        {
            String gpName = glfwGetGamepadName(id);
            if (gpName.equals(name)) return new Gamepad(id);
        }
        return null;
    }
    
    /**
     * Returns the first gamepad which is present and has the given name. If it
     * couldn't be found, returns the first present gamepad, or null if none are
     * present.
     */
    public static Gamepad prefer(String name)
    {
        Gamepad out = search(name);
        if (out != null) return out;
        return getFirst();
    }
    
    public final int id;
    public final String name;
    public final float[] axes = new float[GLFW_GAMEPAD_AXIS_LAST + 1];
    public final int[] buttons = new int[GLFW_GAMEPAD_BUTTON_LAST + 1];
    
    private final List<AxisCallback> axisCallbacks = new ArrayList<>();
    private final List<ButtonCallback> buttonCallbacks = new ArrayList<>();
    
    /**
     * Creates a new view of the given gamepad ID. If the given gamepad id is
     * not present, throws an IllegalStateException.
     */
    public Gamepad(int id)
    {
        this.id = id;
        if (!isPresent()) throw new IllegalStateException();
        name = glfwGetGamepadName(id);
    }
    
    public void addAxisCallback(AxisCallback callback)
    {
        if (callback == null) throw new NullPointerException();
        axisCallbacks.add(callback);
    }
    
    public void addButtonCallback(ButtonCallback callback)
    {
        if (callback == null) throw new NullPointerException();
        buttonCallbacks.add(callback);
    }
    
    public boolean isPresent()
    {
        return glfwJoystickIsGamepad(id);
    }
    
    /**
     * If the gamepad has been disconnected, or if focus has been lost, this
     * method may be used to reset its state for all axes and buttons to zero.
     */
    public void zeroOut()
    {
        for (int i=0; i<GLFW_GAMEPAD_AXIS_LEFT_TRIGGER; i++) if (axes[i] != 0.0f)
        {
            axes[i] = 0.0f;
            for (AxisCallback callback : axisCallbacks) callback.accept(i, 0.0f);
        }
        
        //'zero' for the triggers is actually -1.0
        if (axes[GLFW_GAMEPAD_AXIS_LEFT_TRIGGER] != -1.0f)
        {
            axes[GLFW_GAMEPAD_AXIS_LEFT_TRIGGER] = -1.0f;
            for (AxisCallback callback : axisCallbacks) callback.accept(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER, -1.0f);
        }
        
        if (axes[GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER] != -1.0f)
        {
            axes[GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER] = -1.0f;
            for (AxisCallback callback : axisCallbacks) callback.accept(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER, -1.0f);
        }

        for (int i=0; i<buttons.length; i++) if (buttons[i] != GLFW_RELEASE)
        {
            buttons[i] = GLFW_RELEASE;
            for (ButtonCallback callback : buttonCallbacks) callback.accept(i, GLFW_RELEASE);
        }
    }
    
    public void update()
    {
        if (!isPresent()) return;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            GLFWGamepadState state = GLFWGamepadState.mallocStack(stack);
            glfwGetGamepadState(id, state);
            
            for (int i=0; i<axes.length; i++)
            {
                float val = state.axes(i);
                if (val != axes[i])
                {
                    axes[i] = val;
                    for (AxisCallback callback : axisCallbacks) callback.accept(i, val);
                }
            }
            
            for (int i=0; i<buttons.length; i++)
            {
                int val = state.buttons(i);
                if (val != buttons[i])
                {
                    buttons[i] = val;
                    for (ButtonCallback callback : buttonCallbacks) callback.accept(i, val);
                }
            }
        }
    }
    
    @FunctionalInterface
    public interface AxisCallback
    {
        public void accept(int axis, float x);
    }
    
    @FunctionalInterface
    public interface ButtonCallback
    {
        public void accept(int button, int action);
    }
}
