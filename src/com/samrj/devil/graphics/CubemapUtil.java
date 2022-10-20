package com.samrj.devil.graphics;

import com.samrj.devil.gl.*;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;

/**
 * Class for converting equirectangular projection maps to cubemaps. But not the other way around. Yet.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CubemapUtil
{
    private static final String LATLONG_TO_CUBEMAP_VERT = """
            #version 140
                        
            in vec2 in_pos;
                        
            out vec2 v_coord;
                        
            void main()
            {
                v_coord = in_pos*0.5 + 0.5;
                gl_Position = vec4(in_pos, 0.0, 1.0);
            }
            """;

    private static final String LATLOVE_TO_CUBEMAP_FRAG = """
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

    public enum Face
    {
        POS_X("_pos_x"),
        NEG_X("_neg_x"),
        POS_Y("_pos_y"),
        NEG_Y("_neg_y"),
        POS_Z("_pos_z"),
        NEG_Z("_neg_z");

        public final String postFix;

        Face(String postFix)
        {
            this.postFix = postFix;
        }
    }

    private static boolean isInit;
    private static FBO fbo;
    private static VertexBuffer fsq;
    private static ShaderProgram latlongToCubemapShader;

    public static void init()
    {
        if (isInit) throw new IllegalStateException();

        fbo = DGL.genFBO().bind();
        fbo.drawBuffers(GL_COLOR_ATTACHMENT0);
        fsq = DGLUtil.makeFSQ("in_pos");
        latlongToCubemapShader = DGL.loadProgram(LATLONG_TO_CUBEMAP_VERT, LATLOVE_TO_CUBEMAP_FRAG);
        isInit = true;
    }

    public static void destroy()
    {
        if (!isInit) throw new IllegalStateException();

        DGL.delete(fbo, fsq, latlongToCubemapShader);
        fbo = null;
        fsq = null;
        latlongToCubemapShader = null;
        isInit = false;
    }

    /**
     * Converts the provided direction to a face UV coordinates, and stores the result in result. Returns the face.
     */
    public static Face getUV(Vec3 dir, Vec2 result)
    {
        float absX = Math.abs(dir.x);
        float absY = Math.abs(dir.y);
        float absZ = Math.abs(dir.z);

        float max, u, v;
        Face face;

        if (absX >= absY && absX >= absZ)
        {
            max = absX;
            if (dir.x > 0) // POSITIVE X
            {
                u = -dir.z;
                v = dir.y;
                face = Face.POS_X;
            }
            else // NEGATIVE X
            {
                u = dir.z;
                v = dir.y;
                face = Face.NEG_X;
            }
        }
        else if (absY >= absX && absY >= absZ)
        {
            max = absY;
            if (dir.y > 0) // POSITIVE Y
            {
                u = dir.x;
                v = -dir.z;
                face = Face.POS_Y;
            }
            else // NEGATIVE Y
            {
                u = dir.x;
                v = dir.z;
                face = Face.NEG_Y;
            }
        }
        else
        {
            max = absZ;
            if (dir.z > 0) // POSITIVE Z
            {
                u = dir.x;
                v = dir.y;
                face = Face.POS_Z;
            }
            else // NEGATIVE Z
            {
                u = -dir.x;
                v = dir.y;
                face = Face.NEG_Z;
            }
        }

        result.set(0.5f*(u/max + 1.0f), 0.5f*(v/max + 1.0f));
        return face;
    }

    /**
     * Converts the given face and UV coordinates into its direction on this cubemap. The result is not normalized.
     */
    public static void getDir(Vec2 uv, Face face, Vec3 result)
    {
        float u = uv.x*2.0f - 1.0f;
        float v = uv.y*2.0f - 1.0f;

        switch (face)
        {
            case POS_X -> result.set(1.0f, v, -u);
            case NEG_X -> result.set(-1.0f, v, u);
            case POS_Y -> result.set(u, 1.0f, -v);
            case NEG_Y -> result.set(u, -1.0f, v);
            case POS_Z -> result.set(u, v, 1.0f);
            case NEG_Z -> result.set(-u, v, -1.0f);
            default -> throw new NullPointerException();
        }
    }

    /**
     * Converts the given face and UV coordinates into its direction on this cubemap. The result is not normalized.
     */
    public static Vec3 getDir(Vec2 uv, Face face)
    {
        Vec3 result = new Vec3();
        getDir(uv, face, result);
        return result;
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
        for (Face face : Face.values())
        {
            int index = noExtension.indexOf(face.postFix);
            if (index >= 0)
            {
                noPostfix = noExtension.substring(0, index);
                break;
            }
        }
        if (noPostfix == null) throw new IOException("Filename " + filename + " does not end with one of valid cubemap face postfixes.");

        Path directory = path.toAbsolutePath().getParent();

        Image[] images = new Image[6];
        TextureCubemap result = null;
        try
        {
            for (int face = 0; face < 6; face++)
            {
                Path facePath = directory.resolve(noPostfix + Face.values()[face].postFix + extension);
                if (!facePath.toFile().canRead()) throw new IOException("Cannot read file " + facePath);
                images[face] = DGL.loadImage(facePath);
            }

            result = DGL.genTextureCubemap().bind();
            result.image(images);
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

    /**
     * Converts the equirectangular projection image at the given path to a cubemap with the given resolution.
     */
    public static TextureCubemap convertFromLatlong(Path path, int resolution) throws IOException
    {
        boolean startedInit = isInit;
        if (!isInit) init();

        Texture2D latlongTex = null;
        TextureCubemap cubemap = null;
        try
        {
            latlongTex = DGL.loadTex2D(path).bind();
            latlongTex.parami(GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            latlongTex.paramf(EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16.0f);
            latlongTex.generateMipmap();

            cubemap = DGL.genTextureCubemap();
            cubemap.image(resolution, latlongTex.getInternalFormat());

            glViewport(0, 0,  resolution, resolution);

            DGL.useProgram(latlongToCubemapShader);
            for (int face=0; face<6; face++)
            {
                fbo.textureCubemap(cubemap, face, GL_COLOR_ATTACHMENT0);
                latlongToCubemapShader.uniform1i("u_face", face);
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

    /**
     * Saves the given cubemap as 6 PNG files at the given directory, with the given filename prefix.
     */
    public static void save(TextureCubemap cubemap, Path outputDirectory, String outputFilename) throws IOException
    {
        int resolution = cubemap.getSize();

        Image image = null;
        try
        {
            int format = cubemap.getInternalFormat();
            int bands = TexUtil.getBands(TexUtil.getBaseFormat(format));
            Util.PrimType primType = TexUtil.getPrimitiveType(format);

            image = DGL.genImage(resolution, resolution, bands, primType);
            image.buffer.clear();

            for (int face = 0; face < 6; face++)
            {
                cubemap.download(face, image, format);
                Path outPath = outputDirectory.resolve(outputFilename + Face.values()[face].postFix + ".png");
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

    /**
     * Converts the equirectangular projection image at the given path to a cubemap with the given resolution, and
     * saves it as 6 PNGs with the given filename prefix in the same directory.
     */
    public static void convertFromLatlong(Path path, String outputFilename, int resolution) throws IOException
    {
        TextureCubemap cubemap = null;
        Image image = null;
        try
        {
            cubemap = convertFromLatlong(path, resolution);
            save(cubemap, path.toAbsolutePath().getParent(), outputFilename);
        }
        finally
        {
            DGL.delete(cubemap, image);
        }
    }

    /**
     * Converts the equirectangular projection image at the given path to a cubemap with the given resolution, and
     * saves it as 6 PNGs with the given filename prefix in the same directory.
     */
    public static void convertFromLatlong(String path, String outputFilename, int resolution) throws IOException
    {
        convertFromLatlong(Path.of(path), outputFilename, resolution);
    }

    private CubemapUtil()
    {
    }
}
