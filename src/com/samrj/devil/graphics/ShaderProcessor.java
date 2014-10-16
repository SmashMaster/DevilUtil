package com.samrj.devil.graphics;

import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.res.Resource;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ShaderProcessor
{
    private static final Map<String, ShaderSource> sources = new LinkedHashMap<>();
    private static final DAG<ShaderSource> graph = new DAG<>();
    
    public static void preload(String filePath) throws IOException
    {
        Resource res = Resource.find(filePath);
        String name = res.name().toLowerCase();
        if (sources.containsKey(name)) throw new IllegalArgumentException("Already loaded " + name);
        
        sources.put(name, new ShaderSource(res));
    }
    
    private ShaderProcessor() {}
}
