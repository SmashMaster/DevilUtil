package com.samrj.devil.util;

import com.samrj.devil.al.DAL;
import com.samrj.devil.game.GameWindow;
import com.samrj.devil.game.GameWindowMode;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.graphics.EZDraw;
import com.samrj.devil.gui.DUI;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import org.lwjgl.system.APIUtil;

import javax.swing.*;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;

/**
 * Function plotter.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Plotter implements GameWindowMode
{
    private final ArrayList<Plot> plots = new ArrayList<>();
    private float x0 = 0.0f, x1 = 1.0f, y0 = 0.0f, y1 = 1.0f;
    private final Mat4 projection = Mat4.identity();

    public Plotter()
    {
        try
        {
            GameWindow.setDebug(true);

            GameWindow.hint(GLFW_ALPHA_BITS, 0);
            GameWindow.hint(GLFW_DEPTH_BITS, 0);
            GameWindow.hint(GLFW_STENCIL_BITS, 0);
            GameWindow.hint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
            GameWindow.hint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            GameWindow.hint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            GameWindow.hint(GLFW_CONTEXT_VERSION_MINOR, 2);
            GameWindow.hint(GLFW_SAMPLES, 8);

            GameWindow.setTitle("DevilUtil Plotter");
            GameWindow.setResolution(768, 768);
            GameWindow.onInit(() ->
            {
                glClearColor(0.25f, 0.25f, 0.25f, 1.0f);
                glClearDepth(1.0);
                glEnable(GL_MULTISAMPLE);
                DGL.init();
                DUI.init();
                EZDraw.init(65536*4);

                GameWindow.setCallbacks(this);
            });
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            APIUtil.DEBUG_STREAM.close(); //Prevent LWJGL leak message spam.
            System.exit(-1);
        }
    }

    @FunctionalInterface
    public interface FloatFunction
    {
        float apply(float v);
    }

    private class Plot
    {
        private final Vec3 color = new Vec3();
        private final FloatFunction function;

        private Plot(float r, float g, float b, FloatFunction function)
        {
            color.set(r, g, b);
            this.function = function;
        }
    }

    public Plotter viewport(float x0, float x1, float y0, float y1)
    {
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        projection.setOrthographic(x0, x1, y0, y1, -1.0f, 1.0f);
        return this;
    }

    public Plotter plot(float r, float g, float b, FloatFunction function)
    {
        plots.add(new Plot(r, g, b, function));
        return this;
    }

    public Plotter run()
    {
        GameWindow.run();
        return this;
    }

    @Override
    public void beforeInput()
    {
    }

    @Override
    public void afterInput()
    {
    }

    @Override
    public void resize(int width, int height)
    {
    }

    @Override
    public void mouseMoved(float x, float y)
    {
    }

    @Override
    public void mouseButton(int button, int action, int mods)
    {
    }

    @Override
    public void mouseScroll(float dx, float dy)
    {
    }

    @Override
    public void key(int key, int action, int mods)
    {
    }

    @Override
    public void character(char character, int codepoint)
    {
    }

    @Override
    public void step(float dt)
    {
    }

    @Override
    public void render()
    {
        glClear(GL_COLOR_BUFFER_BIT);

        EZDraw.setProjection(projection);

        EZDraw.color(0.5f, 0.5f, 0.5f);
        EZDraw.begin(GL_LINES);
        EZDraw.vertex(x0, 0.0f);
        EZDraw.vertex(x1, 0.0f);
        EZDraw.vertex(0.0f, y0);
        EZDraw.vertex(0.0f, y1);
        EZDraw.end();

        for (Plot plot : plots)
        {
            EZDraw.color(plot.color);
            EZDraw.begin(GL_LINE_STRIP);
            int points = GameWindow.getResolution().x;
            for (int i=0; i<points; i++)
            {
                float x = Util.lerp(x0, x1, (i + 0.5f)/points);
                float y = plot.function.apply(x);
                EZDraw.vertex(x, y);
            }
            EZDraw.end();
        }
    }

    @Override
    public void destroy(boolean crashed)
    {
        EZDraw.destroy();
        DUI.destroy();

        if (crashed)
        {
            DAL.setDebugEnabled(false);
            DGL.setDebugLeakTracking(false);
        }

        DGL.destroy();
    }
}
