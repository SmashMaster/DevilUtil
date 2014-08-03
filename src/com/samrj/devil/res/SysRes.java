package com.samrj.devil.res;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SysRes extends Resource
{
    public final URL url;
    
    public static SysRes find(String path) throws IOException
    {
        URL url = ClassLoader.getSystemResource(path);
        if (url == null) throw new FileNotFoundException();
        return new SysRes(path, url);
    }
    
    public static SysRes findSilent(String path)
    {
        URL url = ClassLoader.getSystemResource(path);
        if (url == null) return null;
        return new SysRes(path, url);
    }
    
    public static boolean isValid(String path)
    {
        return ClassLoader.getSystemResource(path) != null;
    }
    
    SysRes(String path, URL url)
    {
        super(path);
        this.url = url;
    }
    
    @Override
    public boolean canOpen()
    {
        return true;
    }

    @Override
    public InputStream open() throws IOException
    {
        return url.openStream();
    }
}