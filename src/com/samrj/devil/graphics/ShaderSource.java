package com.samrj.devil.graphics;

import com.samrj.devil.res.Resource;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
class ShaderSource
{
    private static int STATIC_HASH = 0;
    
    static void resetHash()
    {
        STATIC_HASH = 0;
    }
    
    final String name;
    private final int id;
    private int incLine = -1;
    private final LinkedList<String> lines = new LinkedList<>();
    private final LinkedList<String> dependencies = new LinkedList<>();
    
    ShaderSource(String name, Resource res) throws IOException
    {
        if (STATIC_HASH == -1) throw new IllegalStateException(
                "How did you manage to load 2^32 shader sources? Seriously, let me know.");
        id = STATIC_HASH++;
        
        this.name = name;
        
        InputStream in = res.open();
        if (in == null) throw new FileNotFoundException(res.path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        while (reader.ready())
        {
            String line = reader.readLine();
            String lowerLine = line.toLowerCase(Locale.ENGLISH);
            
            if (lowerLine.startsWith("#include"))
            {
                if (incLine == -1) incLine = lines.size();
                dependencies.add(lowerLine.substring(line.indexOf(' ')).trim());
            }
            else lines.add(line);
        }
    }
    
    /**
     * Must insert in reverse order! Also, probably need to store original raw
     * source AND current full source.
     */
    void insert(ShaderSource source)
    {
        Iterator<String> i = source.lines.descendingIterator();
        while (i.hasNext()) lines.add(incLine > 0 ? incLine : 1, i.next());
    }
    
    String getSource()
    {
        String out = "";
        for (String line : lines) out += line + '\n';
        return out;
    }
    
    List<String> getLines()
    {
        return Collections.unmodifiableList(lines);
    }
    
    List<String> getDependencies()
    {
        return Collections.unmodifiableList(dependencies);
    }
    
    @Override
    public final int hashCode()
    {
        return id;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj == this;
    }
}
