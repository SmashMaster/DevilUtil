package com.samrj.devil.display;

import com.samrj.devil.buffer.BufferUtil;
import com.samrj.devil.math.Vec2i;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;

/**
 * Monitor class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public final class Monitor
{
    private static final Map<Long, Monitor> monitors = new LinkedHashMap<>();
    private static Monitor primary;
    
    static void init()
    {
        PointerBuffer pBuffer = GLFW.glfwGetMonitors();
        
        while (pBuffer.hasRemaining())
        {
            long id = pBuffer.get();
            monitors.put(id, new Monitor(id));
        }
        
        primary = monitors.get(GLFW.glfwGetPrimaryMonitor());
        
        GLFW.glfwSetMonitorCallback(GLFW.GLFWMonitorCallback(Monitor::callback));
        GLFWError.flushErrors();
    }
    
    static Monitor get(long id)
    {
        if (id == 0L) return null;
        
        Monitor monitor = monitors.get(id);
        if (monitor == null)
        {
            monitor = new Monitor(id);
            monitors.put(id, monitor);
        }
        return monitor;
    }
    
    private static void callback(long id, int event)
    {
        Monitor monitor = monitors.get(id);
        
        if (monitor != null) switch (event)
        {
            case GLFW.GLFW_CONNECTED: monitor.connected = true;
                break;
            case GLFW.GLFW_DISCONNECTED: monitor.connected = false;
                break;
        }
    }
    
    /**
     * This method returns a list of all currently connected monitors.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return 
     */
    public static Collection<Monitor> getMonitors()
    {
        Window.ensureInitialized();
        return Collections.unmodifiableCollection(monitors.values());
    }
    
    /**
     * Returns the primary monitor. This is usually the monitor where elements
     * like the Windows task bar or the OS X menu bar is located.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return The primary monitor.
     */
    public static Monitor getPrimaryMonitor()
    {
        Window.ensureInitialized();
        return primary;
    }
    
    final long id;
    private final String name;
    private final List<VideoMode> videoModes;
    private boolean connected = true;
    
    private Monitor(long id)
    {
        this.id = id;
        videoModes = VideoMode.getAll(id);
        name = GLFW.glfwGetMonitorName(id);
        GLFWError.flushErrors();
    }
    
    public boolean isConnected()
    {
        return connected;
    }
    
    /**
     * This method returns the position, in screen coordinates, of the upper-
     * left corner of this monitor.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return the position of the upper-left corner of this monitor.
     */
    public Vec2i getPos()
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetMonitorPos(id, BufferUtil.pubBufA, BufferUtil.pubBufB);
        GLFWError.flushErrors();
        
        return new Vec2i(BufferUtil.pubBufA.getInt(),
                         BufferUtil.pubBufB.getInt());
    }
    
    /**
     * This method returns the size, in millimetres, of the display area of this
     * monitor.
     * 
     * Some systems do not provide accurate monitor size information, either
     * because the monitor EDID data is incorrect or because the driver does not
     * report it accurately.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return The size, in millimetres, of the display area of this monitor.
     */
    public Vec2i getPhysicalSize()
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetMonitorPhysicalSize(id, BufferUtil.pubBufA, BufferUtil.pubBufB);
        GLFWError.flushErrors();
        
        return new Vec2i(BufferUtil.pubBufA.getInt(),
                         BufferUtil.pubBufB.getInt());
    }
    
    /**
     * This method returns a human-readable name for this monitor. The name
     * typically reflects the make and model of the monitor and is not
     * guaranteed to be unique among the connected monitors.
     * 
     * @return A name for this monitor.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * This method returns a list of all video modes supported by this monitor.
     * The returned list is sorted in ascending order, first by color bit depth
     * (the sum of all channel depths) and then by resolution area (the product
     * of width and height).
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return A list of all video modes supported by this monitor.
     */
    public Collection<VideoMode> getVideoModes()
    {
        return Collections.unmodifiableCollection(videoModes);
    }
    
    /**
     * This method returns the current video mode of this monitor. If you have
     * created a full screen window for this monitor, the return value will
     * depend on whether that window is iconified.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return The current video mode of the specified monitor.
     */
    public VideoMode getVideoMode()
    {
        ByteBuffer buffer = GLFW.glfwGetVideoMode(id);
        GLFWError.flushErrors();
        return new VideoMode(buffer);
    }
    
    /**
     * This method generates a 256-element gamma ramp from the specified
     * exponent and then calls setGammaRamp with it. The value must be a finite
     * number greater than zero.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @param gamma The desired gamma exponent.
     */
    public void setGamma(float gamma)
    {
        GLFW.glfwSetGamma(id, gamma);
        GLFWError.flushErrors();
    }
    
    /**
     * This method returns the current gamma ramp of this monitor.
     * 
     * <p>This method may only be called from the main thread.</p>
     * 
     * @return The current gamma ramp of this monitor.
     */
    public GammaRamp getGammaRamp()
    {
        GammaRamp gammaRamp = new GammaRamp(GLFW.glfwGetGammaRamp(id));
        GLFWError.flushErrors();
        return gammaRamp;
    }
    
    /**
     * This method sets the current gamma ramp for this monitor. The original
     * gamma ramp for this monitor is saved by GLFW the first time this method
     * is called and is restored by Window.terminate().
     * 
     * <p>This method may only be called from the main thread.</p>
     *
     * @param gammaRamp The desired gamma ramp.
     */
    public void setGammaRamp(GammaRamp gammaRamp)
    {
        GLFW.glfwSetGammaRamp(id, gammaRamp.toBuffer());
        GLFWError.flushErrors();
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
