package com.samrj.devil.res;

import java.io.*;
import java.net.URI;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FileRes extends Resource
{
    public static FileRes find(String path) throws IOException
    {
        if (path == null) throw new NullPointerException();
        return find(new File(path));
    }
    
    public static FileRes findSilent(String path)
    {
        if (path == null) return null;
        return findSilent(new File(path));
    }
    
    public static boolean isValid(String path)
    {
        return findSilent(path) != null;
    }
    
    public static FileRes find(File file) throws IOException
    {
        if (file == null) throw new NullPointerException();
        if (!file.isFile()) throw new FileNotFoundException(file.toString());
        if (!file.canRead())  throw new IOException("Cannot read file " + file);
        
        File workingDir = new File(System.getProperty("user.dir"));

        URI fileURI = file.toURI();
        URI relURI = workingDir.toURI().relativize(fileURI);

        if (relURI != fileURI)
        {
            String path = relURI.getPath();
            return new FileRes(path, file);
        }
        else throw new AbsoluteFileException(file.toString());
    }
    
    public static FileRes findSilent(File file)
    {
        if (file == null || !file.isFile() || !file.canRead()) return null;
        
        File workingDir = new File(System.getProperty("user.dir"));

        URI fileURI = file.toURI();
        URI relURI = workingDir.toURI().relativize(fileURI);

        if (relURI != fileURI)
        {
            String path = relURI.getPath();
            return new FileRes(path, file);
        }
        
        return null;
    }
    
    public static boolean isValid(File file)
    {
        return findSilent(file) != null;
    }
    
    public final File file;
    
    FileRes(String path, File file)
    {
        super(path);
        this.file = file;
    }
    
    @Override
    public boolean canOpen()
    {
        return file.canRead();
    }

    @Override
    public InputStream open() throws IOException
    {
        if (!canOpen()) throw new IOException("Can no longer read file " + path);
        return new FileInputStream(file);
    }
    
    public OutputStream write() throws IOException
    {
        if (!file.canWrite()) throw new IOException("Cannot write to " + path);
        return new FileOutputStream(file);
    }
}