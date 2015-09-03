package com.samrj.devil.gl;

import com.samrj.devil.io.Memory;
import static com.samrj.devil.io.Memory.memUtil;
import com.samrj.devil.math.Util.PrimType;
import com.samrj.devil.res.Resource;
import com.samrj.devil.util.QuickIdentitySet;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
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
    
    //Allocation fields
    private static Set<Shader> shaders;
    private static Set<ShaderProgram> programs;
    private static Set<VAO> vaos;
    private static Set<VertexData> datas;
    private static Set<Image> images;
    private static Set<Texture> textures;
    private static Set<FBO> fbos;
    
    //State fields
    private static ShaderProgram boundProgram;
    private static VAO boundVAO;
    private static VertexData boundData;
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
        
        if (!capabilities.OpenGL20) throw new RuntimeException(
                "OpenGL version must be at least 2.0 to support DevilGL.");
        
        shaders = new QuickIdentitySet<>();
        programs = new QuickIdentitySet<>();
        vaos = new QuickIdentitySet<>();
        datas = new QuickIdentitySet<>();
        images = new QuickIdentitySet<>();
        textures = new QuickIdentitySet<>();
        fbos = new QuickIdentitySet<>();
        
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
        Shader shader = new Shader(type);
        shaders.add(shader);
        return shader;
    }
    
    /**
     * Generates a new OpenGL shader, loads sources from the given resource
     * path, then compiles the shader.
     * 
     * @param memory The memory to use for buffering sources.
     * @param path The class/file path from which to load sources.
     * @param type The type of shader to load.
     * @return A new, compiled shader.
     * @throws IOException If an I/O error occurs.
     */
    public static Shader loadShader(Memory memory, String path, int type) throws IOException
    {
        Shader shader = genShader(type);
        shader.source(memory, path);
        return shader;
    }
    
    /**
     * Generates a new OpenGL shader, loads sources from the given resource
     * path, then compiles the shader. Uses DevilUtil default memory for
     * buffering the sources--may not be sufficient for large sources!
     * 
     * @param path The class/file path from which to load sources.
     * @param type The type of shader to load.
     * @return A new, compiled shader.
     * @throws IOException If an I/O error occurs.
     */
    public static Shader loadShader(String path, int type) throws IOException
    {
        return loadShader(memUtil, path, type);
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
        shaders.remove(shader);
    }
    
    /**
     * Generates and returns a new shader program.
     * 
     * @return A new shader program.
     */
    public static ShaderProgram genProgram()
    {
        ShaderProgram program = new ShaderProgram();
        programs.add(program);
        return program;
    }
    
    /**
     * Generates, loads, compiles, links, and validates a new shader program
     * using the two given shaders.
     * 
     * @param vertexShader The vertex shader to use.
     * @param fragmentShader The fragment shader to use.
     * @return A new, complete shader program.
     */
    public static ShaderProgram loadProgram(Shader vertexShader, Shader fragmentShader)
    {
        ShaderProgram program = genProgram();
        program.attach(vertexShader);
        program.attach(fragmentShader);
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
     * @param memory The memory to use for buffering sources.
     * @param path The directory and name to load sources from.
     * @return A new, complete shader program.
     * @throws IOException 
     */
    public static ShaderProgram loadProgram(Memory memory, String path) throws IOException
    {
        Shader vertShader = loadShader(memory, path + ".vert", GL20.GL_VERTEX_SHADER);
        Shader fragShader = loadShader(memory, path + ".frag", GL20.GL_FRAGMENT_SHADER);
        return loadProgram(vertShader, fragShader);
    }
    
    /**
     * Generates, loads, compiles, links, and validates a new shader program as
     * well as an underlying vertex and fragment shader. The shader sources are
     * assumed to be in the given path, with the same name, ending in .vert and
     * .frag, respectively. Uses the default DevilUtil memory for buffering
     * sources, which may not be sufficient for large sources.
     * 
     * @param path The directory and name to load sources from.
     * @return A new, complete shader program.
     * @throws IOException 
     */
    public static ShaderProgram loadProgram(String path) throws IOException
    {
        return loadProgram(memUtil, path);
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
     * resources except for the underlying shaders.
     * 
     * @param shaderProgram The shader program to delete.
     * @see com.samrj.devilgl.DGL#deleteDeep(com.samrj.devilgl.ShaderProgram) 
     */
    public static void deleteProgram(ShaderProgram shaderProgram)
    {
        if (boundProgram == shaderProgram) useProgram(null);
        shaderProgram.delete();
        programs.remove(shaderProgram);
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
        deleteProgram(shaderProgram);
        for (Shader shader : shaderProgram.getShaders()) deleteShader(shader);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="VAO methods">
    /**
     * @return A newly created vertex array object.
     */
    public static VAO genVAO()
    {
        VAO vao = capabilities.OpenGL30 ? new VAOGL() : new VAODGL();
        vaos.add(vao);
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
        
        if (boundVAO != null)
        {
            boundVAO.unbind();
            if (boundData != null && boundData.vao == boundVAO)
            {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                boundData = null;
            }
        }
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
    
    /**
     * @param vao Deletes the given vertex array object.
     */
    public static void deleteVAO(VAO vao)
    {
        if (boundVAO == vao) bindVAO(null);
        vao.delete();
        vaos.remove(vao);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Vertex data methods">
    /**
     * Generates a new vertex buffer of the given capacity, using the given
     * memory.
     * 
     * @param memory The memory to buffer within.
     * @param maxVertices The maximum number of vertices to buffer.
     * @param maxIndices The maximum number of indices to buffer.
     * @return A new vertex buffer.
     */
    public static VertexBuffer genVertexBuffer(Memory memory, int maxVertices, int maxIndices)
    {
        VertexBuffer buffer = new VertexBuffer(memory, maxVertices, maxIndices);
        datas.add(buffer);
        return buffer;
    }
    
    /**
     * Generates a new vertex buffer of the given capacity, using the given
     * memory.
     * 
     * @param memory The memory to buffer within.
     * @param maxVertices The maximum number of vertices to buffer.
     * @param maxIndices The maximum number of indices to buffer.
     * @return A new vertex buffer.
     */
    public static VertexStream genVertexStream(Memory memory, int maxVertices, int maxIndices)
    {
        VertexStream stream = new VertexStream(memory, maxVertices, maxIndices);
        datas.add(stream);
        return stream;
    }
    
    /**
     * Binds the given vertex data so that all subsequent draw calls use it.
     * 
     * @param vertexData The vertex data to bind.
     */
    public static void bindData(VertexData vertexData)
    {
        if (boundData == vertexData) return;
        
        if (boundData != null) boundData.unbind();
        if (vertexData != null) vertexData.bind();
        
        boundData = vertexData;
    }
    
    /**
     * @return The currently bound vertex data, or null if none is bound.
     */
    public static VertexData currentData()
    {
        return boundData;
    }
    
    /**
     * Deletes the given vertex data, freeing any associated resources.
     * 
     * @param vertexData The vertex data to delete.
     */
    public static void deleteData(VertexData vertexData)
    {
        if (boundData == vertexData) bindData(null);
        vertexData.delete();
        datas.remove(vertexData);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Image methods">
    /**
     * Allocates a new image buffer in the given memory, with the given
     * dimensions and format. The newly allocated buffer has indeterminate
     * contents.
     * 
     * @param memory The memory to allocate from.
     * @param width The width of the image, in pixels.
     * @param height The height of the image, in pixels.
     * @param bands The number of bands the image will have.
     * @param type The primitive type of the image data.
     * @return A newly allocated image.
     */
    public static Image genImage(Memory memory, int width, int height, int bands, PrimType type)
    {
        if (memory == null) throw new NullPointerException();
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Illegal dimensions specified.");
        if (bands <= 0 || bands > 4)
            throw new IllegalArgumentException("Illegal number of image bands specified.");
        if (!Image.typeSupported(type))
            throw new IllegalArgumentException("Illegal primitive type " + type + " specified.");
        
        Image image = new Image(memory, width, height, bands, type);
        images.add(image);
        return image;
    }
    
    /**
     * Allocates an image  buffer for the given raster, then buffers the raster
     * in it. Returns the allocated buffer.
     * 
     * @param memory The memory to allocate from.
     * @param raster The raster to buffer.
     * @return A newly allocated buffer containing the given raster.
     */
    public static Image loadImage(Memory memory, Raster raster)
    {
        PrimType type = Image.getType(raster);
        if (type == null) throw new IllegalArgumentException("Given raster is not bufferable.");
        
        Image image = genImage(memory, raster.getWidth(), raster.getHeight(), raster.getNumBands(), type);
        image.buffer(raster);
        image.buffer.reset();
        return image;
    }
    
    /**
     * Loads an image type at the given path, which may be a classpath or file
     * path, and then buffers the image in a newly allocated buffer. Returns
     * the image buffer.
     * 
     * @param memory The memory to allocate from.
     * @param path The path to load an image from.
     * @return A newly allocated buffer containing the loaded image.
     * @throws IOException If an io exception occurs.
     */
    public static Image loadImage(Memory memory, String path) throws IOException
    {
        if (memory == null) throw new NullPointerException();
        
        InputStream in = Resource.open(path);
        BufferedImage bImage = ImageIO.read(in);
        in.close();
        if (bImage == null) throw new IOException("Cannot read image from " + path);
        
        return loadImage(memory, bImage.getRaster());
    }
    
    /**
     * Deletes the given image buffer, releasing any native memory associated
     * with it. Can be done safely after the image is loaded into a texture.
     * 
     * @param image The image to delete.
     */
    public static void deleteImage(Image image)
    {
        image.delete();
        images.remove(image);
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
        Texture2D texture = new Texture2D();
        textures.add(texture);
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
        if (image.deleted()) throw new IllegalArgumentException("Given image is deleted.");
        Texture2D texture = genTex2D();
        texture.image(image);
        return texture;
    }
    
    /**
     * Creates a new OpenGL 2D texture using the given raster. Allocates memory
     * to use as an image buffer, uploads the image, and then deallocates the
     * memory.
     * 
     * @param memory The memory to temporarily allocate from.
     * @param raster The raster to load as a texture.
     * @return A new 2D texture object.
     */
    public static Texture2D loadTex2D(Memory memory, Raster raster)
    {
        Image image = loadImage(memory, raster);
        Texture2D texture = loadTex2D(image);
        deleteImage(image);
        return texture;
    }
    
    /**
     * Creates a new openGL 2D texture using the image found at the given path.
     * Allocates memory to use as an image buffer, uploads the image, and then
     * deallocates the memory.
     * 
     * @param memory The memory to temporarily allocate from.
     * @param path The classpath or file path to an image.
     * @return A new 2D texture object.
     * @throws IOException If an io exception occurred.
     */
    public static Texture2D loadTex2D(Memory memory, String path) throws IOException
    {
        Image image = loadImage(memory, path);
        Texture2D texture = loadTex2D(image);
        deleteImage(image);
        return texture;
    }
    
    /**
     * Generates a new OpenGL name for a rectangle texture.
     * 
     * @return A new rectangle texture object.
     */
    public static TextureRectangle genTexRect()
    {
        if (!capabilities.OpenGL31) throw new UnsupportedOperationException(
                "Rectangle textures require OpenGL >= 3.1");
        
        TextureRectangle texture = new TextureRectangle();
        textures.add(texture);
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
        if (image.deleted()) throw new IllegalArgumentException("Given image is deleted.");
        TextureRectangle texture = genTexRect();
        texture.image(image);
        return texture;
    }
    
    /**
     * Creates a new OpenGL rectangle texture using the given raster. Allocates
     * memory to use as an image buffer, uploads the image, and then deallocates
     * the memory.
     * 
     * @param memory The memory to temporarily allocate from.
     * @param raster The raster to load as a texture.
     * @return A new rectangle texture object.
     */
    public static TextureRectangle loadTexRect(Memory memory, Raster raster)
    {
        Image image = loadImage(memory, raster);
        TextureRectangle texture = loadTexRect(image);
        deleteImage(image);
        return texture;
    }
    
    /**
     * Creates a new openGL rectangle texture using the image found at the given
     * path. Allocates memory to use as an image buffer, uploads the image, and
     * then deallocates the memory.
     * 
     * @param memory The memory to temporarily allocate from.
     * @param path The classpath or file path to an image.
     * @return A new rectangle texture object.
     * @throws IOException If an io exception occurred.
     */
    public static TextureRectangle loadTexRect(Memory memory, String path) throws IOException
    {
        Image image = loadImage(memory, path);
        TextureRectangle texture = loadTexRect(image);
        deleteImage(image);
        return texture;
    }
    
    /**
     * Deletes the given texture, releasing any associated hardware memory.
     * 
     * @param texture The texture to delete.
     */
    public static void deleteTex(Texture texture)
    {
        texture.delete();
        textures.remove(texture);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="FBO methods">
    /**
     * @return A newly created frame buffer.
     */
    public static FBO genFBO()
    {
        if (!capabilities.OpenGL30) throw new UnsupportedOperationException(
                "Frame buffers unsupported in OpenGL < 3.0");
        
        FBO fbo = new FBO();
        fbos.add(fbo);
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
     * Deletes the given frame buffer.
     * 
     * @param fbo The frame buffer to delete.
     */
    public static void deleteFBO(FBO fbo)
    {
        if (readFBO == fbo) readFBO = null;
        if (drawFBO == fbo) drawFBO = null;
        fbo.delete();
        fbos.remove(fbo);
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
     * Draws the currently bound vertex data with the shader program currently
     * in use. Fails if either no shader or vertex data is bound/in use.
     * 
     * @param mode An OpenGL primitive draw mode.
     */
    public static void draw(int mode)
    {
        if (boundData == null) throw new IllegalStateException("No vertex data bound.");
        if (boundProgram == null) throw new IllegalStateException("No shader program is in use.");
        
        boundData.draw(mode);
    }
    
    /**
     * Destroys DevilGL and releases all associated resources.
     */
    public static void destroy()
    {
        checkState();
        init = false;
        
        boundProgram = null;
        boundVAO = null;
        boundData = null;
        readFBO = null; drawFBO = null;
        
        for (ShaderProgram program : programs) program.delete();
        for (Shader shader : shaders) shader.delete();
        for (VertexData data : datas) data.delete();
        for (VAO vao : vaos) vao.delete();
        for (Image image : images) image.delete();
        for (Texture texture : textures) texture.delete();
        for (FBO fbo : fbos) fbo.delete();
        
        shaders.clear(); shaders = null;
        programs.clear(); programs = null;
        vaos.clear(); vaos = null;
        datas.clear(); datas = null;
        images.clear(); images = null;
        textures.clear(); textures = null;
        fbos.clear(); fbos = null;
        
        thread = null;
        context = null;
        capabilities = null;
    }
    
    private DGL()
    {
    }
}
