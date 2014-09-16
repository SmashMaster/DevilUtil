package com.samrj.devil.sound;

import com.samrj.devil.res.Resource;
import java.io.IOException;
import java.io.InputStream;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Sound
{
    private int id = -1;
    
    public Sound(Resource res) throws IOException
    {
        InputStream in = res.open();
        id = AL10.alGenBuffers();
        
        WaveData data = WaveData.create(in);
        AL10.alBufferData(id, data.format, data.data, data.samplerate);
        data.dispose();
    }
    
    public Sound(String path) throws IOException
    {
        this(Resource.find(path));
    }
    
    public int id()
    {
        return id;
    }

    public void glDelete()
    {
        AL10.alDeleteBuffers(id);
        id = -1;
    }
}
