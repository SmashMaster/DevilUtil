package com.samrj.devil.display;

import java.util.LinkedList;
import java.util.Queue;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class Window
{
    private static final Queue<DisplayException> errors = new LinkedList<>();
    private static boolean initialized = false;
    
    public static void init()
    {
        if (initialized) throw new IllegalStateException("Windowing system already initialized.");
        GLFW.glfwSetErrorCallback(GLFW.GLFWErrorCallback(Window::onError));
        GLFW.glfwInit();
        flushErrors();
        initialized = true;
    }
    
    private static void onError(int errorCode, long descPointer)
    {
        String desc = MemoryUtil.memDecodeUTF8(descPointer);
        errors.add(new DisplayException("GLFW error " + errorCode + ": " + desc));
    }
    
    private static void flushErrors()
    {
        DisplayException error = errors.poll();
        if (error == null) return;
        errors.clear();
        throw error;
    }
    
    public static void terminate()
    {
        if (!initialized) throw new IllegalStateException("Windowing system not initialized.");
        GLFW.glfwTerminate();
        errors.clear();
        initialized = false;
    }
    
    private long id = -1;
    
    public Window(int width, int height, CharSequence title)
    {
        id = GLFW.glfwCreateWindow(width, height, title, 0, 0);
        flushErrors();
        
        GLFW.glfwSetWindowPosCallback(id, GLFW.GLFWWindowPosCallback(this::onMove));
        GLFW.glfwSetWindowSizeCallback(id, GLFW.GLFWWindowSizeCallback(this::onResize));
        GLFW.glfwSetWindowCloseCallback(id, GLFW.GLFWWindowCloseCallback(this::onClose));
        GLFW.glfwSetWindowRefreshCallback(id, GLFW.GLFWWindowRefreshCallback(this::onRefresh));
        GLFW.glfwSetWindowFocusCallback(id, GLFW.GLFWWindowFocusCallback(this::onFocus));
        GLFW.glfwSetWindowIconifyCallback(id, GLFW.GLFWWindowIconifyCallback(this::onIconify));
        GLFW.glfwSetFramebufferSizeCallback(id, GLFW.GLFWFramebufferSizeCallback(this::onFramebufferResize));
    }
    
    public final void destroy()
    {
        GLFW.glfwDestroyWindow(id);
        id = -1;
        flushErrors(); 
    }
    
    public final long getID()
    {
        return id;
    }
    
    //I'm not sure these callbacks will work. Will they be overriden and called properly?
    
    /**
     * Called when the window is moved. The coordinates provided correspond to
     * the top-left corner of the window.
     */
    protected void onMove(int posX, int posY) {}
    
    /**
     * Called when the window is resized.
     */
    protected void onResize(int width, int height) {}
    
    /**
     * Called when the user attempts to close the window, for example by
     * clicking the close widget in the title bar.
     * 
     * <p>The close flag is set before this callback is called, but you can
     * modify it at any time with {@link #setWindowShouldClose setWindowShouldClose}.</p>
     * 
     * <p>The close callback is not triggered by {@link #glfwDestroyWindow DestroyWindow}.</p>
     */
    protected void onClose() {}
    
    /**
     * Called when the client area of the window needs to be redrawn, for
     * example if the window has been exposed after having been covered by
     * another window.
     * 
     * <p>On compositing window systems such as Aero, Compiz or Aqua, where the
     * window contents are saved off-screen, this callback may be called only
     * very infrequently or never at all.</p>
     */
    protected void onRefresh() {}
    
    /**
     * Called when this window gains or loses focus.
     * 
     * <p>After the focus callback is called for a window that lost focus,
     * synthetic key and mouse button release events will be generated for all
     * such that had been pressed.</p>
     */
    protected void onFocus(boolean focused) {}
    
    /**
     * Called when this window is iconified or restored.
     */
    protected void onIconify(boolean iconified) {}
    
    /**
     * Called when this window's framebuffer is resized.
     */
    protected void onFramebufferResize(int width, int height) {}
    
    // <editor-fold defaultstate="collapsed" desc="Callback wrappers">
    private void onMove(long windowID, int posX, int posY)
    {
        assert(windowID == id);
        onMove(posX, posY);
    }
    
    private void onResize(long windowID, int width, int height)
    {
        assert(windowID == id);
        onResize(width, height);
    }
    
    private void onClose(long windowID)
    {
        assert(windowID == id);
        onClose();
    }
    
    private void onRefresh(long windowID)
    {
        assert(windowID == id);
        onRefresh();
    }
    
    private void onFocus(long windowID, int focused)
    {
        assert(windowID == id);
        onFocus(focused == GL11.GL_TRUE);
    }
    
    private void onIconify(long windowID, int iconified)
    {
        assert(windowID == id);
        onIconify(iconified == GL11.GL_TRUE);
    }
    
    private void onFramebufferResize(long windowID, int width, int height)
    {
        assert(windowID == id);
        onFramebufferResize(width, height);
    }
    // </editor-fold>
}