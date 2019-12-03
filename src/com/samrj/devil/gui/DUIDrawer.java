package com.samrj.devil.gui;

import com.samrj.devil.gl.*;
import com.samrj.devil.gui.Font.BakedChar;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec4;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;

/**
 * Used for internal DevilUI draw calls, but also exposed for convenience.
 */
public class DUIDrawer
{
    private static final String VERTEX_SHADER_SOURCE =
            "#version 140\n" +
            "uniform mat3 u_matrix;\n" +
            "in vec2 in_pos;\n" +
            "in vec2 in_tex_coord;\n" +
            "in vec4 in_color;\n" +
            "out vec2 v_tex_coord;\n" +
            "out vec4 v_color;\n" +
            "void main()\n" +
            "{\n" +
            "    v_tex_coord = in_tex_coord;\n" +
            "    v_color = in_color;\n" +
            "    gl_Position = vec4(u_matrix*vec3(in_pos, 1.0), 1.0);\n" +
            "}";
    private static final String FRAGMENT_SHADER_SOURCE =
            "#version 140\n" +
            "uniform sampler2D u_texture;\n" +
            "in vec2 v_tex_coord;\n" +
            "in vec4 v_color;\n" +
            "out vec4 out_color;\n" +
            "void main()\n" +
            "{\n" +
            "    out_color = texture(u_texture, v_tex_coord).brga*v_color;\n" +
            "}";
    private static final int MAX_VERTICES = 1024;
    
    
    private final Texture2D nullTexture;
    private final ShaderProgram shader;
    private final VertexStream stream;
    private final Vec2 pos;
    private final Vec2 texCoord;
    private final Vec4 color;
    
    DUIDrawer()
    {
        nullTexture = DGL.genTex2D();
        Image nullImage = DGL.genImage(1, 1, 4, Util.PrimType.BYTE);
        nullImage.shade((x, y, b) -> 255.0);
        nullTexture.image(nullImage);
        DGL.delete(nullImage);
        
        shader = DGL.loadProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        
        stream = DGL.genVertexStream(MAX_VERTICES, -1);
        pos = stream.vec2("in_pos");
        texCoord = stream.vec2("in_tex_coord");
        color = stream.vec4("in_color");
        color.set(1.0f);
        stream.begin();
    }
    
    /**
     * Prepares for a series of draw commands by enabling blending; and binding
     * and configuring the UIDraw shader. The correct glViewport should be set
     * before calling this. It is the application's responsibility to make sure
     * that GL_DEPTH_TEST, GL_STENCIL_TEST, GL_CULL_FACE, or any other OpenGL
     * state that might interfere with UIDraw, are set to default.
     */
    public DUIDrawer begin()
    {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        Vec2 viewport = DUI.viewport();
        Mat3 matrix = Mat3.orthographic(0.0f, viewport.x, 0.0f, viewport.y);
        
        DGL.useProgram(shader);
        shader.uniformMat3("u_matrix", matrix);
        
        return this;
    }
    
    /**
     * Sets the current draw color.
     */
    public DUIDrawer color(float r, float g, float b, float a)
    {
        color.set(r, g, b, a);
        return this;
    }
    
    /**
     * Sets the current draw color.
     */
    public DUIDrawer color(Vec4 color)
    {
        this.color.set(color);
        return this;
    }
    
    public DUIDrawer line(float x0, float x1, float y0, float y1)
    {
        pos.set(x0, y0); texCoord.set(0.0f, 0.0f); stream.vertex();
        pos.set(x1, y1); texCoord.set(1.0f, 1.0f); stream.vertex();
        
        stream.upload();
        nullTexture.bind(GL_TEXTURE0);
        DGL.draw(stream, GL_LINE_LOOP);
        
        return this;
    }
    
    /**
     * Draws a rectangle outline at the given position.
     */
    public DUIDrawer rect(float x0, float x1, float y0, float y1)
    {
        pos.set(x0, y0); texCoord.set(0.0f, 0.0f); stream.vertex();
        pos.set(x0, y1); texCoord.set(0.0f, 1.0f); stream.vertex();
        pos.set(x1, y1); texCoord.set(1.0f, 1.0f); stream.vertex();
        pos.set(x1, y0); texCoord.set(1.0f, 0.0f); stream.vertex();
        
        stream.upload();
        nullTexture.bind(GL_TEXTURE0);
        DGL.draw(stream, GL_LINE_LOOP);
        
        return this;
    }
    
    /**
     * Draws a rectangle outline at the given position.
     */
    public DUIDrawer rectFill(float x0, float x1, float y0, float y1)
    {
        pos.set(x0, y0); texCoord.set(0.0f, 0.0f); stream.vertex();
        pos.set(x0, y1); texCoord.set(0.0f, 1.0f); stream.vertex();
        pos.set(x1, y1); texCoord.set(1.0f, 1.0f); stream.vertex();
        pos.set(x1, y0); texCoord.set(1.0f, 0.0f); stream.vertex();
        
        stream.upload();
        nullTexture.bind(GL_TEXTURE0);
        DGL.draw(stream, GL_TRIANGLE_FAN);
        
        return this;
    }
    
    /**
     * Draws text at the specified position as the upper-left corner, using the
     * given font and string.
     */
    public DUIDrawer text(String text, Font font, float x, float y)
    {
        if (font.isDestroyed()) throw new IllegalStateException("Font destroyed.");
        if (text.isEmpty()) return this;
        
        BakedChar[] chars = font.chars;
        int length = text.length();
        
        for (int offset = 0; offset < length;)
        {
           int codepoint = text.codePointAt(offset);
           BakedChar c = codepoint < chars.length ? chars[codepoint] : Font.NULL_CHAR;
           
           float x0 = x + c.offsetX;
           float x1 = x0 + c.width;
           float y1 = y - c.offsetY;
           float y0 = y1 - c.height;
           
           pos.set(x0, y0); texCoord.set(c.uvS0, c.uvT1); stream.vertex();
           pos.set(x0, y1); texCoord.set(c.uvS0, c.uvT0); stream.vertex();
           pos.set(x1, y1); texCoord.set(c.uvS1, c.uvT0); stream.vertex();
           
           pos.set(x0, y0); texCoord.set(c.uvS0, c.uvT1); stream.vertex();
           pos.set(x1, y1); texCoord.set(c.uvS1, c.uvT0); stream.vertex();
           pos.set(x1, y0); texCoord.set(c.uvS1, c.uvT1); stream.vertex();
           
           x += c.advance;
           offset += Character.charCount(codepoint);
        }
        
        stream.upload();
        font.texture.bind(GL_TEXTURE0);
        DGL.draw(stream, GL_TRIANGLES);
        
        return this;
    }
    
    public DUIDrawer text(String text, Font font, float x, float y, Vec2 align)
    {
        Vec2 aligned = Align.toEdge(font.getSize(text), x, y, align);
        return text(text, font, aligned.x, aligned.y);
    }
    
    void destroy()
    {
        DGL.delete(nullTexture, shader, stream);
    }
}
