/*
 * Copyright (c) 2020 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.gl;

import com.samrj.devil.graphics.TexUtil;
import com.samrj.devil.math.Util.PrimType;
import com.samrj.devil.model.Mesh;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.*;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * DevilGL. A state-based, object-oriented, forward compatible OpenGL wrapper;
 * inspired by deprecated OpenGL.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class DGL
{
    //Constant fields
    private static Thread thread;
    private static GLCapabilities capabilities;
    private static Set<DGLObj> objects;
    private static boolean debugContext;
    private static boolean debugLeak;
    private static Thread debugShutdownHook;
    private static boolean init;
    
    //State fields
    private static ShaderProgram boundProgram;
    private static FBO readFBO, drawFBO;
    
    static void checkState()
    {
        if (!init) throw new IllegalStateException("DGL not initialized.");
        if (Thread.currentThread() != thread)
            throw new IllegalThreadStateException("DGL initialized on different thread.");
    }
    
    /**
     * Initializes DevilGL. Must be called from a thread on which an OpenGL
     * context is current.
     */
    public static void init()
    {
        if (init) throw new IllegalStateException("DGL already initialized.");
        thread = Thread.currentThread();
        capabilities = GL.getCapabilities();
        objects = Collections.newSetFromMap(new IdentityHashMap<>());
        debugContext = (glGetInteger(GL_CONTEXT_FLAGS) & GL_CONTEXT_FLAG_DEBUG_BIT) != 0;
        debugLeak = debugContext;
        VAO.init();
        DGLException.init(debugContext);
        debugShutdownHook = new Thread(() ->
        {
            if (debugLeak && init)
            {
                for (DGLObj obj : objects) obj.debugLeakTrace();
                System.err.println("DevilUtil (DGL) - DGL not terminated before JVM shut down!");
            }
        });
        Runtime.getRuntime().addShutdownHook(debugShutdownHook);
        init = true;
    }
    
    /**
     * Returns whether the OpenGL context in use by DGL is a debug context.
     */
    public static boolean getDebugEnabled()
    {
        return debugContext;
    }
    
    /**
     * Returns whether or not leak tracking is currently enabled for DevilGL.
     */
    public static boolean getDebugLeakTracking()
    {
        return debugLeak;
    }
    
    /**
     * Enables resource leak tracking. Note: Leak tracking works only for
     * objects that were constructed *after* debug was enabled, so it is best to
     * enable debug before initializing DevilGL. It is enabled by default in
     * debug OpenGL contexts.
     */
    public static void setDebugLeakTracking(boolean debugLeak)
    {
        DGL.debugLeak = debugLeak;
    }
    
    /**
     * @return The current OpenGL context's capabilities.
     */
    public static GLCapabilities getCapabilities()
    {
        checkState();
        return capabilities;
    }
    
    private static <T extends DGLObj> T gen(T obj)
    {
        objects.add(obj);
        return obj;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Shader methods">
    /**
     * Generates a new OpenGL shader. Shader will not have any associated
     * sources or be compiled.
     * 
     * @param type The type of shader to generate.
     * @return A new shader.
     */
    public static Shader genShader(int type)
    {
        return gen(new Shader(type));
    }
    
    /**
     * Generates a new OpenGL shader, loads sources from the given resource
     * path, then compiles the shader.
     * 
     * @param path The class/file path from which to load sources.
     * @param type The type of shader to load.
     * @return A new, compiled shader.
     * @throws IOException If an I/O error occurs.
     */
    public static Shader loadShader(String path, int type) throws IOException
    {
        return genShader(type).sourceFromFile(path);
    }
    
    /**
     * Generates and returns a new shader program.
     * 
     * @return A new shader program.
     */
    public static ShaderProgram genProgram()
    {
        return gen(new ShaderProgram());
    }
    
    /**
     * Generates, loads, compiles, and links a new shader program using any
     * number of given shaders.
     * 
     * @param shaders An array of shaders to attach.
     * @return A new, complete shader program.
     */
    public static ShaderProgram loadProgram(Shader... shaders)
    {
        return genProgram().attach(shaders).link().detachAll();
    }
    
    /**
     * Loads a shader using the given strings as its source code.
     * 
     * @param vertSource The vertex shader source code.
     * @param fragSource The fragment shader source code.
     * @return A new, complete shader program.
     */
    public static ShaderProgram loadProgram(String vertSource, String fragSource)
    {
        Shader vert = DGL.genShader(GL_VERTEX_SHADER);
        vert.source(vertSource);
        Shader frag = DGL.genShader(GL_FRAGMENT_SHADER);
        frag.source(fragSource);
        ShaderProgram shader = loadProgram(vert, frag);
        delete(vert, frag);
        return shader;
    }
    
    /**
     * Generates, loads, compiles, links, and validates a new shader program as
     * well as an underlying vertex and fragment shader. The shader sources are
     * assumed to be in the given path, with the same name, ending in .vert and
     * .frag, respectively.
     * 
     * @param path The directory and name to load sources from.
     * @return A new, complete shader program.
     * @throws IOException 
     */
    public static ShaderProgram loadProgram(String path) throws IOException
    {
        Shader vertShader = loadShader(path + ".vert", GL_VERTEX_SHADER);
        Shader fragShader = loadShader(path + ".frag", GL_FRAGMENT_SHADER);
        ShaderProgram program = loadProgram(vertShader, fragShader);
        delete(vertShader, fragShader);
        return program;
    }
    
    private static void checkProgramState()
    {
        if (glGetInteger(GL_CURRENT_PROGRAM) != ShaderProgram.glSafeID(boundProgram))
            throw new IllegalStateException("Shader state modified outside of DGL.");
    }
    
    /**
     * Uses the given shader program for any subsequent draw calls. Pass null to
     * unbind the current shader.
     * 
     * @param shaderProgram The shader program to use.
     * @return The given shader program, which is now in use.
     */
    public static ShaderProgram useProgram(ShaderProgram shaderProgram)
    {
        if (getDebugEnabled()) checkProgramState();
        
        ShaderProgram.ensureNotDeleted(shaderProgram);
        glUseProgram(ShaderProgram.glSafeID(shaderProgram));
        boundProgram = shaderProgram;
        
        return shaderProgram;
    }
    
    /**
     * @return The shader program currently in use, or null if none is in use.
     */
    public static ShaderProgram currentProgram()
    {
        if (getDebugEnabled()) checkProgramState();
        
        return boundProgram;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Vertex data methods">
    /**
     * Generates a new vertex buffer of the given capacity.
     * 
     * @param maxVertices The maximum number of vertices to buffer.
     * @param maxIndices The maximum number of indices to buffer.
     * @return A new vertex buffer.
     */
    public static VertexBuffer genVertexBuffer(int maxVertices, int maxIndices)
    {
        return gen(new VertexBuffer(maxVertices, maxIndices));
    }
    
    /**
     * Generates a new vertex buffer of the given capacity.
     * 
     * @param maxVertices The maximum number of vertices to buffer.
     * @param maxIndices The maximum number of indices to buffer.
     * @return A new vertex buffer.
     */
    public static VertexStream genVertexStream(int maxVertices, int maxIndices)
    {
        return gen(new VertexStream(maxVertices, maxIndices));
    }
    
    /**
     * Returns a new mesh drawer, which buffers the given mesh onto the GPU.
     * 
     * @param mesh The mesh to buffer.
     * @return A new mesh buffer.
     */
    public static MeshBuffer genMeshBuffer(Mesh mesh)
    {
        return gen(new MeshBuffer(mesh));
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Image methods">
    /**
     * Allocates a new image buffer with the given dimensions and format. The
     * newly allocated buffer has indeterminate contents.
     * 
     * @param width The width of the image, in pixels.
     * @param height The height of the image, in pixels.
     * @param bands The number of bands the image will have.
     * @param type The primitive type of the image data.
     * @return A newly allocated image.
     */
    public static Image genImage(int width, int height, int bands, PrimType type)
    {
        return gen(new Image(width, height, bands, type));
    }
    
    /**
     * Creates a new compressed image container. Does not allocate any storage
     * for the image.
     * 
     * @param width The width of the image, in pixels.
     * @param height The height of the image, in pixels.
     * @param format The OpenGL compressed image format to use.
     * @return A new compressed image container.
     */
    public static ImageCompressed genImageCompressed(int width, int height, int format)
    {
        return gen(new ImageCompressed(width, height, format));
    }
    
    /**
     * Allocates an image  buffer for the given raster, then buffers the raster
     * in it. Returns the allocated buffer.
     * 
     * @param raster The raster to buffer.
     * @return A newly allocated buffer containing the given raster.
     */
    public static Image loadImage(Raster raster)
    {
        PrimType type = Image.getType(raster);
        if (type == null) throw new IllegalArgumentException("Given raster is not bufferable.");
        
        return genImage(raster.getWidth(),
                        raster.getHeight(),
                        raster.getNumBands(), type).buffer(raster);
    }
    
    /**
     * Loads an image type at the given path, which may be a classpath or file
     * path, and then buffers the image in a newly allocated buffer. Returns
     * the image buffer.
     * 
     * @param path The path to load an image from.
     * @return A newly allocated buffer containing the loaded image.
     * @throws IOException If an io exception occurs.
     */
    public static Image loadImage(String path) throws IOException
    {
        BufferedImage bImage = ImageIO.read(new File(path));
        
        if (bImage == null) throw new IOException("Cannot read image from " + path);
        
        return loadImage(bImage.getRaster());
    }
    
    /**
     * Creates a new image from the current read framebuffer and viewport.
     * Useful for taking screenshots.
     * 
     * @return A newly allocated image.
     */
    public static Image screenshotImage()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            long addr = stack.nmalloc(16);
            nglGetIntegerv(GL_VIEWPORT, addr);
            int x = memGetInt(addr);
            int y = memGetInt(addr + 4);
            int w = memGetInt(addr + 8);
            int h = memGetInt(addr + 12);

            Image out = genImage(w, h, 3, PrimType.BYTE);
            glReadPixels(x, y, w, h, GL_RGB, GL_UNSIGNED_BYTE, out.buffer);
            out.buffer.limit(out.buffer.capacity());
            return out;
        }
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Texture methods">
    /**
     * Generates a new OpenGL name for a 1D texture.
     * 
     * @return A new 1D texture object.
     */
    public static Texture1D genTex1D()
    {
        return gen(new Texture1D());
    }
    
    /**
     * Creates a new OpenGL 1D texture using the given image.
     * 
     * @param image The image to load as a texture.
     * @return A new 2D texture object.
     */
    public static Texture1D loadTex1D(Image image)
    {
        return genTex1D().image(image);
    }
    
    /**
     * Creates a new OpenGL 1D texture using the given raster. Allocates memory
     * to use as an image buffer, uploads the image, and then deallocates the
     * memory.
     * 
     * @param raster The raster to load as a texture.
     * @return A new 1D texture object.
     */
    public static Texture1D loadTex1D(Raster raster)
    {
        Image image = loadImage(raster);
        Texture1D texture = loadTex1D(image);
        delete(image);
        return texture;
    }
    
    /**
     * Creates a new openGL 1D texture using the image found at the given path.
     * Allocates memory to use as an image buffer, uploads the image, and then
     * deallocates the memory.
     * 
     * @param path The classpath or file path to an image.
     * @return A new 1D texture object.
     * @throws IOException If an io exception occurred.
     */
    public static Texture1D loadTex1D(String path) throws IOException
    {
        Image image = loadImage(path);
        Texture1D texture = loadTex1D(image);
        delete(image);
        return texture;
    }
    
    /**
     * Generates a new OpenGL name for a 2D texture.
     * 
     * @return A new 2D texture object.
     */
    public static Texture2D genTex2D()
    {
        return gen(new Texture2D());
    }
    
    /**
     * Creates a new OpenGL 2D texture using the given image.
     * 
     * @param image The image to load as a texture.
     * @return A new 2D texture object.
     */
    public static Texture2D loadTex2D(Image image)
    {
        return genTex2D().image(image);
    }
    
    /**
     * Creates a new OpenGL 2D texture using the given raster. Allocates memory
     * to use as an image buffer, uploads the image, and then deallocates the
     * memory.
     * 
     * @param raster The raster to load as a texture.
     * @return A new 2D texture object.
     */
    public static Texture2D loadTex2D(Raster raster)
    {
        Image image = loadImage(raster);
        Texture2D texture = loadTex2D(image);
        delete(image);
        return texture;
    }
    
    /**
     * Creates a new openGL 2D texture using the image found at the given path.
     * Allocates memory to use as an image buffer, uploads the image, and then
     * deallocates the memory.
     * 
     * @param path The classpath or file path to an image.
     * @return A new 2D texture object.
     * @throws IOException If an io exception occurred.
     */
    public static Texture2D loadTex2D(String path) throws IOException
    {
        Image image = loadImage(path);
        Texture2D texture = loadTex2D(image);
        delete(image);
        return texture;
    }
    
    /**
     * Generates a new OpenGL name for a 3D texture.
     * 
     * @return A new 3D texture object.
     */
    public static Texture3D genTex3D()
    {
        return gen(new Texture3D());
    }
    
    /**
     * Creates a new OpenGL 3D texture using the given image array.
     * 
     * @param images The array of images to load as a texture.
     * @return A new 3D texture object.
     */
    public static Texture3D loadTex3D(Image... images)
    {
        Texture3D texture = genTex3D();
        Image first = images[0];
        
        int format = TexUtil.getFormat(first);
        if (format == -1) throw new IllegalArgumentException("Illegal image format.");
        
        texture.image(first.width, first.height, images.length, format);
        
        for (int i=0; i<images.length; i++)
            texture.subimage(images[i], i, format);
        
        return texture;
    }
    
    /**
     * Creates a new OpenGL 3D texture using the given raster array.
     * 
     * @param rasters The raster array to load as a texture.
     * @return A new 3D texture object.
     */
    public static Texture3D loadTex3D(Raster... rasters)
    {
        Image[] images = new Image[rasters.length];
        for (int i=0; i<images.length; i++) images[i] = loadImage(rasters[i]);
        
        Texture3D texture = loadTex3D(images);
        delete(images);
        return texture;
    }
    
    /**
     * Creates a new OpenGL 3D texture using the given array of paths.
     * 
     * @param paths An array of file or class paths to load from.
     * @return A new 3D texture object.
     * @throws IOException If an io exception occurred.
     */
    public static Texture3D loadTex3D(String... paths) throws IOException
    {
        Image[] images = new Image[paths.length];
        for (int i=0; i<images.length; i++) images[i] = loadImage(paths[i]);
        
        Texture3D texture = loadTex3D(images);
        delete(images);
        return texture;
    }
    
    /**
     * Generates a new OpenGL name for a rectangle texture.
     * 
     * @return A new rectangle texture object.
     */
    public static TextureRectangle genTexRect()
    {
        return gen(new TextureRectangle());
    }
    
    /**
     * Creates a new OpenGL rectangle texture using the given image.
     * 
     * @param image The image to load as a texture.
     * @return A new rectangle texture object.
     */
    public static TextureRectangle loadTexRect(Image image)
    {
        return genTexRect().image(image);
    }
    
    /**
     * Creates a new OpenGL rectangle texture using the given raster. Allocates
     * memory to use as an image buffer, uploads the image, and then deallocates
     * the memory.
     * 
     * @param raster The raster to load as a texture.
     * @return A new rectangle texture object.
     */
    public static TextureRectangle loadTexRect(Raster raster)
    {
        Image image = loadImage(raster);
        TextureRectangle texture = loadTexRect(image);
        delete(image);
        return texture;
    }
    
    /**
     * Creates a new openGL rectangle texture using the image found at the given
     * path. Allocates memory to use as an image buffer, uploads the image, and
     * then deallocates the memory.
     * 
     * @param path The classpath or file path to an image.
     * @return A new rectangle texture object.
     * @throws IOException If an io exception occurred.
     */
    public static TextureRectangle loadTexRect(String path) throws IOException
    {
        Image image = loadImage(path);
        TextureRectangle texture = loadTexRect(image);
        delete(image);
        return texture;
    }
    
    /**
     * Generates a new OpenGL name for a 3D texture.
     * 
     * @return A new 3D texture object.
     */
    public static Texture2DArray genTex2DArray()
    {
        return gen(new Texture2DArray());
    }
    
    /**
     * Creates a new OpenGL 3D texture using the given image array.
     * 
     * @param images The array of images to load as a texture.
     * @return A new 3D texture object.
     */
    public static Texture2DArray loadTex2DArray(Image... images)
    {
        Texture2DArray texture = genTex2DArray();
        Image first = images[0];
        
        int format = TexUtil.getFormat(first);
        if (format == -1) throw new IllegalArgumentException("Illegal image format.");
        
        texture.image(first.width, first.height, images.length, format);
        
        for (int i=0; i<images.length; i++)
            texture.subimage(images[i], i, format);
        
        return texture;
    }
    
    /**
     * Creates a new OpenGL 3D texture using the given raster array.
     * 
     * @param rasters The raster array to load as a texture.
     * @return A new 3D texture object.
     */
    public static Texture2DArray loadTex2DArray(Raster... rasters)
    {
        Image[] images = new Image[rasters.length];
        for (int i=0; i<images.length; i++) images[i] = loadImage(rasters[i]);
        
        Texture2DArray texture = loadTex2DArray(images);
        delete(images);
        return texture;
    }
    
    /**
     * Creates a new OpenGL 3D texture using the given array of paths.
     * 
     * @param paths An array of file or class paths to load from.
     * @return A new 3D texture object.
     * @throws IOException If an io exception occurred.
     */
    public static Texture2DArray loadTex2DArray(String... paths) throws IOException
    {
        Image[] images = new Image[paths.length];
        for (int i=0; i<images.length; i++) images[i] = loadImage(paths[i]);
        
        Texture2DArray texture = loadTex2DArray(images);
        delete(images);
        return texture;
    }
    
    /**
     * Generates a new OpenGL name for a multisampled 2D texture.
     * 
     * @return A new multisampled 2D texture object.
     */
    public static Texture2DMultisample genTex2DMultisample()
    {
        return gen(new Texture2DMultisample());
    }
    
    /**
     * Generates a new OpenGL name for a cubemap texture.
     * 
     * @return A new cubemap texture object.
     */
    public static TextureCubemap genTextureCubemap()
    {
        return gen(new TextureCubemap());
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="RBO methods">
    /**
     * @return A newly created render buffer.
     */
    public static RBO genRBO()
    {
        return gen(new RBO());
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="FBO methods">
    /**
     * @return A newly created frame buffer.
     */
    public static FBO genFBO()
    {
        return gen(new FBO());
    }
    
    /**
     * Binds the given frame buffer to the given target. Pass null to bind the
     * default frame buffer.
     * 
     * @param fbo The frame buffer to bind.
     * @param target The targe to bind to, or null to bind the default frame
     *        buffer;
     * @return The given frame buffer.
     */
    public static FBO bindFBO(FBO fbo, int target)
    {
        switch (target)
        {
            case GL_FRAMEBUFFER: readFBO = fbo; drawFBO = fbo; break;
            case GL_READ_FRAMEBUFFER: readFBO = fbo; break;
            case GL_DRAW_FRAMEBUFFER: drawFBO = fbo; break;
            default: throw new IllegalArgumentException("Illegal target specified.");
        }
        
        if (fbo != null) fbo.bind(target);
        else glBindFramebuffer(target, 0);
        return fbo;
    }
    
    /**
     * Binds the given frame buffer to read from and draw to. Pass null to bind
     * the default frame buffer.
     * 
     * @param fbo The frame buffer to bind, or null to bind the default frame
     *        buffer.
     * @return The given frame buffer.
     */
    public static FBO bindFBO(FBO fbo)
    {
        return bindFBO(fbo, GL_FRAMEBUFFER);
    }
    
    /**
     * Blits the currently bound read buffer into the bound draw frame buffer.
     * Assumes the buffers are of equal dimensions and have compatible formats.
     * 
     * @param width The width of both buffers.
     * @param height The height of both buffers.
     * @param mask The bitwise OR of the flags indicating which buffers are to
     *        be copied. The allowed flags are GL_COLOR_BUFFER_BIT,
     *        GL_DEPTH_BUFFER_BIT and GL_STENCIL_BUFFER_BIT.
     */
    public static void blitFBO(int width, int height, int mask)
    {
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, mask, GL_NEAREST);
    }
    
    /**
     * Blits the given source frame buffer into the given target frame buffer.
     * Assumes the buffers are of equal dimensions and have compatible formats.
     * 
     * Temporarily alters the state of bound frame buffers.
     * 
     * @param source The buffer to read from, or null to read from the default
     *        frame buffer.
     * @param target The buffer to draw to, or null to draw to the default frame
     *        buffer.
     * @param width The width of both buffers.
     * @param height The height of both buffers.
     * @param mask The bitwise OR of the flags indicating which buffers are to
     *        be copied. The allowed flags are GL_COLOR_BUFFER_BIT,
     *        GL_DEPTH_BUFFER_BIT and GL_STENCIL_BUFFER_BIT.
     */
    public static void blitFBO(FBO source, FBO target, int width, int height, int mask)
    {
        FBO oldRead = readFBO, oldDraw = drawFBO;
        
        bindFBO(source, GL_READ_FRAMEBUFFER);
        bindFBO(target, GL_DRAW_FRAMEBUFFER);
        blitFBO(width, height, mask);
        bindFBO(oldRead, GL_READ_FRAMEBUFFER);
        bindFBO(oldDraw, GL_DRAW_FRAMEBUFFER);
    }
    
    /**
     * @return The currently bound read FBO, or null if bound to the default
     *         frame buffer.
     */
    public static FBO currentReadFBO()
    {
        return readFBO;
    }
    
    /**
     * @return The currently bound draw FBO, or null if bound to the default
     *         frame buffer.
     */
    public static FBO currentDrawFBO()
    {
        return drawFBO;
    }
    
    /**
     * @return The currently bound FBO, or null if either different FBOs are
     *         bound to the read and draw targets, or the default frame buffer
     *         is bound.
     */
    public static FBO currentFBO()
    {
        return readFBO == drawFBO ? readFBO : null;
    }
    // </editor-fold>
    
    /**
     * Draws the given vertex data using the given primitive mode. A shader must
     * be bound.
     * 
     * @param vData The vertex data to render.
     * @param mode An OpenGL primitive draw mode.
     */
    public static void draw(VertexData vData, int mode)
    {
        if (boundProgram == null) throw new IllegalStateException("No shader program is in use.");
        
        int verts = vData.numVertices();
        int inds = vData.numIndices();
        
        VAO.bindFor(null, vData, boundProgram, () ->
        {
            if (inds < 0) glDrawArrays(mode, 0, verts);
            else glDrawElements(mode, inds, GL_UNSIGNED_INT, 0);
        });
    }
    
    /**
     * Performs instanced rendering on the given vertex data, using the given
     * primitive mode. The instance ID may be read as by a vertex shader as
     * {@code gl_InstanceID}.
     * 
     * @param vData The vertex data to render.
     * @param mode An OpenGL primitive draw mode.
     * @param primcount The number of instances to render.
     */
    public static void drawInstanced(VertexData vData, int mode, int primcount)
    {
        if (boundProgram == null) throw new IllegalStateException("No shader program is in use.");
        
        int verts = vData.numVertices();
        int inds = vData.numIndices();
        
        VAO.bindFor(null, vData, boundProgram, () ->
        {
            if (inds < 0) glDrawArraysInstanced(mode, 0, verts, primcount);
            else glDrawElementsInstanced(mode, inds, GL_UNSIGNED_INT, 0, primcount);
        });
    }
    
    /**
     * Performs instanced rendering on the given instance and vertex data, using
     * the given primitive mode. The instance ID may be read as by a vertex
     * shader as {@code gl_InstanceID}, but per-instance data is efficiently
     * provided by the InstanceData.
     * 
     * @param iData The instance data to render.
     * @param vData The vertex data to render.
     * @param mode An OpenGL primitive draw mode.
     */
    public static void drawInstanced(InstanceData iData, VertexData vData, int mode)
    {
        if (boundProgram == null) throw new IllegalStateException("No shader program is in use.");
        
        int primcount = iData.numInstances();
        int verts = vData.numVertices();
        int inds = vData.numIndices();
        
        VAO.bindFor(iData, vData, boundProgram, () ->
        {
            if (inds < 0) glDrawArraysInstanced(mode, 0, verts, primcount);
            else glDrawElementsInstanced(mode, inds, GL_UNSIGNED_INT, 0, primcount);
        });
    }
    
    /**
     * Deletes each DevilGL object in the given array, freeing any native
     * resources associated with those objects.
     * 
     * @param objects An array of DevilGL objects to delete.
     */
    public static void delete(DGLObj... objects)
    {
        for (DGLObj object : objects) if (DGL.objects.contains(object))
        {
            object.delete();
            DGL.objects.remove(object);
            if (object instanceof VAOBindable) VAO.delete((VAOBindable)object);
        }
    }
    
    /**
     * Destroys DGL and releases native resources allocated through init().
     * Native resources allocated through the genXXX() or loadXXX() methods
     * must be freed explicitly through delete() before calling destroy().
     */
    public static void destroy()
    {
        checkState();
        init = false;
        if (getDebugLeakTracking()) for (DGLObj obj : objects) obj.debugLeakTrace();
        Runtime.getRuntime().removeShutdownHook(debugShutdownHook);
        objects = null;
        VAO.terminate();
        
        boundProgram = null;
        readFBO = null;
        drawFBO = null;
        
        DGLException.terminate();
        
        thread = null;
        capabilities = null;
    }
    
    private DGL()
    {
    }
}
