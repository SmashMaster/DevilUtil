package com.samrj.devil.ui;

import com.samrj.devil.math.Vector2f;
import java.util.LinkedHashSet;
import java.util.Set;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Input
{
    private static Vector2f mousePos = new Vector2f();
    private static Set<Integer> downButtons = new LinkedHashSet<>();
    private static Set<Integer> downKeys = new LinkedHashSet<>();
    
    public static boolean isButtonDown(int button)
    {
        return downButtons.contains(button);
    }
    
    public static void setButtonState(int button, boolean state)
    {
        if (state) downButtons.add(button);
        else downButtons.remove(button);
    }
    
    public static Set<Integer> downButtons()
    {
        Set out = new LinkedHashSet<>();
        out.addAll(downButtons);
        return out;
    }
    
    public static boolean isKeyDown(int key)
    {
        return downKeys.contains(key);
    }
    
    public static void setKeyState(int key, boolean state)
    {
        if (state) downKeys.add(key);
        else downKeys.remove(key);
    }
    
    public static Set<Integer> downKeys()
    {
        Set out = new LinkedHashSet<>();
        out.addAll(downKeys);
        return out;
    }
    
    public static Vector2f getMousePos()
    {
        return mousePos.clone();
    }
    
    public static void step(Element e)
    {
        while (Mouse.next())
        {
            MouseEvent out = new MouseEvent();
            out.pos.set(Mouse.getEventX(), Mouse.getEventY());
            out.dp.set(Mouse.getEventDX(), Mouse.getEventDY());
            out.button = Mouse.getEventButton();
            out.state = Mouse.getEventButtonState();
            out.dWheel = Mouse.getEventDWheel()/120;
            out.time = Mouse.getEventNanoseconds();
            
            mousePos.set(out.pos);
            
            if (out.state) downButtons.add(out.button);
            else           downButtons.remove(out.button);
            
            if (e != null) e.in(out);
        }

        while (Keyboard.next())
        {
            KeyEvent out = new KeyEvent();
            out.key = Keyboard.getEventKey();
            out.state = Keyboard.getEventKeyState();
            out.letter = Keyboard.getEventCharacter();
            out.time = Keyboard.getEventNanoseconds();
            
            if (out.state) downKeys.add(out.key);
            else           downKeys.remove(out.key);
            
            if (e != null) e.in(out);
        }
    }
    
    public static void step()
    {
        step(null);
    }
    
    private Input() {}
}