package com.samrj.devil.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public class GLDebug
{
//    public static String getErrorName(int glError)
//    {
//        switch (glError)
//        {
//            case GL11.GL_INVALID_ENUM: return "GL_INVALID_ENUM";
//            default: return "UNKNOWN";
//        }
//    }
    
    private static boolean enabled = false;
    private static GLContext context;
    
    public static void enableDebug(GLContext context)
    {
        if (context == null) throw new NullPointerException();
        GLDebug.context = context;
        enabled = true;
    }
    
    public static void disable()
    {
        context = null;
        enabled = false;
    }
    
    public static void check()
    {
        if (enabled) context.checkGLError();
    }
    
    private GLDebug()
    {
    }
}
