/*
 * Copyright (c) 2015 Sam Johnson
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

import com.samrj.devil.io.Memory;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.res.Resource;
import com.samrj.devil.util.IdentitySet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import org.kc7bfi.jflac.FLACDecoder;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALCapabilities;

/**
 * DevilAL. An object-oriented OpenAL wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class DAL
{
    //Constant fields
    private static boolean init;
    private static Thread thread;
    private static ALCapabilities capabilities;
    private static Set<DALObj> objects;
    
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
        if (init) throw new IllegalStateException("DGL already initialized.");
        thread = Thread.currentThread();
        capabilities = AL.getCapabilities();
        objects = new IdentitySet<>();
        init = true;
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
        AL10.alListener3f(AL10.AL_POSITION, v.x, v.y, v.z);
    }
    
    public static void setListenVel(Vec3 v)
    {
        AL10.alListener3f(AL10.AL_POSITION, v.x, v.y, v.z);
    }
    
    public static void setListenDir(Vec3 look, Vec3 up)
    {
        Memory m = Memory.wrapv(look, up);
        AL10.nalListenerfv(AL10.AL_ORIENTATION, m.address);
        m.free();
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
        catch (Throwable t) //Catch all possible exceptions/errors.
        {
            buffer.close().free();
            throw t;
        }
        return gen(new SoundBuffer(buffer));
    }
    
    public static SoundBuffer decodeFlac(String path) throws IOException
    {
        return decodeFlac(Resource.open(path));
    }
    
    public static SoundBuffer decodeOgg(InputStream in) throws IOException
    {
        PCMBuffer buffer = OGGDecoder.decode(in);
        return gen(new SoundBuffer(buffer));
    }
    
    public static SoundBuffer decodeOgg(String path) throws IOException
    {
        return decodeOgg(Resource.open(path));
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
     * Destroys DevilAL and releases all associated resources.
     */
    public static void destroy()
    {
        checkState();
        init = false;
        
        for (DALObj obj : objects) obj.delete();
        objects.clear();
        objects = null;
        
        thread = null;
        capabilities = null;
    }
}
