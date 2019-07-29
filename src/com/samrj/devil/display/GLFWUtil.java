/*
 * Copyright (c) 2019 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.display;

import com.samrj.devil.io.MemStack;
import com.samrj.devil.math.Vec2i;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Utility class for GLFW. GLFW must be initialized for most of these methods to
 * work.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class GLFWUtil
{
    public static Vec2i getWindowPos(long window)
    {
        long x = MemStack.push(4);
        long y = MemStack.push(4);
        nglfwGetWindowPos(window, x, y);
        Vec2i out = new Vec2i(MemoryUtil.memGetInt(x),
                              MemoryUtil.memGetInt(y));
        MemStack.pop(2);
        return out;
    }
    
    public static Vec2i getWindowSize(long window)
    {
        long w = MemStack.push(4);
        long h = MemStack.push(4);
        nglfwGetWindowSize(window, w, h);
        Vec2i out = new Vec2i(MemoryUtil.memGetInt(w),
                              MemoryUtil.memGetInt(h));
        MemStack.pop(2);
        return out;
    }
    
    public static final Vec2i getFramebufferSize(long window)
    {
        long w = MemStack.push(4);
        long h = MemStack.push(4);
        nglfwGetFramebufferSize(window, w, h);
        Vec2i out = new Vec2i(MemoryUtil.memGetInt(w),
                              MemoryUtil.memGetInt(h));
        MemStack.pop(2);
        return out;
    }
    
    public static final FrameSize getWindowFrameSize(long window)
    {
        long left = MemStack.push(4);
        long top = MemStack.push(4);
        long right = MemStack.push(4);
        long bottom = MemStack.push(4);
        nglfwGetWindowFrameSize(window, left, top, right, bottom);
        FrameSize out =  new FrameSize(MemoryUtil.memGetInt(left),
                                       MemoryUtil.memGetInt(top),
                                       MemoryUtil.memGetInt(right),
                                       MemoryUtil.memGetInt(bottom));
        MemStack.pop(4);
        return out;
    }
    
    public static Vec2i getMonitorPos(long monitor)
    {
        long x = MemStack.push(4);
        long y = MemStack.push(4);
        nglfwGetMonitorPos(monitor, x, y);
        Vec2i out = new Vec2i(MemoryUtil.memGetInt(x),
                              MemoryUtil.memGetInt(y));
        MemStack.pop(2);
        return out;
    }
    
    public static Vec2i getMonitorPhysicalSize(long monitor)
    {
        long w = MemStack.push(4);
        long h = MemStack.push(4);
        nglfwGetMonitorPhysicalSize(monitor, w, h);
        Vec2i out = new Vec2i(MemoryUtil.memGetInt(w),
                              MemoryUtil.memGetInt(h));
        MemStack.pop(2);
        return out;
    }
    
    private GLFWUtil()
    {
    }
}
