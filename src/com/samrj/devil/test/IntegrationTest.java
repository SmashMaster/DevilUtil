package com.samrj.devil.test;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.numerical.Derivative;
import com.samrj.devil.math.numerical.Euler;
import com.samrj.devil.math.numerical.Integrator;
import com.samrj.devil.math.numerical.Midpoint;
import com.samrj.devil.math.numerical.NumState;
import com.samrj.devil.math.numerical.RK4;
import com.samrj.devil.ui.KeyEvent;
import com.samrj.devil.ui.MouseEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * Test for the integrators in the numerical analysis package.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IntegrationTest implements Test
{
    private final Derivative<FloatNum> derv = new TestDerivative();
    private final float start = -360f, end = 360f;
    private final float min = -2f, max = 2f;
    private final float dx = 1f;
    
    public IntegrationTest()
    {
        GL11.glViewport(0, 0, 1280, 720);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(start, end, min, max, -1.0, 1.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }
    
    /**
     * The function to integrate.
     */
    private float f(float t)
    {
//        return t;
        return Util.sin(t);
    }
    
    /**
     * The analytical solution, to compare against the numerical approximation.
     */
    private float F(float t)
    {
//        return t*t*.5f;
        return -Util.cos(t);
    }
    
    @Override
    public void in(MouseEvent in)
    {
    }

    @Override
    public void in(KeyEvent in)
    {
        if (in.state && in.key == Keyboard.KEY_ESCAPE) TestBed.stop();
    }

    @Override
    public void step(float dt)
    {
    }

    @Override
    public void render()
    {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        
        GL11.glColor3f(1f, 1f, 1f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (float x = start; x <= end; x += dx) GL11.glVertex2f(x, f(x));
        GL11.glEnd();
        
        GL11.glColor3f(0f, .5f, 1f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (float x = start; x <= end; x += dx) GL11.glVertex2f(x, F(x));
        GL11.glEnd();
        
        Integrator integrator = new Euler();
//        Integrator integrator = new Midpoint();
//        Integrator integrator = new RK4();
        
        float y = F(start);
        GL11.glColor3f(1f, .5f, 0f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (float x = start; x <= end; x += dx)
        {
            GL11.glVertex2f(x, y);
            y = integrator.integrate(x, dx/56f, new FloatNum(y), derv).f;
        }
        GL11.glEnd();
    }
    
    private class TestDerivative implements Derivative<FloatNum>
    {
        @Override
        public FloatNum getSlope(float t, FloatNum state)
        {
            return new FloatNum(f(t));
        }
    }
    
    private class FloatNum implements NumState<FloatNum>
    {
        private float f;
        
        public FloatNum(float f)
        {
            this.f = f;
        }

        @Override
        public FloatNum add(FloatNum ns)
        {
            f += ns.f;
            return this;
        }

        @Override
        public FloatNum sub(FloatNum ns)
        {
            f -= ns.f;
            return this;
        }

        @Override
        public FloatNum mult(float f)
        {
            this.f *= f;
            return this;
        }

        @Override
        public FloatNum div(float f)
        {
            this.f /= f;
            return this;
        }

        @Override
        public FloatNum clone()
        {
            return new FloatNum(f);
        }
    }
}