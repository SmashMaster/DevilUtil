package com.samrj.devil.game;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final Gamepad[] ARRAY = new Gamepad[GLFW_JOYSTICK_LAST + 1];
    private static final List<Consumer<Gamepad>> CONNECT_CALLBACKS = new ArrayList<>();
    
    static void init()
    {
        for (int joystick=0; joystick<GLFW_JOYSTICK_LAST; joystick++)
            if (glfwJoystickPresent(joystick))
                ARRAY[joystick] = new Gamepad(joystick);
        
        glfwSetJoystickCallback(Gamepads::onJoyStick);
    }
    
    private static void onJoyStick(int joystick, int event)
    {
        synchronized (ARRAY)
        {
            switch (event)
            {
                case GLFW_CONNECTED:
                    Gamepad gamepad = new Gamepad(joystick);
                    ARRAY[joystick] = gamepad;
                    CONNECT_CALLBACKS.forEach(c -> c.accept(gamepad));
                    break;
                case GLFW_DISCONNECTED:
                    ARRAY[joystick].disconnect();
                    ARRAY[joystick] = null;
                    break;
            }
        }
    }
    
    public static void forEach(Consumer<Gamepad> consumer)
    {
        synchronized (ARRAY)
        {
            for (Gamepad gamepad : ARRAY) if (gamepad != null) consumer.accept(gamepad);
        }
    }
    
    public static List<Gamepad> getAll()
    {
        List<Gamepad> list = new ArrayList<>();
        forEach(list::add);
        return list;
    }
    
    public static Gamepad getFirst()
    {
        synchronized (ARRAY)
        {
            for (Gamepad gamepad : ARRAY) if (gamepad != null) return gamepad;
            return null;
        }
    }
    
    static void update()
    {
        forEach(Gamepad::update);
    }
    
    public static void addConnectCallback(Consumer<Gamepad> callback)
    {
        synchronized (ARRAY)
        {
            if (callback == null) throw new NullPointerException();
            CONNECT_CALLBACKS.add(callback);
        }
    }
    
    static final void terminate()
    {
        synchronized (ARRAY)
        {
            forEach(Gamepad::disconnect);
            Arrays.fill(ARRAY, null);
            glfwSetJoystickCallback(null);
            CONNECT_CALLBACKS.clear();
        }
    }
    
    private Gamepads()
    {
    }
}
