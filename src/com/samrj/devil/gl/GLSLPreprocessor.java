package com.samrj.devil.gl;

import com.samrj.devil.math.topo.DAG;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Allows usage of the #include directive in GLSL source files.
 */
public final class GLSLPreprocessor extends DGLObj
{
    private final Map<Path, Source> sources = new HashMap<>();

    GLSLPreprocessor()
    {
    }

    public void load(Path filePath) throws IOException
    {
        sources.put(filePath, new Source(filePath));
    }

    public void load(String filePath) throws IOException
    {
        load(Path.of(filePath));
    }

    private class Include
    {
        private final Path path;
        private Source source;

        private Include(Path path)
        {
            this.path = path;
        }

        @Override
        public String toString()
        {
            return source.string;
        }
    }

    private class Source
    {
        private final ArrayList<Object> lines = new ArrayList<>();
        private final ArrayList<Include> includes = new ArrayList<>();

        private String string;

        private Source(Path filePath) throws IOException
        {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile())))
            {
                Path directory = filePath.getParent();

                while (reader.ready())
                {
                    String line = reader.readLine();
                    if (line.startsWith("#") && line.toLowerCase().startsWith("#include"))
                    {
                        int begin = line.indexOf('<');
                        int end = line.indexOf('>');
                        String pathStr = line.substring(begin + 1, end);
                        Path includePath = directory.resolve(pathStr);
                        Include include = new Include(includePath);
                        lines.add(include);
                        includes.add(include);
                    }
                    else lines.add(line);
                }
            }
            catch (Exception e)
            {
                throw new IOException("In shader " + filePath, e);
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
            include.source = sources.get(include.path);
            if (include.source == null) throw new NoSuchElementException(include.path.toString());
            dag.addEdgeSafe(include.source, source);
        }
        catch (Exception e)
        {
            throw new RuntimeException("In shader " + source, e);
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

    public String getSource(Path path)
    {
        return sources.get(path).string;
    }

    public String getSource(String path)
    {
        return getSource(Path.of(path));
    }

    public Map<Path, String> getSources()
    {
        Map<Path, String> map = new HashMap<>(sources.size());
        for (Map.Entry<Path, Source> e : sources.entrySet())
            map.put(e.getKey(), e.getValue().string);
        return map;
    }

    @Override
    void delete()
    {
    }
}
