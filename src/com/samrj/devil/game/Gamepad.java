package com.samrj.devil.game;

import com.samrj.devil.util.IntList;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryStack;

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
     * Returns a list of all gamepad IDs that are currently present.
     */
    public static IntList getAll()
    {
        IntList out = new IntList();
        for (int id=0; id<=GLFW_JOYSTICK_LAST; id++) if (glfwJoystickIsGamepad(id))
            out.add(id);
        return out;
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
    
    private final List<ButtonPressInterface> buttonCallbacks;
    
    /**
     * Creates a new view of the given gamepad ID. If the given gamepad id is
     * not present, throws an IllegalStateException.
     */
    public Gamepad(int id)
    {
        this.id = id;
        if (!isPresent()) throw new IllegalStateException();
        name = glfwGetGamepadName(id);
        
        buttonCallbacks = new ArrayList<>();
    }
    
    public void addButtonCallback(ButtonPressInterface callback)
    {
        if (callback == null) throw new NullPointerException();
        buttonCallbacks.add(callback);
    }
    
    public boolean isPresent()
    {
        return glfwJoystickIsGamepad(id);
    }
    
    public void update()
    {
        if (!isPresent()) return;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            GLFWGamepadState state = GLFWGamepadState.mallocStack(stack);
            glfwGetGamepadState(id, state);
            for (int i=0; i<axes.length; i++) axes[i] = state.axes(i);
            for (int i=0; i<buttons.length; i++)
            {
                int button = i;
                int action = state.buttons(i);
                if (action != buttons[i])
                    buttonCallbacks.forEach(callback -> callback.onButton(button, action));
                buttons[i] = action;
            }
        }
    }
    
    @FunctionalInterface
    public interface ButtonPressInterface
    {
        public void onButton(int button, int action);
    }
}
