package com.samrj.devil.display;

import com.samrj.devil.buffer.BufferUtil;
import com.samrj.devil.math.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

/**
 * Window class for creating OpenGL contexts via the GLFW library.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public class Window
{
    private static boolean initialized = false;
    
    private static int toEnum(boolean bool)
    {
        return bool ? GL11.GL_TRUE : GL11.GL_FALSE;
    }
    
    private static boolean toBool(int flag)
    {
        return flag == GL11.GL_TRUE;
    }
    
    static void ensureInitialized()
    {
        if (!initialized) throw new IllegalStateException("Windowing system not initialized.");
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
        GLFWError.init();
        GLFW.glfwInit();
        GLFWError.flushErrors();
        
        Monitor.init();
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
        ensureInitialized();
        GLFW.glfwTerminate();
        GLFWError.clearErrors();
        initialized = false;
    }
    
    /**
     * This method processes only those events that are already in the event
     * queue and then returns immediately. Processing events will cause the
     * window and input callbacks associated with those events to be called.
     * 
     * <p>On some platforms, a window move, resize or menu operation will cause
     * event processing to block. This is due to how event processing is
     * designed on those platforms. You can use the window refresh callback to
     * redraw the contents of your window when necessary during such operations.</p>
     * 
     * <p>On some platforms, certain events are sent directly to the application
     * without going through the event queue, causing callbacks to be called
     * outside of a call to one of the event processing functions.</p>
     * 
     * <p>Event processing is not required for joystick input to work.</p>
     * 
     * <p>This method may only be called from the main thread.</p>
     */
    public static void pollEvents()
    {
        GLFW.glfwPollEvents();
        GLFWError.flushErrors();
    }
    
    //Static methods needed:
    //glfwPollEvents
    //glfwWaitEvents
    //glfwPostEmptyEvent
    
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
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies whether the (windowed mode) window will be resizable by the
     * user. The window will still be resizable using the setSize function.
     * This hint is ignored for full screen windows.
     * 
     * <p>Defaults to true.</p>
     * 
     * @param resizable Whether the window should be resizable by the user.
     */
    public static void hintResizable(boolean resizable)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, toEnum(resizable));
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies whether the (windowed mode) window will be initially visible.
     * This hint is ignored for full screen windows.
     * 
     * <p>Defaults to true.</p>
     * 
     * @param visible Whether the window should be visible upon creation.
     */
    public static void hintVisible(boolean visible)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, toEnum(visible));
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies whether the (windowed mode) window will have window decorations
     * such as a border, a close widget, etc. This hint is ignored for full
     * screen windows. Note that even though a window may lack a close widget,
     * it is usually still possible for the user to generate close events.
     * 
     * <p>Defaults to true.</p>
     * 
     * @param decorated Whether the window should be decorated.
     */
    public static void hintDecorated(boolean decorated)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, toEnum(decorated));
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies whether the (windowed mode) window will be given input focus
     * when created. This hint is ignored for full screen and initially hidden
     * windows.
     * 
     * <p>Defaults to true.</p>
     * 
     * @param focused Whether the window should be focused upon creation.
     */
    public static void hintFocused(boolean focused)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, toEnum(focused));
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies whether the (full screen) window will automatically iconify and
     * restore the previous video mode on input focus loss. This hint is ignored
     * for windowed mode windows.
     * 
     * <p>Defaults to true.</p>
     * 
     * @param autoIconify Whether the full screen window should automatically
     *                    iconify.
     */
    public static void hintAutoIconify(boolean autoIconify)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, toEnum(autoIconify));
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies whether the window will be floating above other regular
     * windows, also called topmost or always-on-top. This is intended primarily
     * for debugging purposes and cannot be used to implement proper full screen
     * windows. This hint is ignored for full screen windows.
     * 
     * <p>Defaults to false.</p>
     * 
     * @param floating Whether this window should be floating or not.
     */
    public static void hintFloating(boolean floating)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, toEnum(floating));
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the red component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     * 
     * @param bits The bit depth of the red component of the framebuffer.
     */
    public static void hintRedBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, bits);
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the green component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     * 
     * @param bits The bit depth of the green component of the framebuffer.
     */
    public static void hintGreenBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, bits);
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the blue component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     * 
     * @param bits The bit depth of the blue component of the framebuffer.
     */
    public static void hintBlueBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, bits);
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the alpha component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     * 
     * @param bits The bit depth of the alpha component of the framebuffer.
     */
    public static void hintAlphaBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, bits);
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the depth component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 24.</p>
     * 
     * @param bits The bit depth of the depth component of the framebuffer.
     */
    public static void hintDepthBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, bits);
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies the desired bit depth of the stencil component of the default
     * framebuffer. Any negative value means the application has no preference.
     * 
     * <p>Defaults to 8.</p>
     * 
     * @param bits The bit depth of the stencil component of the framebuffer.
     */
    public static void hintStencilBits(int bits)
    {
        if (bits < 0) bits = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, bits);
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies the desired number of samples to use for multisampling. Zero
     * disables multisampling. Any negative value means the application has no
     * preference.
     * 
     * <p>Defaults to 0.</p>
     * 
     * @param samples The number of samples to use for multisampling.
     */
    public static void hintSamples(int samples)
    {
        if (samples < 0) samples = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, samples);
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies whether the framebuffer should be sRGB capable.
     * 
     * <p>Defaults to false.</p>
     * 
     * @param srgbCapable Whether the framebuffer should be sRGB capable.
     */
    public static void hintSRGBCapable(boolean srgbCapable)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_SRGB_CAPABLE, toEnum(srgbCapable));
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies whether the framebuffer should be double buffered. You nearly
     * always want to use double buffering. This is a hard constraint.
     * 
     * <p>Defaults to true.</p>
     * 
     * @param doubleBuffer Whether or not to use double buffering.
     */
    public static void hintDoubleBuffer(boolean doubleBuffer)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_DOUBLE_BUFFER, toEnum(doubleBuffer));
        GLFWError.flushErrors();
    }
    
    /**
     * Specifies the desired refresh rate for full screen windows. If set to
     * any negative value, the highest available refresh rate will be used. This
     * hint is ignored for windowed mode windows.
     * 
     * <p>Defaults to highest available refresh rate.</p>
     * 
     * @param refreshRate The desired refresh rate.
     */
    public static void hintRefreshRate(int refreshRate)
    {
        if (refreshRate < 0) refreshRate = GLFW.GLFW_DONT_CARE;
        GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, refreshRate);
        GLFWError.flushErrors();
    }
    // </editor-fold>
    
    private long id = -1;
    private GLContext context;
    private GLFWWindowPosCallback posCallback;
    private GLFWWindowSizeCallback sizeCallback;
    private GLFWWindowCloseCallback closeCallback;
    private GLFWWindowRefreshCallback refreshCallback;
    private GLFWWindowFocusCallback focusCallback;
    private GLFWWindowIconifyCallback iconifyCallback;
    private GLFWFramebufferSizeCallback framebufferSizeCallback;
    
    /**
     * This function creates a window and its associated OpenGL context. Most of
     * the options controlling how the window and its context should be created
     * are specified with window hints.
     * 
     * <p>Successful creation does not change which context is current. Before
     * you can use the newly created context, you need to make it current.</p>
     * 
     * <p>The created window, framebuffer and context may differ from what you
     * requested, as not all parameters and hints are hard constraints. This
     * includes the size of the window, especially for full screen windows. To
     * query the actual attributes of the created window, framebuffer and
     * context, see glfwGetWindowAttrib, glfwGetWindowSize and
     * glfwGetFramebufferSize.</p>
     * 
     * <p>To create a full screen window, you need to specify the monitor the
     * window will cover. If no monitor is specified, windowed mode will be
     * used. Unless you have a way for the user to choose a specific monitor, it
     * is recommended that you pick the primary monitor.</p>
     * 
     * <p>For full screen windows, the specified size becomes the resolution of
     * the window's desired video mode. As long as a full screen window has
     * input focus, the supported video mode most closely matching the desired
     * video mode is set for the specified monitor.</p>
     * 
     * <p>By default, newly created windows use the placement recommended by the
     * window system. To create the window at a specific position, make it
     * initially invisible using the hintVisible() window hint, set its position
     * and then show it.</p>
     * 
     * <p>If a full screen window has input focus, the screensaver is prohibited
     * from starting.</p>
     * 
     * <p>Window systems put limits on window sizes. Very large or very small
     * window dimensions may be overridden by the window system on creation.
     * Check the actual size after creation. </p>
     * 
     * <p>The swap interval is not set during window creation and the initial
     * value may vary depending on driver settings and defaults.</p>
     * 
     * @param width The desired width, in screen coordinates, of the window.
     * @param height The desired height, in screen coordinates, of the window.
     * @param title The initial window title.
     * @param monitor The monitor to use for full screen mode, or 0 to use
     *                windowed mode.
     */
    public Window(int width, int height, CharSequence title, Monitor monitor)
    {
        id = GLFW.glfwCreateWindow(width, height, title, monitor == null ? 0 : monitor.id, 0);
        GLFWError.flushErrors();
        
        GLFW.glfwSetWindowPosCallback(id, posCallback = GLFW.GLFWWindowPosCallback(this::onMove));
        GLFW.glfwSetWindowSizeCallback(id, sizeCallback = GLFW.GLFWWindowSizeCallback(this::onResize));
        GLFW.glfwSetWindowCloseCallback(id, closeCallback = GLFW.GLFWWindowCloseCallback(this::onClose));
        GLFW.glfwSetWindowRefreshCallback(id, refreshCallback = GLFW.GLFWWindowRefreshCallback(this::onRefresh));
        GLFW.glfwSetWindowFocusCallback(id, focusCallback = GLFW.GLFWWindowFocusCallback(this::onFocus));
        GLFW.glfwSetWindowIconifyCallback(id, iconifyCallback = GLFW.GLFWWindowIconifyCallback(this::onIconify));
        GLFW.glfwSetFramebufferSizeCallback(id, framebufferSizeCallback = GLFW.GLFWFramebufferSizeCallback(this::onFramebufferResize));
        GLFWError.flushErrors();
        
        makeCurrent();
        context = GLContext.createFromCurrent();
    }
    
    /**
     * Creates a window using windowed mode.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @param width The desired width, in screen coordinates, of the window.
     * @param height The desired height, in screen coordinates, of the window.
     * @param title The initial window title.
     */
    public Window(int width, int height, CharSequence title)
    {
        this(width, height, title, null);
    }
    
    /**
     * This method destroys this window and its context. On calling this method,
     * no further callbacks will be called for this window.
     * 
     * <p>If the context of this window is current on the main thread, it is
     * detached before being destroyed.</p>
     * 
     * <p>The context of this window must not be current on any other thread
     * when this method is called.</p>
     * 
     * <p>This method may only be called from the main thread.</p>
     */
    public final void destroy()
    {
        posCallback.release(); posCallback = null;
        sizeCallback.release(); sizeCallback = null;
        closeCallback.release(); closeCallback = null;
        refreshCallback.release(); refreshCallback = null;
        focusCallback.release(); focusCallback = null;
        iconifyCallback.release(); iconifyCallback = null;
        framebufferSizeCallback.release(); framebufferSizeCallback = null;
        
        context.destroy();
        context = null;
        
        GLFW.glfwDestroyWindow(id);
        id = -1;
        GLFWError.flushErrors(); 
    }
    
    /**
     * This method returns the value of the close flag of this window.
     * 
     * <p>This method is not synchronized and may be called from any thread.</p>
     * 
     * @return The value of the close flag.
     */
    public final boolean shouldClose()
    {
        int flag = GLFW.glfwWindowShouldClose(id);
        GLFWError.flushErrors(); 
        return toBool(flag);
    }
    
    /**
     * This method sets the value of the close flag of this window. This can be
     * used to override the user's attempt to close the window, or to signal
     * that it should be closed.
     * 
     * <p>This method is not synchronized and may be called from any thread.</p>
     * 
     * @param shouldClose 
     */
    public final void setShouldClose(boolean shouldClose)
    {
        GLFW.glfwSetWindowShouldClose(id, toEnum(shouldClose));
        GLFWError.flushErrors(); 
    }
    
    /**
     * This method sets this window's title.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @param title The desired title.
     */
    public final void setTitle(String title)
    {
        GLFW.glfwSetWindowTitle(id, title);
        GLFWError.flushErrors();
    }
    
    /**
     * This method retrieves the position, in screen coordinates, of the upper-
     * left corner of the client area of this window.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return The position of the upper-left corner of this window.
     */
    public final Vector2i getPos()
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetWindowPos(id, BufferUtil.pubBufA, BufferUtil.pubBufB);
        GLFWError.flushErrors();
        
        return new Vector2i(BufferUtil.pubBufA.getInt(),
                            BufferUtil.pubBufB.getInt());
    }
    
    /**
     * This method sets the position, in screen coordinates, of the upper-left
     * corner of the client area of this windowed mode window. If the window is
     * a full screen window, this function does nothing.
     * 
     * <p>Do not use this method to move an already visible window unless you
     * have very good reasons for doing so, as it will confuse and annoy the
     * user.</p>
     * 
     * <p>The window manager may put limits on what positions are allowed. GLFW
     * cannot and should not override these limits.</p>
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @param x The x-coordinate of the upper-left corner of the client area.
     * @param y The y-coordinate of the upper-left corner of the client area.
     */
    public final void setPos(int x, int y)
    {
        GLFW.glfwSetWindowPos(id, x, y);
        GLFWError.flushErrors();
    }
    
    /**
     * This method retrieves the size, in screen coordinates, of the client area
     * of the specified window. If you wish to retrieve the size of the
     * framebuffer of the window in pixels, use getFramebufferSize().
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return The size of the client area.
     */
    public final Vector2i getSize()
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetWindowSize(id, BufferUtil.pubBufA, BufferUtil.pubBufB);
        GLFWError.flushErrors();
        
        return new Vector2i(BufferUtil.pubBufA.getInt(),
                            BufferUtil.pubBufB.getInt());
    }
    
    /**
     * This function sets the size, in screen coordinates, of the client area of
     * this window.
     * 
     * <p>For full screen windows, this function selects and switches to the
     * resolution closest to the specified size, without affecting the window's
     * context. As the context is unaffected, the bit depths of the framebuffer
     * remain unchanged.</p>
     * 
     * <p>The window manager may put limits on what sizes are allowed. GLFW
     * cannot and should not override these limits.</p>
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @param x The desired width of the client area.
     * @param y The desired height of the client area.
     */
    public final void setSize(int x, int y)
    {
        GLFW.glfwSetWindowSize(id, x, y);
        GLFWError.flushErrors();
    }
    
    /**
     * This function retrieves the size, in pixels, of the framebuffer of this
     * window. If you wish to retrieve the size of the window in screen
     * coordinates, use getSize().
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return The size of the framebuffer of this window.
     */
    public final Vector2i getFramebufferSize()
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetFramebufferSize(id, BufferUtil.pubBufA, BufferUtil.pubBufB);
        GLFWError.flushErrors();
        
        return new Vector2i(BufferUtil.pubBufA.getInt(),
                            BufferUtil.pubBufB.getInt());
    }
    
    /**
     * This method retrieves the size, in screen coordinates, of each edge of
     * the frame of this window. This size includes the title bar, if the window
     * has one. The size of the frame may vary depending on the window-related
     * hints used to create it.
     * 
     * <p>Because this method retrieves the size of each window frame edge and
     * not the offset along a particular coordinate axis, the retrieved values
     * will always be zero or positive.</p>
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return The size of each edge of the frame of this window.
     */
    public final FrameSize getFrameSize()
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetWindowFrameSize(id, BufferUtil.pubBufA, BufferUtil.pubBufB,
                                        BufferUtil.pubBufC, BufferUtil.pubBufD);
        GLFWError.flushErrors();
        
        return new FrameSize(BufferUtil.pubBufA.getInt(),
                             BufferUtil.pubBufB.getInt(),
                             BufferUtil.pubBufC.getInt(),
                             BufferUtil.pubBufD.getInt());
    }
    
    /**
     * This method iconifies (minimizes) the specified window if it was
     * previously restored. If the window is already iconified, this method
     * does nothing.
     * 
     * <p>If the specified window is a full screen window, the original monitor
     * resolution is restored until the window is restored.</p>
     * 
     * <p>This method may only be called from the main thread.</p>
     */
    public final void iconify()
    {
        GLFW.glfwIconifyWindow(id);
        GLFWError.flushErrors();
    }
    
    /**
     * This method restores the specified window if it was previously iconified
     * (minimized). If the window is already restored, this method does nothing.
     * 
     * <p>If the specified window is a full screen window, the resolution chosen
     * for the window is restored on the selected monitor.</p>
     * 
     * <p>This method may only be called from the main thread.</p>
     */
    public final void restore()
    {
        GLFW.glfwRestoreWindow(id);
        GLFWError.flushErrors();
    }
    
    /**
     * This method makes this window visible if it was previously hidden. If the
     * window is already visible or is in full screen mode, this method does
     * nothing.
     * 
     * <p>This method may only be called from the main thread.</p>
     */
    public final void show()
    {
        GLFW.glfwShowWindow(id);
        GLFWError.flushErrors();
    }
    
    /**
     * This method hides this window if it was previously visible. If the window
     * is already hidden or is in full screen mode, this method does nothing.
     * 
     * <p>This method may only be called from the main thread.</p>
     */
    public final void hide()
    {
        GLFW.glfwHideWindow(id);
        GLFWError.flushErrors();
    }
    
    /**
     * This method returns the monitor that this window is in full screen on.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return The monitor, or null if the window is in windowed mode.
     */
    public final Monitor getMonitor()
    {
        long monitor = GLFW.glfwGetWindowMonitor(id);
        GLFWError.flushErrors();
        
        return Monitor.get(monitor);
    }
    
    /**
     * This method returns the value of an attribute of this window or its
     * OpenGL context.
     * 
     * <p>Framebuffer related hints are not window attributes. You need to use
     * OpenGL calls to query for that information.</p>
     * 
     * <p>See http://www.glfw.org/docs/latest/window.html#window_attribs for a
     * list of window attributes.</p>
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @param attrib The window attribute whose value to return.
     * @return The value of the attribute.
     * @deprecated You need to pass GLFW enums into this function, which is bad
     *             form. A better solution needs to be found.
     */
    @Deprecated
    public final int glfwGetAttrib(int attrib)
    {
        int value = GLFW.glfwGetWindowAttrib(id, attrib);
        GLFWError.flushErrors();
        return value;
    }
    
    /**
     * This method swaps the front and back buffers of this window. If the swap
     * interval is greater than zero, the GPU driver waits the specified number
     * of screen updates before swapping the buffers.
     * 
     * <p>This method is not synchronized and may be called from any thread.</p>
     */
    public final void swapBuffers()
    {
        GLFW.glfwSwapBuffers(id);
        GLFWError.flushErrors();
    }
    
    /**
     * This method makes the OpenGL context of this window current on the
     * calling thread. A context can only be made current on a single thread at
     * a time and each thread can have only a single current context at a time.
     * 
     * <p>By default, making a context non-current implicitly forces a pipeline
     * flush. On machines that support GL_KHR_context_flush_control, you can
     * control whether a context performs this flush by setting the
     * GLFW_CONTEXT_RELEASE_BEHAVIOR window hint.</p>
     */
    public final void makeCurrent()
    {
        GLFW.glfwMakeContextCurrent(id);
        GLFWError.flushErrors();
    }
    
    long getID()
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