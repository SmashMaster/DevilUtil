package com.samrj.devil.graphics;

import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.res.Resource;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Locale;
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
        
        ShaderSource source = new ShaderSource(name, res);
        sources.put(name, source);
        graph.add(source);
    }
    
    public static void process()
    {
        //Construct dependency graph
        for (ShaderSource source : sources.values())
            for (String depName : source.getDependencies())
                graph.addEdge(sources.get(depName), source);
        
        //Iterate through vertices in reverse topological order
        ListIterator<ShaderSource> i1 = graph.sort().listIterator(graph.size());
        while (i1.hasPrevious())
        {
            ShaderSource source = i1.previous();
            DAG<ShaderSource> subgraph = graph.subgraph(source);
            
            //Insert code into dependant source in correct order
            ListIterator<ShaderSource> i2 = subgraph.sort().listIterator(subgraph.size());
            while (i2.hasPrevious())
            {
                ShaderSource dependency = i2.previous();
                if (dependency == source) continue;
                source.insert(dependency);
            }
        }
    }
    
    private static ShaderSource loudGet(String name)
    {
        name = name.toLowerCase(Locale.ENGLISH);
        ShaderSource out = sources.get(name);
        if (out == null) throw new IllegalArgumentException("No such source '" + name + "'");
        return out;
    }
    
    public static GLShader makeShader(String vertName, String fragName, boolean shouldComplete) throws ShaderException
    {
        return new GLShader(loudGet(vertName), loudGet(fragName), shouldComplete);
    }
    
    public static GLShader makeShader(String vertName, String fragName) throws ShaderException
    {
        return makeShader(vertName, fragName, true);
    }
    
    public static void unload()
    {
        sources.clear();
        graph.clear();
        ShaderSource.resetHash();
    }
    
    private ShaderProcessor() {}
}
