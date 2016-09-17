package com.samrj.devil.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;

/**
 * Simple gamepad class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Gamepad
{
    public final int id;
    public final String name;
    public final float[] axes;
    public final int[] buttons;
    
    private final List<ButtonPressInterface> buttonCallbacks;
    
    Gamepad(int id)
    {
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
    
    void update()
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
    
    void disconnect()
    {
        
    }
    
    @FunctionalInterface
    public interface ButtonPressInterface
    {
        public void onButton(int button, int action);
    }
}
