package com.samrj.devil.test;

import com.samrj.devil.ui.Input;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

/**
 * Testbed for DevilUtil.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class TestBed
{
    private static boolean running = false;
    
    public static void main(String[] args) throws Exception
    {
        Display.setDisplayMode(new DisplayMode(1280, 720));
        Display.setVSyncEnabled(false);
        Display.create(new PixelFormat(0, 0, 0));
        
        Test test = new IntegrationTest();//Your desired test here.
        
        running = true;
        while (running)
        {
            Display.processMessages();
            
            Input.step(test);
            test.step(1f/60f);
            test.render();
            
            Display.update(false);
            Display.sync(60);
        }
        
        Display.destroy();
    }
    
    public static void stop()
    {
        running = false;
    }
}
