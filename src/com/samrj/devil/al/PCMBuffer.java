package com.samrj.devil.al;

import com.samrj.devil.io.DynamicBuffer;
import com.samrj.devil.io.Memory;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
final class PCMBuffer implements PCMProcessor
{
    int rate, channels, bits;
    final DynamicBuffer buffer;
    
    PCMBuffer()
    {
        buffer = new DynamicBuffer();
    }
    
    @Override
    public void processStreamInfo(StreamInfo streamInfo)
    {
        rate = streamInfo.getSampleRate();
        channels = streamInfo.getChannels();
        bits = streamInfo.getBitsPerSample();
    }

    @Override
    public void processPCM(ByteData pcm)
    {
        byte[] data = pcm.getData();
        int len = pcm.getLen();
        buffer.put(data, 0, len);
    }
    
    Memory close()
    {
        return buffer.close();
    }
}
