package com.samrj.devil.graphics;

import com.samrj.devil.gl.*;
import com.samrj.devil.math.Util;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;

/**
 * Class for converting equirectangular projection maps to cubemaps. But not the other way around. Yet.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class LatlongToCubemap
{
    private static final String VERT = """
            #version 140
                        
            in vec2 in_pos;
                        
            out vec2 v_coord;
                        
            void main()
            {
                v_coord = in_pos*0.5 + 0.5;
                gl_Position = vec4(in_pos, 0.0, 1.0);
            }
            """;

    private static final String FRAG = """
            #version 140
                        
            uniform sampler2D u_latlong;
            uniform int u_face;
            
            in vec2 v_coord;
                        
            out vec4 out_color;
                        
            const float PI = 3.1415926535897932384626433832795;
            const int POSITIVE_X = 0;
            const int NEGATIVE_X = 1;
            const int POSITIVE_Y = 2;
            const int NEGATIVE_Y = 3;
            const int POSITIVE_Z = 4;
            const int NEGATIVE_Z = 5;
                        
            void main()
            {
                vec2 uv = v_coord*2.0 - 1.0;
                vec3 dir;
                
                switch(u_face)
                {
                    case POSITIVE_X: dir = vec3(1.0, -uv.y, -uv.x); break;
                    case NEGATIVE_X: dir = vec3(-1.0, -uv.y, uv.x); break;
                    case POSITIVE_Y: dir = vec3(uv.x, 1.0, uv.y); break;
                    case NEGATIVE_Y: dir = vec3(uv.x, -1.0, -uv.y); break;
                    case POSITIVE_Z: dir = vec3(uv.x, -uv.y, 1.0); break;
                    case NEGATIVE_Z: dir = vec3(-uv.x, -uv.y, -1.0); break;
                }
                
                dir = normalize(dir);
                
                vec2 latlong_coord = vec2(atan(dir.x, dir.z)/(2.0*PI), (asin(dir.y)/(0.5*PI))*0.5 + 0.5);
                out_color = texture(u_latlong, latlong_coord).rgba;
            }
            """;

    private static final String[] FACE_NAMES = {"_pos_x", "_neg_x", "_pos_y", "_neg_y", "_pos_z", "_neg_z"};

    private static boolean isInit;
    private static FBO fbo;
    private static VertexBuffer fsq;
    private static ShaderProgram shader;

    public static void init()
    {
        if (isInit) throw new IllegalStateException();

        fbo = DGL.genFBO().bind();
        fbo.drawBuffers(GL_COLOR_ATTACHMENT0);
        fsq = DGLUtil.makeFSQ("in_pos");
        shader = DGL.loadProgram(VERT, FRAG);
        isInit = true;
    }

    public static void destroy()
    {
        if (!isInit) throw new IllegalStateException();

        DGL.delete(fbo, fsq, shader);
        fbo = null;
        fsq = null;
        shader = null;
        isInit = false;
    }

    /**
     * Loads a cubemap given any one of its faces. Finds the other faces in the same directory using these postfixes:
     *
     * "_pos_x", "_neg_x", "_pos_y", "_neg_y", "_pos_z", "_neg_z"
     */
    public static TextureCubemap load(Path path) throws IOException
    {
        String filename = path.getFileName().toString();

        //Strip extension.
        int extIndex = filename.lastIndexOf('.');
        if (extIndex < 0) throw new IOException("Filename " + filename + " has no extension.");
        String extension = filename.substring(extIndex);
        String noExtension = filename.substring(0, extIndex);

        //Strip postfix.
        String noPostfix = null;
        for (String postfix : FACE_NAMES)
        {
            int index = noExtension.indexOf(postfix);
            if (index >= 0)
            {
                noPostfix = noExtension.substring(0, index);
                break;
            }
        }
        if (noPostfix == null) throw new IOException("Filename " + filename + " does not end with one of valid postfixes:\n" + Arrays.toString(FACE_NAMES));

        Path directory = path.toAbsolutePath().getParent();

        Image[] images = new Image[6];
        TextureCubemap result = null;
        try
        {
            for (int i = 0; i < 6; i++)
            {
                Path facePath = directory.resolve(noPostfix + FACE_NAMES[i] + extension);
                if (!facePath.toFile().canRead()) throw new IOException("Cannot read file " + facePath);
                images[i] = DGL.loadImage(facePath);
            }

            result = DGL.genTextureCubemap().bind();
            result.image(images, GL_RGBA8);
            return result;
        }
        catch (Throwable t)
        {
            DGL.delete(result);
            throw t;
        }
        finally
        {
            DGL.delete(images);
        }
    }

    public static TextureCubemap convert(Path path, int resolution) throws IOException
    {
        boolean startedInit = isInit;
        if (!isInit) init();

        Texture2D latlongTex = null;
        TextureCubemap cubemap = null;
        try
        {
            latlongTex = DGL.loadTex2D(path).bind();
            latlongTex.parami(GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            latlongTex.generateMipmap();

            cubemap = DGL.genTextureCubemap();
            cubemap.image(resolution, GL_RGBA8);

            glViewport(0, 0,  resolution, resolution);

            DGL.useProgram(shader);
            for (int face=0; face<6; face++)
            {
                fbo.textureCubemap(cubemap, face, GL_COLOR_ATTACHMENT0);
                shader.uniform1i("u_face", face);
                DGL.draw(fsq, GL_TRIANGLES);
            }
            return cubemap;
        }
        catch (Throwable t)
        {
            DGL.delete(cubemap);
            throw t;
        }
        finally
        {
            DGL.delete(latlongTex);
            if (!startedInit) destroy();
        }
    }

    public static void save(TextureCubemap cubemap, Path outputDirectory, String outputFilename) throws IOException
    {
        int resolution = cubemap.getSize();

        Image image = null;
        try
        {
            image = DGL.genImage(resolution, resolution, 4, Util.PrimType.BYTE);
            image.buffer.clear();

            for (int face = 0; face < 6; face++)
            {
                cubemap.download(face, image, GL_RGBA8);
                Path outPath = outputDirectory.resolve(outputFilename + FACE_NAMES[face] + ".png");
                File outFile = outPath.toFile();
                outFile.createNewFile();
                ImageIO.write(image.toBufferedImage(), "PNG", outFile);
            }
        }
        finally
        {
            DGL.delete(image);
        }
    }

    public static void convert(Path path, String outputFilename, int resolution) throws IOException
    {
        TextureCubemap cubemap = null;
        Image image = null;
        try
        {
            cubemap = convert(path, resolution);
            save(cubemap, path.toAbsolutePath().getParent(), outputFilename);
        }
        finally
        {
            DGL.delete(cubemap, image);
        }
    }

    public static void convert(String path, String outputFilename, int resolution) throws IOException
    {
        convert(Path.of(path), outputFilename, resolution);
    }

    private LatlongToCubemap()
    {
    }
}
