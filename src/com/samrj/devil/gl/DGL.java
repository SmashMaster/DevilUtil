package com.samrj.devil.gl;

import com.samrj.devil.graphics.TexUtil;
import com.samrj.devil.math.Util.PrimType;
import com.samrj.devil.res.Resource;
import com.samrj.devil.util.IdentitySet;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;

/**
 * DevilGL. A state-based, object-oriented, forward compatible OpenGL wrapper;
 * inspired by deprecated OpenGL.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class DGL
{
    //Constant fields
    private static boolean init;
    private static Thread thread;
    private static GLContext context;
    private static ContextCapabilities capabilities;
    private static Set<DGLObj> objects;
    
    //State fields
    private static ShaderProgram boundProgram;
    private static VAO boundVAO;
    private static FBO readFBO, drawFBO;
    
    private static void checkState()
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
        context = GLContext.createFromCurrent();
        capabilities = context.getCapabilities();
        objects = new IdentitySet<>();
        init = true;
    }
    
    /**
     * @return The current OpenGL context's capabilities.
     */
    public static ContextCapabilities getCapabilities()
    {
        checkState();
        return capabilities;
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
        checkState();
        if (!capabilities.OpenGL20) throw new UnsupportedOperationException(
                "Shaders unsupported in OpenGL < 2.0");
        
        Shader shader = new Shader(type);
        objects.add(shader);
        return shader;
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
        Shader shader = genShader(type);
        shader.source(path);
        return shader;
    }
    
    /**
     * Deletes the given shader and releases all associated system resources.
     * Ensure that no shader programs rely on the given shader.
     * 
     * @param shader The shader to delete.
     */
    public static void deleteShader(Shader shader)
    {
        shader.delete();
        objects.remove(shader);
    }
    
    /**
     * Generates and returns a new shader program.
     * 
     * @return A new shader program.
     */
    public static ShaderProgram genProgram()
    {
        checkState();
        if (!capabilities.OpenGL20) throw new UnsupportedOperationException(
                "Shader programs unsupported in OpenGL < 2.0");
        
        ShaderProgram program = new ShaderProgram();
        objects.add(program);
        return program;
    }
    
    /**
     * Generates, loads, compiles, links, and validates a new shader program
     * using any number of given shaders.
     * 
     * @param shaders An array of shaders to attach.
     * @return A new, complete shader program.
     */
    public static ShaderProgram loadProgram(Shader... shaders)
    {
        ShaderProgram program = genProgram();
        for (Shader shader : shaders) program.attach(shader);
        program.link();
        program.validate();
        return program;
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
        Shader vertShader = loadShader(path + ".vert", GL20.GL_VERTEX_SHADER);
        Shader fragShader = loadShader(path + ".frag", GL20.GL_FRAGMENT_SHADER);
        return loadProgram(vertShader, fragShader);
    }
    
    /**
     * Uses the given shader program for any subsequent draw calls. Pass null to
     * unbind the current shader.
     * 
     * @param shaderProgram The shader program to use.
     */
    public static void useProgram(ShaderProgram shaderProgram)
    {
        if (boundProgram == shaderProgram) return;
        
        if (shaderProgram == null) GL20.glUseProgram(0);
        else shaderProgram.use();
        
        boundProgram = shaderProgram;
    }
    
    /**
     * @return The shader program currently in use, or null if none is in use.
     */
    public static ShaderProgram currentProgram()
    {
        return boundProgram;
    }
    
    /**
     * Deletes the given shader program, releasing all associated system
     * resources, including the underlying shaders. Ensure that no other shader
     * programs rely on the underlying shaders before calling this.
     * 
     * @param shaderProgram The shader program to delete.
     * @see com.samrj.devilgl.DGL#delete(com.samrj.devilgl.ShaderProgram) 
     */
    public static void deleteProgramDeep(ShaderProgram shaderProgram)
    {
        delete(shaderProgram);
        for (Shader shader : shaderProgram.getShaders()) delete(shader);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="VAO methods">
    /**
     * @return A newly created vertex array object.
     */
    public static VAO genVAO()
    {
        checkState();
        if (!capabilities.OpenGL30) throw new UnsupportedOperationException(
                "Vertex arrays unsupported in OpenGL < 3.0");
        
        VAO vao = new VAO();
        objects.add(vao);
        return vao;
    }
    
    /**
     * Creates a new vertex array and then links the given vertex data and
     * shader program with it. The created array may be used to render the given
     * data with the given shader.
     * 
     * @param data The vertex data to link.
     * @param program The shader program to link.
     * @return A newly created and linked vertex array object.
     */
    public static VAO genVAO(VertexData data, ShaderProgram program)
    {
        VAO oldVAO = currentVAO();
        VAO vao = genVAO();
        bindVAO(vao);
        vao.link(data, program);
        bindVAO(oldVAO);
        return vao;
    }
    
    /**
     * Binds the given vertex array object, unbinding any previously bound VAO.
     * 
     * @param vao The vertex array to bind.
     */
    public static void bindVAO(VAO vao)
    {
        if (boundVAO == vao) return;
        
        if (boundVAO != null) boundVAO.unbind();
        if (vao != null) vao.bind();
        
        boundVAO = vao;
    }
    
    /**
     * @return The currently bound vertex array, or null if none is bound.
     */
    public static VAO currentVAO()
    {
        return boundVAO;
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
        checkState();
        if (!capabilities.OpenGL20) throw new UnsupportedOperationException(
                "Vertex builders unsupported in OpenGL < 2.0");
        
        VertexBuffer buffer = new VertexBuffer(maxVertices, maxIndices);
        objects.add(buffer);
        return buffer;
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
        checkState();
        if (!capabilities.OpenGL20) throw new UnsupportedOperationException(
                "Vertex builders unsupported in OpenGL < 2.0");
        
        VertexStream stream = new VertexStream(maxVertices, maxIndices);
        objects.add(stream);
        return stream;
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
        checkState();
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Illegal dimensions specified.");
        if (bands <= 0 || bands > 4)
            throw new IllegalArgumentException("Illegal number of image bands specified.");
        if (!Image.typeSupported(type))
            throw new IllegalArgumentException("Illegal primitive type " + type + " specified.");
        
        Image image = new Image(width, height, bands, type);
        objects.add(image);
        return image;
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
        
        Image image = genImage(raster.getWidth(), raster.getHeight(), raster.getNumBands(), type);
        image.buffer(raster);
        image.buffer.rewind();
        return image;
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
        InputStream in = Resource.open(path);
        BufferedImage bImage = ImageIO.read(in);
        in.close();
        if (bImage == null) throw new IOException("Cannot read image from " + path);
        
        return loadImage(bImage.getRaster());
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Texture methods">
    /**
     * Generates a new OpenGL name for a 2D texture.
     * 
     * @return A new 2D texture object.
     */
    public static Texture2D genTex2D()
    {
        checkState();
        Texture2D texture = new Texture2D();
        objects.add(texture);
        return texture;
    }
    
    /**
     * Creates a new OpenGL 2D texture using the given image.
     * 
     * @param image The image to load as a texture.
     * @return A new 2D texture object.
     */
    public static Texture2D loadTex2D(Image image)
    {
        Texture2D texture = genTex2D();
        texture.image(image);
        return texture;
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
        checkState();
        Texture3D texture = new Texture3D();
        objects.add(texture);
        return texture;
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
        checkState();
        if (!(capabilities.OpenGL31 || capabilities.GL_ARB_texture_rectangle))
            throw new UnsupportedOperationException(
                "Rectangle textures unsupported on this machine.");
        
        TextureRectangle texture = new TextureRectangle();
        objects.add(texture);
        return texture;
    }
    
    /**
     * Creates a new OpenGL rectangle texture using the given image.
     * 
     * @param image The image to load as a texture.
     * @return A new rectangle texture object.
     */
    public static TextureRectangle loadTexRect(Image image)
    {
        TextureRectangle texture = genTexRect();
        texture.image(image);
        return texture;
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
        checkState();
        Texture2DArray texture = new Texture2DArray();
        objects.add(texture);
        return texture;
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
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="FBO methods">
    /**
     * @return A newly created frame buffer.
     */
    public static FBO genFBO()
    {
        checkState();
        if (!capabilities.OpenGL30) throw new UnsupportedOperationException(
                "Frame buffers unsupported in OpenGL < 3.0");
        
        FBO fbo = new FBO();
        objects.add(fbo);
        return fbo;
    }
    
    /**
     * Binds the given frame buffer to the given target. Pass null to bind the
     * default frame buffer.
     * 
     * @param fbo The frame buffer to bind.
     * @param target The targe to bind to, or null to bind the default frame
     *        buffer;
     */
    public static void bindFBO(FBO fbo, int target)
    {
        switch (target)
        {
            case GL30.GL_FRAMEBUFFER: readFBO = fbo; drawFBO = fbo; break;
            case GL30.GL_READ_FRAMEBUFFER: readFBO = fbo; break;
            case GL30.GL_DRAW_FRAMEBUFFER: drawFBO = fbo; break;
            default: throw new IllegalArgumentException("Illegal target specified.");
        }
        
        if (fbo != null) fbo.bind(target);
        else GL30.glBindFramebuffer(target, 0);
    }
    
    /**
     * Binds the given frame buffer to read from and draw to. Pass null to bind
     * the default frame buffer.
     * 
     * @param fbo The frame buffer to bind, or null to bind the default frame
     *        buffer.
     */
    public static void bindFBO(FBO fbo)
    {
        bindFBO(fbo, GL30.GL_FRAMEBUFFER);
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
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, mask, GL11.GL_NEAREST);
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
        
        bindFBO(source, GL30.GL_READ_FRAMEBUFFER);
        bindFBO(target, GL30.GL_DRAW_FRAMEBUFFER);
        blitFBO(width, height, mask);
        bindFBO(oldRead, GL30.GL_READ_FRAMEBUFFER);
        bindFBO(oldDraw, GL30.GL_DRAW_FRAMEBUFFER);
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
     * Draws the given vertex data using the given primitive mode. If using a
     * VAO, it must be compatible with the given data, or this method will have
     * undefined behavior. If not using a VAO, attributes will be automatically
     * enabled, but this is not recommended.
     * 
     * @param data The vertex data to render.
     * @param mode An OpenGL primitive draw mode.
     */
    public static void draw(VertexData data, int mode)
    {
        if (boundProgram == null) throw new IllegalStateException("No shader program is in use.");
        
        int verts = data.numVertices();
        int inds = data.numIndices();
        
        //TODO: Automatically enable and disable attributes if no VAO is bound.
        
        if (inds < 0) GL11.glDrawArrays(mode, 0, verts);
        else GL11.glDrawElements(mode, inds, GL11.GL_UNSIGNED_INT, 0);
    }
    
    /**
     * Deletes each DevilGL object in the given array.
     * 
     * @param objects An array of DevilGL objects to delete.
     */
    public static void delete(DGLObj... objects)
    {
        for (DGLObj object : objects)
        {
            object.delete();
            DGL.objects.remove(object);
        }
    }
    
    /**
     * Destroys DevilGL and releases all associated resources.
     */
    public static void destroy()
    {
        checkState();
        init = false;
        
        for (DGLObj obj : objects) obj.delete();
        objects.clear();
        objects = null;
        
        boundProgram = null;
        boundVAO = null;
        readFBO = null;
        drawFBO = null;
        
        thread = null;
        context = null;
        capabilities = null;
    }
    
    private DGL()
    {
    }
}
