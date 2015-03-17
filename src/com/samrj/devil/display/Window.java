package com.samrj.devil.display;

import java.util.LinkedList;
import java.util.Queue;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

/**
 * Window class for creating OpenGL contexts via the GLFW library.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public class Window
{
    private static final Queue<DisplayException> errors = new LinkedList<>();
    private static boolean initialized = false;
    
    private static int toEnum(boolean bool)
    {
        return bool ? GL11.GL_TRUE : GL11.GL_FALSE;
    }
    
    private static void onError(int errorCode, long descPointer)
    {
        String desc = MemoryUtil.memDecodeUTF8(descPointer);
        errors.add(new DisplayException(errorCode, desc));
    }
    
    /**
     * Flushes any errors stored by onError() callback. Should be called after
     * most GLFW library calls.
     */
    private static void flushErrors()
    {
        DisplayException error = errors.poll();
        if (error == null) return;
        errors.clear();
        throw error;
    }
    
    /**
     * This function initializes the GLFW library. Before most GLFW functions
     * can be used, GLFW must be initialized, and before an application
     * terminates GLFW should be terminated in order to free any resources
     * allocated during or after initialization.
     * 
     * <p>If this function fails, it throws a DisplayException and the library
     * is terminated. If it succeeds, you should call terminate before the
     * application exits.</p>
     * 
     * <p>Additional calls to this function after successful initialization but
     * before termination will throw an IllegalStateException.</p>
     */
    public static void init()
    {
        if (initialized) throw new IllegalStateException("Windowing system already initialized.");
        GLFW.glfwSetErrorCallback(GLFW.GLFWErrorCallback(Window::onError));
        GLFW.glfwInit();
        flushErrors();
        initialized = true;
    }
    
    /**
     * This function destroys all remaining windows and cursors, restores any
     * modified gamma ramps and frees any other allocated resources. Once this
     * function is called, you must again call init successfully before you
     * will be able to use most GLFW functions.
     * 
     * <p>If GLFW has been successfully initialized, this function should be
     * called before the application exits. If initialization fails, there is no
     * need to call this function, as it is called by init before it throws an
     * exception.</p>
     * 
     * <p>No window's context may be current on another thread when this
     * function is called.</p>
     */
    public static void terminate()
    {
        if (!initialized) throw new IllegalStateException("Windowing system not initialized.");
        GLFW.glfwTerminate();
        errors.clear();
        initialized = false;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Hints">
    /**
     * Not including context-related hints because those should be handled
     * directly by DevilGL. We should always use the core profile with forward-
     * compatibility turned on.
     */
    
    /**
     * Resets all window hints back to their default values.
     */
    public static void resetHints()
    {
        GLFW.glfwDefaultWindowHints();
        flushErrors();
    }
    
    /**
     * Specifies whether the (windowed mode) window will be resizable by the
     * user. The window will still be resizable using the setSize function.
     * This hint is ignored for full screen windows.
     * 
     * <p>Defaults to true.</p>
     */
    public static void hintResizable(boolean resizable)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, toEnum(resizable));
        flushErrors();
    }
    
    /**
     * Specifies whether the (windowed mode) window will be initially visible.
     * This hint is ignored for full screen windows.
     * 
     * <p>Defaults to true.</p>
     */
    public static void hintVisible(boolean visible)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, toEnum(visible));
        flushErrors();
    }
    
    /**
     * Specifies whether the (windowed mode) window will have window decorations
     * such as a border, a close widget, etc. This hint is ignored for full
     * screen windows. Note that even though a window may lack a close widget,
     * it is usually still possible for the user to generate close events.
     * 
     * <p>Defaults to true.</p>
     */
    public static void hintDecorated(boolean decorated)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, toEnum(decorated));
        flushErrors();
    }
    
    /**
     * Specifies whether the (windowed mode) window will be given input focus
     * when created. This hint is ignored for full screen and initially hidden
     * windows.
     * 
     * <p>Defaults to true.</p>
     */
    public static void hintFocused(boolean focused)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, toEnum(focused));
        flushErrors();
    }
    
    /**
     * Specifies whether the (full screen) window will automatically iconify and
     * restore the previous video mode on input focus loss. This hint is ignored
     * for windowed mode windows.
     * 
     * <p>Defaults to true.</p>
     */
    public static void hintAutoIconify(boolean autoIconify)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, toEnum(autoIconify));
        flushErrors();
    }
    
    /**
     * Specifies whether the window will be floating above other regular
     * windows, also called topmost or always-on-top. This is intended primarily
     * for debugging purposes and cannot be used to implement proper full screen
     * windows. This hint is ignored for full screen windows.
     * 
     * <p>Defaults to false.</p>
     */
    public static void hintFloating(boolean floating)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, toEnum(floating));
        flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the red component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     */
    public static void hintRedBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, bits);
        flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the green component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     */
    public static void hintGreenBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, bits);
        flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the blue component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     */
    public static void hintBlueBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, bits);
        flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the alpha component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     */
    public static void hintAlphaBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, bits);
        flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the depth component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 24.</p>
     */
    public static void hintDepthBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, bits);
        flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the stencil component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     */
    public static void hintStencilBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, bits);
        flushErrors();
    }
    
    /**
     * Specifies the desired number of samples to use for multisampling. Zero
     * disables multisampling. Any negative value means the application has no
     * preference.
     * 
     * <p>Defaults to 0.</p>
     */
    public static void hintSamples(int samples)
    {
        if (samples < 0) samples = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, samples);
        flushErrors();
    }
    
    /**
     * Specifies whether the framebuffer should be sRGB capable.
     * 
     * <p>Defaults to false.</p>
     */
    public static void hintSRGBCapable(boolean srgbCapable)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_SRGB_CAPABLE, toEnum(srgbCapable));
        flushErrors();
    }
    
    /**
     * Specifies whether the framebuffer should be double buffered. You nearly
     * always want to use double buffering. This is a hard constraint.
     * 
     * <p>Defaults to true.</p>
     */
    public static void hintDoubleBuffer(boolean doubleBuffer)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_DOUBLE_BUFFER, toEnum(doubleBuffer));
        flushErrors();
    }
    
    /**
     * Specifies the desired refresh rate for full screen windows. If set to
     * any negative value, the highest available refresh rate will be used. This
     * hint is ignored for windowed mode windows.
     * 
     * <p>Defaults to highest available refresh rate.</p>
     */
    public static void hintRefreshRate(int refreshRate)
    {
        if (refreshRate < 0) refreshRate = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, refreshRate);
        flushErrors();
    }
    // </editor-fold>
    
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
        flushErrors();
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
    
    // <editor-fold defaultstate="collapsed" desc="Callbacks">
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
    // </editor-fold>
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