package com.samrj.devil.graphics;

import com.samrj.devil.geo3d.Box3;
import com.samrj.devil.geo3d.Ellipsoid;
import com.samrj.devil.geo3d.OBox3;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.gl.VertexStream;
import com.samrj.devil.math.*;

import static org.lwjgl.opengl.GL11C.*;

/**
 * Utility class for forward-compatible primitive drawing.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class EZDraw
{
    private static final String VERT_SOURCE =
        "#version 140\n" +
        "\n" +
        "uniform mat4 u_projection_matrix;\n" +
        "uniform mat4 u_model_view_matrix;\n" +
        "\n" +
        "in vec3 in_pos;\n" +
        "in vec4 in_color;\n" +
        "\n" +
        "out vec4 v_color;\n" +
        "\n" +
        "void main()\n" +
        "{\n" +
        "    v_color = in_color;\n" +
        "    gl_Position = u_projection_matrix*u_model_view_matrix*vec4(in_pos, 1.0);\n" +
        "}\n";
    
    private static final String FRAG_SOURCE =
        "#version 140\n" +
        "\n" +
        "in vec4 v_color;\n" +
        "\n" +
        "out vec4 out_color;\n" +
        "\n" +
        "void main()\n" +
        "{\n" +
        "    out_color = v_color;\n" +
        "}\n";
    
    private static final int DEFAULT_MAX_VERTS = 4096;
    
    private static ShaderProgram shader;
    private static Mat4 projMat;
    private static MatStack stack;
    private static VertexStream stream;
    private static Vec3 pos;
    private static Vec4 color;
    private static int drawMode;
    private static boolean initialized;
    
    /**
     * Initializes EZDraw, preparing it to draw primitives.
     * 
     * @param maxVertices The maximum number of vertices that can be drawn by a
     *        single draw command. Determines the amount of memory to be
     *        allocated for the vertex buffer.
     */
    public static void init(int maxVertices)
    {
        if (initialized) throw new IllegalStateException("EZDraw already initialized.");
        if (maxVertices <= 0) throw new IllegalArgumentException();
        
        shader = DGL.loadProgram(VERT_SOURCE, FRAG_SOURCE);
        projMat = Mat4.identity();
        stack = new MatStack();
        stream = DGL.genVertexStream(maxVertices, -1);
        pos = stream.vec3("in_pos");
        color = stream.vec4("in_color");
        color.set(1.0f);
        stream.begin();
        
        drawMode = -1;
        initialized = true;
    }
    
    /**
     * Initializes EZDraw, preparing it to draw primitives. Allocates memory for
     * the default maximum vertex count of 4096, using roughly 115 kilobytes of
     * memory.
     */
    public static void init()
    {
        init(DEFAULT_MAX_VERTS);
    }
    
    private static void ensureInitialized()
    {
        if (!initialized) throw new IllegalStateException("EZDraw not initialized.");
    }
    
    /**
     * Begins a draw command. OpenGL constants GL_POINTS, GL_LINE_STRIP,
     * GL_LINE_LOOP, GL_LINES, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN and
     * GL_TRIANGLES are accepted.
     * 
     * @param mode The draw mode to use.
     */
    public static void begin(int mode)
    {
        if (mode < 0) throw new IllegalArgumentException();
        ensureInitialized();
        if (drawMode >= 0) throw new IllegalStateException("Already drawing.");
        drawMode = mode;
    }
    
    /**
     * Sets the projection matrix for subsequent draw commands.
     * 
     * @param matrix The projection matrix to use.
     */
    public static void setProjection(Mat4 matrix)
    {
        ensureInitialized();
        projMat.set(matrix);
    }
    
    /**
     * Sets the view matrix for subsequent draw commands.
     */
    public static MatStack getModelViewStack()
    {
        ensureInitialized();
        return stack;
    }
    
    /**
     * Sets the color of all subsequently emitted vertices. May be called
     * outside of draw commands.
     * 
     * @param vertColor The color to use.
     */
    public static void color(Vec3 vertColor)
    {
        ensureInitialized();
        color.set(vertColor.x, vertColor.y, vertColor.z, 1.0f);
    }
    
    /**
     * Sets the color of all subsequently emitted vertices. May be called
     * outside of draw commands.
     */
    public static void color(float r, float g, float b)
    {
        ensureInitialized();
        color.set(r, g, b, 1.0f);
    }
    
    /**
     * Sets the color of all subsequently emitted vertices. May be called
     * outside of draw commands.
     * 
     * @param vertColor The color to use.
     */
    public static void color(Vec4 vertColor)
    {
        ensureInitialized();
        color.set(vertColor);
    }
    
    /**
     * Sets the color of all subsequently emitted vertices. May be called
     * outside of draw commands.
     */
    public static void color(float r, float g, float b, float a)
    {
        ensureInitialized();
        color.set(r, g, b, a);
    }
    
    private static void ensureDrawing()
    {
        ensureInitialized();
        if (drawMode < 0) throw new IllegalStateException("Not drawing.");
    }
    
    /**
     * Emits a vertex to the underlying EZDraw stream.
     * 
     * @param vertPos The position of the vertex to emit.
     */
    public static void vertex(Vec2 vertPos)
    {
        ensureDrawing();
        pos.set(vertPos.x, vertPos.y, 0.0f);
        stream.vertex();
    }
    
    /**
     * Emits a vertex to the underlying EZDraw stream.
     */
    public static void vertex(float x, float y)
    {
        ensureDrawing();
        pos.set(x, y, 0.0f);
        stream.vertex();
    }
    
    /**
     * Emits a vertex to the underlying EZDraw stream.
     * 
     * @param vertPos The position of the vertex to emit.
     */
    public static void vertex(Vec3 vertPos)
    {
        ensureDrawing();
        pos.set(vertPos);
        stream.vertex();
    }
    
    /**
     * Emits a vertex to the underlying EZDraw stream.
     */
    public static void vertex(float x, float y, float z)
    {
        ensureDrawing();
        pos.set(x, y, z);
        stream.vertex();
    }
    
    public static void ellipsoid(int segments)
    {
        float dt = 8.0f/segments;
        
        begin(GL_LINE_LOOP);
        for (float t=0.0f; t<8.0f; t+=dt)
        {
            Vec2 dir = Util.squareDir(t).normalize();
            vertex(dir.x, dir.y, 0.0f);
        }
        end();
        begin(GL_LINE_LOOP);
        for (float t=0.0f; t<8.0f; t+=dt)
        {
            Vec2 dir = Util.squareDir(t).normalize();
            vertex(dir.x, 0.0f, dir.y);
        }
        end();
        begin(GL_LINE_LOOP);
        for (float t=0.0f; t<8.0f; t+=dt)
        {
            Vec2 dir = Util.squareDir(t).normalize();
            vertex(0.0f, dir.x, dir.y);
        }
        end();
    }
    
    public static void ellipsoid(Ellipsoid e, int segments)
    {
        stack.push();
        stack.mat.translate(e.pos);
        stack.mat.mult(e.radii);
        ellipsoid(segments);
        stack.pop();
    }
    
    public static void box()
    {
        begin(GL_LINES);
        vertex(-1.0f, -1.0f, -1.0f);
        vertex(-1.0f, -1.0f,  1.0f);
        vertex(-1.0f,  1.0f, -1.0f);
        vertex(-1.0f,  1.0f,  1.0f);
        vertex( 1.0f,  1.0f, -1.0f);
        vertex( 1.0f,  1.0f,  1.0f);
        vertex( 1.0f, -1.0f, -1.0f);
        vertex( 1.0f, -1.0f,  1.0f);
        
        vertex(-1.0f, -1.0f, -1.0f);
        vertex(-1.0f,  1.0f, -1.0f);
        vertex(-1.0f, -1.0f,  1.0f);
        vertex(-1.0f,  1.0f,  1.0f);
        vertex( 1.0f, -1.0f,  1.0f);
        vertex( 1.0f,  1.0f,  1.0f);
        vertex( 1.0f, -1.0f, -1.0f);
        vertex( 1.0f,  1.0f, -1.0f);
        
        vertex(-1.0f, -1.0f, -1.0f);
        vertex( 1.0f, -1.0f, -1.0f);
        vertex(-1.0f, -1.0f,  1.0f);
        vertex( 1.0f, -1.0f,  1.0f);
        vertex(-1.0f,  1.0f,  1.0f);
        vertex( 1.0f,  1.0f,  1.0f);
        vertex(-1.0f,  1.0f, -1.0f);
        vertex( 1.0f,  1.0f, -1.0f);
        end();
    }
    
    public static void box(Box3 box)
    {
        Vec3 pos = Vec3.add(box.min, box.max).mult(0.5f);
        Vec3 sca = Vec3.sub(box.max, box.min).mult(0.5f);
        stack.push();
        stack.mat.translate(pos);
        stack.mat.mult(sca);
        box();
        stack.pop();
    }
    
    public static void oBox(OBox3 box)
    {
        stack.push();
        stack.mat.mult(box.transform);
        box();
        stack.pop();
    }
    
    public static void circle(int segments)
    {
        float dt = 8.0f/segments;
        
        begin(GL_LINE_LOOP);
        for (float t=0.0f; t<8.0f; t+=dt)
        {
            Vec2 dir = Util.squareDir(t).normalize();
            vertex(dir.x, dir.y, 0.0f);
        }
        end();
    }
    
    public static void circle(Vec2 pos, float radius, int segments)
    {
        stack.push();
        stack.mat.translate(new Vec3(pos, 0.0f));
        stack.mat.mult(new Vec3(radius, radius, 1.0f));
        circle(segments);
        stack.pop();
    }
    
    /**
     * Ends the current draw command, uploading the data to the GPU and drawing
     * to the current frame buffer.
     */
    public static void end()
    {
        ensureDrawing();
        
        ShaderProgram oldShader = DGL.currentProgram();
        
        stream.upload();
        DGL.useProgram(shader);
        shader.uniformMat4("u_projection_matrix", projMat);
        shader.uniformMat4("u_model_view_matrix", stack.mat);
        DGL.draw(stream, drawMode);
        drawMode = -1;
        
        DGL.useProgram(oldShader);
    }
    
    /**
     * Releases all system resources associated with EZDraw.
     */
    public static void destroy()
    {
        ensureInitialized();
        initialized = false;
        
        DGL.delete(stream, shader);
        shader = null;
        projMat = null;
        stack = null;
        stream = null;
        pos = null;
        color = null;
        drawMode = -1;
    }
    
    private EZDraw()
    {
    }
}
