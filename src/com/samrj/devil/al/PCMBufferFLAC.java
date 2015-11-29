package com.samrj.devil.al;

import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
final class PCMBufferFLAC extends PCMBuffer implements PCMProcessor
{
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
}
