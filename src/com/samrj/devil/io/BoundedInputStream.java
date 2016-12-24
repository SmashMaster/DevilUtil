package com.samrj.devil.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Stream wrapper which supplies a limited number of bytes. Useful for reading
 * parts of files with an InputStream.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class BoundedInputStream extends InputStream
{
    private final InputStream in;
    private final long size;
    private long pos = 0;
    private long mark = -1;
    
    public BoundedInputStream(InputStream in, long size)
    {
        if (size < 0) throw new IllegalArgumentException();
        
        this.in = in;
        this.size = size;
    }
    
    @Override
    public int read() throws IOException
    {
        if (pos >= size) return -1;
        int result = in.read();
        pos++;
        return result;
    }
    
    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (pos >= size) return -1;
        
        len = (int)Math.min(len, size - pos);
        int numRead = in.read(b, off, len);
        if (numRead == -1) return -1;
        
        pos += numRead;
        return numRead;
    }
    
    @Override
    public long skip(long n) throws IOException
    {
        n = Math.min(n, size - pos);
        long numSkipped = in.skip(n);
        pos += numSkipped;
        return numSkipped;
    }
    
    @Override
    public int available() throws IOException
    {
        if (pos >= size) return 0;
        return (int)Math.min(size - pos, in.available());
    }
    
    @Override
    public void close() throws IOException
    {
        in.close();
    }
    
    @Override
    public synchronized void reset() throws IOException
    {
        in.reset();
        pos = mark;
    }
    
    
    @Override
    public synchronized void mark(int readlimit)
    {
        in.mark(readlimit);
        mark = pos;
    }
    
    @Override
    public boolean markSupported()
    {
        return in.markSupported();
    }
}
