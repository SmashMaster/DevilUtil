package com.samrj.devil.gl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;

/**
 * Keeps track of multiple shader sources and allows #include directives.
 */
public class ShaderManager extends DGLObj
{
    private static int getShaderType(String fileName)
    {
        int extIndex = fileName.lastIndexOf('.');
        String ext = fileName.substring(extIndex).toLowerCase();

        switch(ext)
        {
            case ".vert": return GL_VERTEX_SHADER;
            case ".geom": return GL_GEOMETRY_SHADER;
            case ".frag": return GL_FRAGMENT_SHADER;
        }
        return -1;
    }

    private final Path basePath;
    private final Map<URI, String> shaderSources;
    private final Map<URI, Shader> shaders = new HashMap<>();

    ShaderManager(Path basePath) throws IOException
    {
        this.basePath = Objects.requireNonNull(basePath);

        GLSLPreprocessor preprocessor = DGL.genGLSLPreprocessor();
        preprocessor.setLineDirectivesEnabled(false);
        for (Path filePath : Files.walk(basePath).toList())
            if (Files.isRegularFile(filePath)) preprocessor.load(filePath.toUri());
        preprocessor.process();
        shaderSources = preprocessor.getSources();
        DGL.delete(preprocessor);
    }

    public Shader loadShader(String pathStr)
    {
        try
        {
            URI path = basePath.resolve(pathStr).toUri();

            Shader shader = shaders.get(path);
            if (shader != null) return shader;

            String source = shaderSources.get(path);
            if (source == null) throw new FileNotFoundException(pathStr);

            try
            {
                shader = DGL.genShader(getShaderType(pathStr)).source(source);
                shaders.put(path, shader);
            }
            catch (Exception e)
            {
                BufferedReader reader = new BufferedReader(new StringReader(source));
                String line = reader.readLine();
                int lineNo = 1;
                while (line != null)
                {
                    System.err.println(lineNo + ": " + line);
                    lineNo++;
                    line = reader.readLine();
                }

                throw e;
            }
            return shader;
        }
        catch (Exception e)
        {
            throw new RuntimeException("In shader: " + pathStr, e);
        }
    }

    public ShaderProgram loadProgram(String... paths)
    {
        Shader[] shaders = new Shader[paths.length];
        for (int i=0; i<paths.length; i++) shaders[i] = loadShader(paths[i]);
        try
        {
            return DGL.loadProgram(shaders);
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("In program: " + Arrays.toString(paths), e);
        }
    }

    public ShaderProgram loadBasicProgram(String path)
    {
        return loadProgram(path + ".vert", path + ".frag");
    }

    @Override
    void delete()
    {
        for (Shader shader : shaders.values()) DGL.delete(shader);
    }
}
