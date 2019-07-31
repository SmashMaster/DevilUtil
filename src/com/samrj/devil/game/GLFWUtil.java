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

package com.samrj.devil.game;

import com.samrj.devil.math.Vec2i;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

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
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            long x = stack.nmalloc(4);
            long y = stack.nmalloc(4);
            nglfwGetWindowPos(window, x, y);
            Vec2i out = new Vec2i(memGetInt(x),
                                  memGetInt(y));
            return out;
        }
    }
    
    public static Vec2i getWindowSize(long window)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            long w = stack.nmalloc(4);
            long h = stack.nmalloc(4);
            nglfwGetWindowSize(window, w, h);
            Vec2i out = new Vec2i(memGetInt(w),
                                  memGetInt(h));
            return out;
        }
    }
    
    public static final Vec2i getFramebufferSize(long window)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            long w = stack.nmalloc(4);
            long h = stack.nmalloc(4);
            nglfwGetFramebufferSize(window, w, h);
            Vec2i out = new Vec2i(memGetInt(w),
                                  memGetInt(h));
            return out;
        }
    }
    
    public static final FrameSize getWindowFrameSize(long window)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            long left = stack.nmalloc(4);
            long top = stack.nmalloc(4);
            long right = stack.nmalloc(4);
            long bottom = stack.nmalloc(4);
            nglfwGetWindowFrameSize(window, left, top, right, bottom);
            FrameSize out =  new FrameSize(memGetInt(left),
                                           memGetInt(top),
                                           memGetInt(right),
                                           memGetInt(bottom));
            return out;
        }
    }
    
    public static Vec2i getMonitorPos(long monitor)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            long x = stack.nmalloc(4);
            long y = stack.nmalloc(4);
            nglfwGetMonitorPos(monitor, x, y);
            Vec2i out = new Vec2i(memGetInt(x),
                                  memGetInt(y));
            return out;
        }
    }
    
    public static Vec2i getMonitorPhysicalSize(long monitor)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            long w = stack.nmalloc(4);
            long h = stack.nmalloc(4);
            nglfwGetMonitorPhysicalSize(monitor, w, h);
            Vec2i out = new Vec2i(memGetInt(w),
                                  memGetInt(h));
            return out;
        }
    }
    
    private GLFWUtil()
    {
    }
}
