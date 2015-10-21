package com.samrj.devil.al;

import com.samrj.devil.io.Memory;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.res.Resource;
import com.samrj.devil.util.IdentitySet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.kc7bfi.jflac.FLACDecoder;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALContext;

/**
 * DevilAL. An object-oriented OpenAL wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DAL
{
    //Constant fields
    private static boolean init;
    private static Thread thread;
    private static ALContext context;
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
        context = ALContext.create();
        capabilities = context.getCapabilities();
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
    
    public static SoundBuffer bufferFlac(InputStream in) throws IOException
    {
        FLACDecoder decoder = new FLACDecoder(in);
        PCMBuffer buffer = new PCMBuffer();
        try
        {
            decoder.addPCMProcessor(buffer);
            decoder.decode();
        }
        catch (IOException e)
        {
            buffer.close().free();
            throw e;
        }
        return gen(new SoundBuffer(buffer));
    }
    
    public static SoundBuffer bufferFlac(String path) throws IOException
    {
        return bufferFlac(Resource.open(path));
    }
    
    public static Sound genSound()
    {
        return gen(new Sound());
    }
    
    public static Sound loadFlac(InputStream in) throws IOException
    {
        SoundBuffer buffer = bufferFlac(in);
        Sound sound = genSound();
        sound.buffer(buffer);
        delete(buffer);
        return sound;
    }
    
    public static Sound loadFlac(String path) throws IOException
    {
        SoundBuffer buffer = bufferFlac(path);
        Sound sound = genSound();
        sound.buffer(buffer);
        delete(buffer);
        return sound;
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
        AL.destroy(context);
        context = null;
        capabilities = null;
    }
}
