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

package com.samrj.devil.al;

import com.samrj.devil.math.Vec3;
import org.kc7bfi.jflac.FLACDecoder;
import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryStack;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * DevilAL. An object-oriented OpenAL wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class DAL
{
    //Constant fields
    private static boolean init;
    private static long device, context;
    private static Thread thread;
    private static ALCapabilities capabilities;
    private static Set<DALObj> objects;
    private static boolean debug;
    private static Thread debugShutdownHook;
    
    static void checkState()
    {
        if (!init) throw new IllegalStateException("DAL not initialized.");
        if (Thread.currentThread() != thread)
            throw new IllegalThreadStateException("DAL initialized on different thread.");
    }
    
    /**
     * Initializes DevilAL. Must be called from a thread on which an OpenAL
     * context is current.
     */
    public static void init()
    {
        if (init) throw new IllegalStateException("DAL already initialized.");
        thread = Thread.currentThread();
        
        device = ALC10.alcOpenDevice((ByteBuffer)null);
        if (device == NULL)
            throw new RuntimeException("Failed to create OpenAL context.");
        
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        
        context = ALC10.alcCreateContext(device, (IntBuffer)null);
        ALC10.alcMakeContextCurrent(context);
        
        capabilities = AL.createCapabilities(deviceCaps);
        objects = Collections.newSetFromMap(new IdentityHashMap<>());
        
        debugShutdownHook = new Thread(() ->
        {
            if (debug && init)
            {
                for (DALObj obj : objects) obj.debugLeakTrace();
                System.err.println("DevilUtil (DAL) - DAL not terminated before JVM shut down!");
            }
        });
        Runtime.getRuntime().addShutdownHook(debugShutdownHook);
        
        if (debug) System.err.println("DevilUtil (DAL) - OpenAL debug enabled.");
        
        init = true;
    }
    
    /**
     * Enables error-checking and resource leak tracking. Note: Leak tracking
     * works only for objects that were constructed *after* debug was enabled,
     * so it is best to enable debug before initializing DevilAL.
     */
    public static void setDebugEnabled(boolean debug)
    {
        DAL.debug = debug;
    }
    
    /**
     * Returns whether this DAL context has debug enabled.
     */
    public static boolean isDebugEnabled()
    {
        return debug;
    }
    
    /**
     * @return The current OpenAL context's capabilities.
     */
    public static ALCapabilities getCapabilities()
    {
        checkState();
        return capabilities;
    }
    
    private static <T extends DALObj> T gen(T obj)
    {
        objects.add(obj);
        return obj;
    }
    
    public static void setListenPos(Vec3 v)
    {
        alListener3f(AL10.AL_POSITION, v.x, v.y, v.z);
        checkError();
    }
    
    public static void setListenVel(Vec3 v)
    {
        alListener3f(AL10.AL_POSITION, v.x, v.y, v.z);
        checkError();
    }
    
    public static void setListenDir(Vec3 look, Vec3 up)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.mallocFloat(6);
            look.write(buffer);
            up.write(buffer);
            buffer.flip();
            alListenerfv(AL10.AL_ORIENTATION, buffer);
        }
        checkError();
    }
    
    public static void setListenGain(float gain)
    {
        alListenerf(AL10.AL_GAIN, gain);
        checkError();
    }
    
    public static SoundBuffer decodeFlac(InputStream in) throws IOException
    {
        FLACDecoder decoder = new FLACDecoder(in);
        PCMBufferFLAC buffer = new PCMBufferFLAC();
        try
        {
            decoder.addPCMProcessor(buffer);
            decoder.decode();
        }
        catch (Throwable t) //Prevent memory leaks by passing through throwables.
        {
            memFree(buffer.close());
            throw t;
        }
        in.close();
        return gen(new SoundBuffer(buffer));
    }
    
    public static SoundBuffer decodeFlac(String path) throws IOException
    {
        return decodeFlac(new FileInputStream(path));
    }
    
    public static SoundBuffer decodeOgg(InputStream in) throws IOException
    {
        throw new UnsupportedOperationException();
    }
    
    public static SoundBuffer decodeOgg(String path) throws IOException
    {
        return decodeOgg(new FileInputStream(path));
    }
    
    public static Sound genSound()
    {
        return gen(new Sound());
    }
    
    private static Sound load(SoundBuffer buffer)
    {
        Sound sound = genSound();
        sound.buffer(buffer);
        delete(buffer);
        return sound;
    }
    
    public static Sound loadFlac(InputStream in) throws IOException
    {
        return load(decodeFlac(in));
    }
    
    public static Sound loadFlac(String path) throws IOException
    {
        return load(decodeFlac(path));
    }
    
    public static Sound loadOgg(InputStream in) throws IOException
    {
        return load(decodeOgg(in));
    }
    
    public static Sound loadOgg(String path) throws IOException
    {
        return load(decodeOgg(path));
    }
    
    public static Sound loadSound(String path) throws IOException
    {
        int i = path.lastIndexOf('.');
        if (i == -1) throw new IllegalArgumentException("No extension found.");
        
        String ext = path.substring(i+1).toLowerCase(Locale.ENGLISH);
        switch (ext)
        {
            case "ogg": return loadOgg(path);
            case "flac": return loadFlac(path);
        }
        
        throw new IllegalArgumentException("Unsupported audio format: " + ext);
    }
    
    public static Source genSource()
    {
        return gen(new Source());
    }
    
    public static EffectSlot genEffectSlot()
    {
        return gen(new EffectSlot());
    }
    
    public static Effect genEffect()
    {
        return gen(new Effect());
    }
    
    public static Filter genFilter()
    {
        return gen(new Filter());
    }
    
    public static void checkError()
    {
        if (isDebugEnabled())
        {
            int errorCode = alGetError();
            if (errorCode != AL_NO_ERROR) throw new DALException(errorCode);
        }
    }

    public static void checkError(Object message)
    {
        try
        {
            checkError();
        }
        catch (DALException e)
        {
            throw new DALException(e, message.toString());
        }
    }

    /**
     * Deletes each DevilGL object in the given array.
     * 
     * @param objects An array of DevilGL objects to delete.
     */
    public static void delete(DALObj... objects)
    {
        for (DALObj object : objects)
        {
            object.delete();
            DAL.objects.remove(object);
        }
    }
    
    /**
     * Ends playback for all current sources and detaches their sounds. Useful
     * for ending playback before cleaning up resources, as a Sound cannot be
     * deleted if it is attached to any Source.
     */
    public static void detachAllSounds()
    {
        for (DALObj obj : objects) if (obj instanceof Source)
        {
            Source source = (Source)obj;
            source.stop();
            source.detatchAll();
        }
    }
    
    /**
     * Destroys DAL and releases native resources allocated through init().
     * Native resources allocated through the genXXX() or loadXXX() methods
     * must be freed explicitly through delete() before calling destroy().
     */
    public static void destroy()
    {
        checkState();
        init = false;
        if (isDebugEnabled()) for (DALObj obj : objects) obj.debugLeakTrace();
        Runtime.getRuntime().removeShutdownHook(debugShutdownHook);
        objects = null;
        
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
        
        context = 0;
        device = 0;
        
        thread = null;
        capabilities = null;
        
        debug = false;
    }
}
