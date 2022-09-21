package com.samrj.devil.gl;

import com.samrj.devil.math.topo.DAG;

import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * Allows usage of the #include directive in GLSL source files.
 */
public final class GLSLPreprocessor extends DGLObj
{
    private final Map<URI, Source> sources = new HashMap<>();
    private boolean lineDirectivesEnabled = true;

    GLSLPreprocessor()
    {
    }

    public void setLineDirectivesEnabled(boolean enabled)
    {
        lineDirectivesEnabled = enabled;
    }

    public void load(URI uri) throws IOException
    {
        sources.put(uri, new Source(uri));
    }

    private class Include
    {
        private final URI uri;
        private Source source;

        private Include(URI uri)
        {
            this.uri = uri;
        }

        @Override
        public String toString()
        {
            return source.string;
        }
    }

    private class Source
    {
        private final URI uri;
        private final ArrayList<Object> lines = new ArrayList<>();
        private final ArrayList<Include> includes = new ArrayList<>();

        private String string;

        private Source(URI uri) throws IOException
        {
            this.uri = uri;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream())))
            {
                int lineNo = 1;

                while (reader.ready())
                {
                    String line = reader.readLine();
                    if (line.startsWith("#"))
                    {
                        String lower = line.toLowerCase();
                        if (lower.startsWith("#include"))
                        {
                            int begin = line.indexOf('<');
                            int end = line.indexOf('>');
                            String pathStr = line.substring(begin + 1, end);
                            URI includePath = uri.resolve(pathStr);
                            Include include = new Include(includePath);
                            includes.add(include);
                            if (lineDirectivesEnabled) lines.add("#line " + 1);
                            lines.add(include);
                            if (lineDirectivesEnabled) lines.add("#line " + (lineNo + 1)); //Maintains line numbers for more legible errors.
                        }
                        else if (lineDirectivesEnabled && lower.startsWith("#version"))
                        {
                            lines.add(line);
                            lines.add("#line " + (lineNo + 1));
                        }
                        else lines.add(line);
                    }
                    else lines.add(line);

                    lineNo++;
                }
            }
            catch (Exception e)
            {
                throw new IOException("In shader " + uri, e);
            }
        }
    }

    public void process()
    {
        //Sort by dependencies and check for cyclic dependencies.
        DAG<Source> dag = new DAG<>();
        for (Source source : sources.values()) dag.add(source);
        for (Source source : sources.values()) for (Include include : source.includes) try
        {
            include.source = sources.get(include.uri);
            if (include.source == null) throw new NoSuchElementException(include.uri.toString());
            dag.addEdgeSafe(include.source, source);
        }
        catch (Exception e)
        {
            throw new RuntimeException("In shader " + source.uri, e);
        }

        //Build source strings.
        for (Source source : dag)
        {
            StringBuilder builder = new StringBuilder();
            for (Object object : source.lines)
            {
                builder.append(object);
                builder.append('\n');
            }
            source.string = builder.toString();
        }
    }

    public String getSource(URI uri)
    {
        return sources.get(uri).string;
    }

    public Map<URI, String> getSources()
    {
        Map<URI, String> map = new HashMap<>(sources.size());
        for (Map.Entry<URI, Source> e : sources.entrySet())
            map.put(e.getKey(), e.getValue().string);
        return map;
    }

    @Override
    void delete()
    {
    }
}
