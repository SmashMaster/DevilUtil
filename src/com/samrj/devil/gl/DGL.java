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
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

/**
 * DevilGL. A state-based, object-oriented, forward compatible OpenGL wrapper,
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
    
    //State fields
    private static ShaderProgram boundProgram;
    private static VAO boundVAO;
    private static VertexData boundData;
    
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
    public static VAO genVAO()
    {
        VAO vao = capabilities.OpenGL30 ? new VAOGL() : new VAODGL();
        vaos.add(vao);
        return vao;
    }
    
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
    
    public static VAO currentVAO()
    {
        return boundVAO;
    }
    
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
     * Destroys DevilGL and releases any associated resources.
     */
    public static void destroy()
    {
        checkState();
        init = false;
        
        for (ShaderProgram program : programs) program.delete();
        for (Shader shader : shaders) shader.delete();
        for (VertexData data : datas) data.delete();
        for (VAO vao : vaos) vao.delete();
        for (Image image : images) image.delete();
        
        shaders.clear(); shaders = null;
        programs.clear(); programs = null;
        vaos.clear(); vaos = null;
        datas.clear(); datas = null;
        images.clear(); images = null;
        
        thread = null;
        context = null;
        capabilities = null;
    }
    
    private DGL()
    {
    }
}
