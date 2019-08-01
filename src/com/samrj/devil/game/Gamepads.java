package com.samrj.devil.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Class which automatically handles connection and disconnection of gamepads.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Gamepads
{
    private final Gamepad[] array = new Gamepad[GLFW_JOYSTICK_LAST + 1];
    private final List<Consumer<Gamepad>> connectCallbacks = new ArrayList<>();
    
    Gamepads()
    {
        for (int joystick=0; joystick<GLFW_JOYSTICK_LAST; joystick++)
            if (glfwJoystickPresent(joystick))
                array[joystick] = new Gamepad(joystick);
        
        glfwSetJoystickCallback(this::onJoyStick);
    }
    
    private void onJoyStick(int joystick, int event)
    {
        synchronized (array)
        {
            switch (event)
            {
                case GLFW_CONNECTED:
                    Gamepad gamepad = new Gamepad(joystick);
                    array[joystick] = gamepad;
                    connectCallbacks.forEach(c -> c.accept(gamepad));
                    break;
                case GLFW_DISCONNECTED:
                    array[joystick].disconnect();
                    array[joystick] = null;
                    break;
            }
        }
    }
    
    public void forEach(Consumer<Gamepad> consumer)
    {
        synchronized (array)
        {
            for (Gamepad gamepad : array) if (gamepad != null) consumer.accept(gamepad);
        }
    }
    
    public List<Gamepad> getAll()
    {
        List<Gamepad> list = new ArrayList<>();
        forEach(list::add);
        return list;
    }
    
    public Gamepad getFirst()
    {
        synchronized (array)
        {
            for (Gamepad gamepad : array) if (gamepad != null) return gamepad;
            return null;
        }
    }
    
    void update()
    {
        forEach(Gamepad::update);
    }
    
    void destroy()
    {
        glfwSetJoystickCallback(null).free();
    }
    
    public void addConnectCallback(Consumer<Gamepad> callback)
    {
        synchronized (array)
        {
            if (callback == null) throw new NullPointerException();
            connectCallbacks.add(callback);
        }
    }
}
