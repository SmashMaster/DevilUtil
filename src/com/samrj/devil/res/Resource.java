package com.samrj.devil.res;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Resource
{
    /**
     * Returns a resource if one (and only one) exists that matches the given
     * path. If no resource exists, return null. If more than one exists, throw
     * an IllegalArgumentException.
     */
    public static Resource find(String path) throws IOException
    {
        if (path == null) throw new NullPointerException();
        
        FileRes file = FileRes.findSilent(path);
        SysRes sys = SysRes.findSilent(path);
        
        if (sys != null && file != null) throw new ResourceConflictException();
        if (sys != null) return sys;
        if (file != null) return file;
        throw new FileNotFoundException(path);
    }
    
    public static Resource findSilent(String path)
    {
        if (path == null) return null;
        
        FileRes file = FileRes.findSilent(path);
        SysRes sys = SysRes.findSilent(path);
        
        if (sys != null && file != null) return null;
        if (sys != null) return sys;
        if (file != null) return file;
        return null;
    }
    
    public static boolean isValid(String path)
    {
        return findSilent(path) != null;
    }
    
    public static InputStream open(String path) throws IOException
    {
        return find(path).open();
    }
    
    public final String path;
    
    Resource(String path)
    {
        this.path = path;
    }
    
    public abstract boolean canOpen();
    public abstract InputStream open() throws IOException;
}