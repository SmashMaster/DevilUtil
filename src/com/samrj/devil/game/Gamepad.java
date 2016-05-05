package com.samrj.devil.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * Simple gamepad class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Gamepad
{
    public static Gamepad getFirstPresent()
    {
        for (int i=0; i<16; i++) if (present(i))
            return new Gamepad(i);
        return null;
    }
    
    public static boolean present(int id)
    {
        return GLFW.glfwJoystickPresent(id) == GL11.GL_TRUE;
    }
    
    public final int id;
    public final String name;
    public final float[] axes;
    public final int[] buttons;
    
    private final List<ButtonPressInterface> buttonCallbacks;
    
    public Gamepad(int id)
    {
        if (!present(id)) throw new IllegalStateException("Gamepad " + id + " not present.");
        
        this.id = id;
        name = GLFW.glfwGetJoystickName(id);
        
        FloatBuffer axesBuf = GLFW.glfwGetJoystickAxes(id);
        axes = new float[axesBuf.remaining()];
        for (int i=0; i<axes.length; i++) axes[i] = axesBuf.get();
        
        ByteBuffer butBuf = GLFW.glfwGetJoystickButtons(id);
        buttons = new int[butBuf.remaining()];
        for (int i=0; i<buttons.length; i++) buttons[i] = butBuf.get();
        
        buttonCallbacks = new ArrayList<>();
    }
    
    public void addButtonCallback(ButtonPressInterface callback)
    {
        buttonCallbacks.add(callback);
    }
    
    public void update()
    {
        FloatBuffer axesBuf = GLFW.glfwGetJoystickAxes(id);
        for (int i=0; i<axes.length; i++) axes[i] = axesBuf.get();
        
        ByteBuffer butBuf = GLFW.glfwGetJoystickButtons(id);
        for (int i=0; i<buttons.length; i++)
        {
            int button = i;
            int action = butBuf.get();
            if (action != buttons[i])
                buttonCallbacks.forEach(callback -> callback.onButton(button, action));
            buttons[i] = action;
        }
    }
    
    @FunctionalInterface
    public interface ButtonPressInterface
    {
        public void onButton(int button, int action);
    }
}
