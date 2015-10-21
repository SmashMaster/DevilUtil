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

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import com.samrj.devil.math.Util;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Samuel Johnson (SmashMaster)
 */
final class OGGDecoder
{
    private static final int BUFFER_SIZE = 2048;
    
    public static final PCMBuffer decode(InputStream in) throws IOException
    {
        if (in == null) throw new NullPointerException();
        
        //Set up JOrbis crap
        Packet packet = new Packet();
        Page page = new Page();
        StreamState stream = new StreamState();
        SyncState sync = new SyncState();

        DspState dsp = new DspState();
        Block block = new Block(dsp);
        Comment comment = new Comment();
        Info info = new Info();
        
        //Init
        sync.init();
        sync.buffer(BUFFER_SIZE);
        byte[] buffer = sync.data;
        
        int count, index = 0;
        
        {//Read header
            boolean reading = true;
            int p = 1;

            while (reading)
            {
                count = in.read(buffer, index, BUFFER_SIZE);
                sync.wrote(count);

                if (p == 1)
                {
                    int pageOutResult = sync.pageout(page);

                    if (pageOutResult == -1)
                        throw new IOException("Hole found in first packet.");

                    if (pageOutResult == 1)
                    {
                        stream.init(page.serialno());
                        stream.reset();
                        info.init();
                        comment.init();

                        if (stream.pagein(page) == -1)
                            throw new IOException("Error in first header page.");

                        if (stream.packetout(packet) != 1)
                            throw new IOException("Error in first header packet.");

                        if (info.synthesis_headerin(comment, packet) < 0)
                            throw new IOException("Error in first packet.");

                        p++;
                    }
                }

                if (p == 2 || p == 3)
                {
                    int pageOutResult = sync.pageout(page);

                    if (pageOutResult == -1)
                        throw new IOException("Hole found in packet " + p + ".");

                    if (pageOutResult == 1)
                    {
                        stream.pagein(page);
                        int packetOutResult = stream.packetout(packet);

                        if (packetOutResult == -1)
                            throw new IOException("Hole found in first packet data.");

                        if (packetOutResult == 1)
                        {
                            info.synthesis_headerin(comment, packet);
                            p++;
                            if (p == 4) reading = false;
                        }
                    }
                }

                index = sync.buffer(BUFFER_SIZE);
                buffer = sync.data;

                if (count == 0 && reading)
                    throw new IOException("Unexpected end of file.");
            }
        }
        
        //Prepare buffers and stuff
        dsp.synthesis_init(info);
        block.init(dsp);
        
        int cbufSize = BUFFER_SIZE*2;
        byte[] cbuf = new byte[cbufSize];
        float[][][] pcmInfo = new float[1][][];
        int[] pcmIndex = new int[info.channels];
        
        PCMBuffer out = new PCMBuffer();
        out.rate = info.rate;
        out.channels = info.channels;
        out.bits = 16;
        
        try
        {
            //Read body
            while (true)
            {
                if (sync.pageout(page) == 1)
                {
                    stream.pagein(page);
                    if (page.granulepos() == 0) break;
                    while (true)
                    {
                        if (stream.packetout(packet) != 1) break;
                        else
                        {
                            if (block.synthesis(packet) == 0)
                                dsp.synthesis_blockin(block);

                            int samples, range;

                            while ((samples = dsp.synthesis_pcmout(pcmInfo, pcmIndex)) > 0)
                            {
                                range = Math.min(samples, cbufSize);

                                for (int i=0; i<info.channels; i++)
                                {
                                    int sampleIndex = i*2;

                                    for (int j=0; j<range; j++)
                                    {
                                        float f = Util.clamp(pcmInfo[0][i][pcmIndex[i] + j], -1.0f, 1.0f);
                                        int value = (int)(f*32767.5f - 0.5f);

                                        cbuf[sampleIndex] = (byte)value;
                                        cbuf[sampleIndex + 1] = (byte)(value >>> 8);

                                        sampleIndex += 2*info.channels;
                                    }
                                }

                                out.buffer.put(cbuf, 0, 2*info.channels*range);
                                dsp.synthesis_read(range);
                            }
                        }
                    }

                    if (page.eos() != 0) break;
                }

                index = sync.buffer(BUFFER_SIZE);
                buffer = sync.data;
                count = in.read(buffer, index, BUFFER_SIZE);
                sync.wrote(count);

                if (count == 0) break;
            }

            stream.clear();
            block.clear();
            dsp.clear();
            info.clear();
            sync.clear();

            in.close();
        }
        catch (Throwable t)
        {
            out.close().free();
            throw t;
        }
        
        return out;
    }
    
    private OGGDecoder()
    {
    }
}
