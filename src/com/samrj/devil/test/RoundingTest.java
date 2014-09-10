package com.samrj.devil.test;

import com.samrj.devil.math.Util;
import com.samrj.devil.ui.KeyEvent;
import com.samrj.devil.ui.MouseEvent;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class RoundingTest implements Test
{
    public RoundingTest()
    {
        float f1 = 1.25f;
        float f2 = -1.25f;
        System.out.println("floor(" + f1 + ") = " + Util.floor(f1) + " = " + Math.floor(f1));
        System.out.println("floor(" + f2 + ") = " + Util.floor(f2) + " = " + Math.floor(f2));
        
        System.out.println("ceil(" + f1 + ") = " + Util.ceil(f1) + " = " + Math.ceil(f1));
        System.out.println("ceil(" + f2 + ") = " + Util.ceil(f2) + " = " + Math.ceil(f2));
        
        System.out.println("round(" + f1 + ") = " + Util.round(f1) + " = " + Math.round(f1));
        System.out.println("round(" + f2 + ") = " + Util.round(f2) + " = " + Math.round(f2));
        
        float f3 = 1.75f;
        float f4 = -1.75f;
        System.out.println("round(" + f3 + ") = " + Util.round(f3) + " = " + Math.round(f3));
        System.out.println("round(" + f4 + ") = " + Util.round(f4) + " = " + Math.round(f4));
    }

    @Override
    public void in(MouseEvent in)
    {
    }

    @Override
    public void in(KeyEvent in)
    {
    }

    @Override
    public void step(float dt)
    {
    }

    @Override
    public void render()
    {
    }
}
