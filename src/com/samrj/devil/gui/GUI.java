package com.samrj.devil.gui;

import com.samrj.devil.game.GLFWUtil;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.math.Vec2i;
import java.nio.ByteBuffer;
import java.util.Objects;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL14C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Utility wrapper for the Nuklear library.
 * 
 * Adopted from LWJGL demo code, Copyright 2019 LWJGL, under this license:
 * https://www.lwjgl.org/license
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class GUI
{
    private static final int COMMANDS_SIZE = 4*1024;
    private static final int VERTEX_BUFFER_SIZE = 512*1024;
    private static final int ELEMENT_BUFFER_SIZE = 128*1024;
    
    private static final String NK_SHADER_VERSION = Platform.get() == Platform.MACOSX ? "#version 150\n" : "#version 300 es\n";
    private static final String VERTEX_SHADER_SOURCE =
            NK_SHADER_VERSION +
            "uniform mat4 ProjMtx;\n" +
            "in vec2 Position;\n" +
            "in vec2 TexCoord;\n" +
            "in vec4 Color;\n" +
            "out vec2 Frag_UV;\n" +
            "out vec4 Frag_Color;\n" +
            "void main() {\n" +
            "   Frag_UV = TexCoord;\n" +
            "   Frag_Color = Color;\n" +
            "   gl_Position = ProjMtx * vec4(Position.xy, 0, 1);\n" +
            "}\n";
    private static final String FRAGMENT_SHADER_SOURCE =
            NK_SHADER_VERSION +
            "precision mediump float;\n" +
            "uniform sampler2D Texture;\n" +
            "in vec2 Frag_UV;\n" +
            "in vec4 Frag_Color;\n" +
            "out vec4 Out_Color;\n" +
            "void main(){\n" +
            "   Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n" +
            "}\n";
    
    private static boolean init;
    private static long window;
    
    private static NkAllocator allocator;
    private static NkContext context;
    private static NkBuffer commands;
    
    private static ShaderProgram shader;
    private static int uniformTex, uniformProjMat;
    
    private static int vao, vbo, ebo;
    private static NkDrawVertexLayoutElement.Buffer vertexLayout;
    
    private static NkDrawNullTexture nullTex;
    
    private static Font currentFont;
    private static int mouseX, mouseY;
    
    /**
     * Sets up the Nuklear library for the specified window and allocates native
     * resources that it needs.
     */
    public static void init(long window)
    {
        if (init) throw new IllegalStateException("Already initialized.");
        init = true;
        GUI.window = window;
        
        //Set up Nuklear context
        allocator = NkAllocator.malloc()
                .alloc((handle, old, size) -> nmemAllocChecked(size))
                .mfree((handle, ptr) -> nmemFree(ptr));
        context = NkContext.malloc();
        commands = NkBuffer.malloc();
        
        nk_init(context, allocator, null);
        nk_buffer_init(commands, allocator, COMMANDS_SIZE);
        
        context.clip().copy((handle, text, len) ->
        {
            if (len == 0) return;

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                ByteBuffer str = stack.malloc(len + 1);
                memCopy(text, memAddress(str), len);
                str.put(len, (byte)0);

                glfwSetClipboardString(window, str);
            }
        })
        .paste((handle, edit) ->
        {
            long text = nglfwGetClipboardString(window);
            if (text != NULL) nnk_textedit_paste(edit, text, nnk_strlen(text));
        });
        
        //Load and set up shader and vertex layout
        shader = DGL.loadProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        
        uniformTex = shader.getUniformLocation("Texture");
        uniformProjMat = shader.getUniformLocation("ProjMtx");
        int attribPos = shader.getAttributeLocation("Position");
        int attribUV  = shader.getAttributeLocation("TexCoord");
        int attribCol = shader.getAttributeLocation("Color");
        
        vbo = glGenBuffers();
        ebo = glGenBuffers();
        vao = glGenVertexArrays();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        
        glEnableVertexAttribArray(attribPos);
        glEnableVertexAttribArray(attribUV);
        glEnableVertexAttribArray(attribCol);
        
        glVertexAttribPointer(attribPos, 2, GL_FLOAT, false, 20, 0);
        glVertexAttribPointer(attribUV, 2, GL_FLOAT, false, 20, 8);
        glVertexAttribPointer(attribCol, 4, GL_UNSIGNED_BYTE, true, 20, 16);

        vertexLayout = NkDrawVertexLayoutElement.malloc(4)
                .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
                .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
                .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
                .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
                .flip();

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        
        //Create null texture
        int nullTexID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, nullTexID);
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        nullTex = NkDrawNullTexture.malloc();
        nullTex.texture().id(nullTexID);
        nullTex.uv().set(0.5f, 0.5f);
    }
    
    /**
     * Sets the primary font being used by this Nuklear context. This must be
     * called before you do anything else with Nuklear, in order to prevent
     * native crashes.
     */
    public static void setStyleFont(Font font)
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        if (font == null) throw new NullPointerException();
        
        currentFont = font;
        nk_style_set_font(context, font.nkFont);
    }
    
    /**
     * Returns the underlying Nuklear context for this library. Throws an
     * IllegalStateException if the library has not been initialized, or if no
     * style font has been set.
     */
    public static NkContext getContext()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        if (currentFont == null) throw new IllegalStateException("No style font has been chosen.");
        
        return context;
    }
    
    /**
     * This should be called once before any of the input methods, like
     * mouseMoved, are called.
     */
    public static void beforeInput()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        
        nk_input_begin(context);
    }
    
    /**
     * This should be called after all input methods have been called.
     */
    public static void afterInput()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        
        NkMouse nkMouse = context.input().mouse();
        if (nkMouse.grab()) glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        else if (nkMouse.ungrab()) glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        nk_input_end(context);
    }
    
    /**
     * Sends a mouse move event to Nuklear. Should be called between the
     * beforeInput and afterInput methods.
     */
    public static void mouseMoved(float x, float y)
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        
        mouseX = (int)x;
        mouseY = GLFWUtil.getWindowSize(window).y - (int)y;
        nk_input_motion(context, mouseX, mouseY);
    }
    
    /**
     * Sends a mouse move event to Nuklear. DX and DY are not used, but are
     * provided so that this method can be used as a functional interface for
     * the Game class. Should be called between the beforeInput and afterInput
     * methods.
     */
    public static void mouseMoved(float x, float y, float dx, float dy)
    {
        mouseMoved(x, y);
    }
    
    /**
     * Sends a mouse button event to Nuklear. Should be called between the
     * beforeInput and afterInput methods.
     */
    public static void mouseButton(int button, int action, int mods)
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        
        boolean pressed = action == GLFW_PRESS;
        switch (button)
        {
            case GLFW_MOUSE_BUTTON_LEFT:
                nk_input_button(context, NK_BUTTON_LEFT, mouseX, mouseY, pressed);
                break;
            case GLFW_MOUSE_BUTTON_RIGHT:
                nk_input_button(context, NK_BUTTON_RIGHT, mouseX, mouseY, pressed);
                break;
            case GLFW_MOUSE_BUTTON_MIDDLE:
                nk_input_button(context, NK_BUTTON_MIDDLE, mouseX, mouseY, pressed);
                break;
        }
    }
    
    /**
     * Sends a mouse scroll event to Nuklear. Should be called between the
     * beforeInput and afterInput methods.
     */
    public static void mouseScroll(float dx, float dy)
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            nk_input_scroll(context, NkVec2.mallocStack(stack).x(dx).y(dy));
        }
    }
    
    /**
     * Sends a unicode character event to Nuklear. Should be called between the
     * beforeInput and afterInput methods.
     */
    public static void character(char character, int codepoint)
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        
        nk_input_unicode(context, codepoint);
    }
    
    /**
     * Sends a keyboard event to Nuklear. Should be called between the
     * beforeInput and afterInput methods.
     */
    public static void key(int key, int action, int mods)
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        
        boolean pressed = action == GLFW_PRESS || action == GLFW_REPEAT;
        boolean control = (mods & GLFW_MOD_CONTROL) != 0;

        switch (key)
        {
            case GLFW_KEY_LEFT_SHIFT:
            case GLFW_KEY_RIGHT_SHIFT:
                nk_input_key(context, NK_KEY_SHIFT, pressed);
                break;
            case GLFW_KEY_LEFT_CONTROL:
            case GLFW_KEY_RIGHT_CONTROL:
                nk_input_key(context, NK_KEY_CTRL, pressed);
                break;
            case GLFW_KEY_DELETE:
                nk_input_key(context, NK_KEY_DEL, pressed);
                break;
            case GLFW_KEY_ENTER:
                nk_input_key(context, NK_KEY_ENTER, pressed);
                break;
            case GLFW_KEY_TAB:
                nk_input_key(context, NK_KEY_TAB, pressed);
                break;
            case GLFW_KEY_BACKSPACE:
                nk_input_key(context, NK_KEY_BACKSPACE, pressed);
                break;
            case GLFW_KEY_C:
                nk_input_key(context, NK_KEY_COPY, control ? pressed : false);
                break;
            case GLFW_KEY_X:
                nk_input_key(context, NK_KEY_CUT, control ? pressed : false);
                break;
            case GLFW_KEY_V:
                nk_input_key(context, NK_KEY_PASTE, control ? pressed : false);
                break;
            case GLFW_KEY_UP:
                nk_input_key(context, NK_KEY_UP, pressed);
                break;
            case GLFW_KEY_DOWN:
                nk_input_key(context, NK_KEY_DOWN, pressed);
                break;
            case GLFW_KEY_LEFT:
                nk_input_key(context, NK_KEY_LEFT, control ? false : pressed);
                nk_input_key(context, NK_KEY_TEXT_WORD_LEFT, control ? pressed : false);
                break;
            case GLFW_KEY_RIGHT:
                nk_input_key(context, NK_KEY_RIGHT, control ? false : pressed);
                nk_input_key(context, NK_KEY_TEXT_WORD_RIGHT, control ? pressed : false);
                break;
            case GLFW_KEY_HOME:
                nk_input_key(context, NK_KEY_TEXT_LINE_START, control ? false : pressed);
                nk_input_key(context, NK_KEY_TEXT_START, control ? pressed : false);
                break;
            case GLFW_KEY_END:
                nk_input_key(context, NK_KEY_TEXT_LINE_END, control ? false : pressed);
                nk_input_key(context, NK_KEY_TEXT_END, control ? pressed : false);
                break;
            case GLFW_KEY_Z:
                nk_input_key(context, NK_KEY_TEXT_UNDO, control ? pressed : false);
                break;
            case GLFW_KEY_Y:
                nk_input_key(context, NK_KEY_TEXT_REDO, control ? pressed : false);
                break;
            case GLFW_KEY_A:
                nk_input_key(context, NK_KEY_TEXT_SELECT_ALL, control ? pressed : false);
                break;
            case GLFW_KEY_PAGE_UP:
                nk_input_key(context, NK_KEY_SCROLL_UP, pressed);
                break;
            case GLFW_KEY_PAGE_DOWN:
                nk_input_key(context, NK_KEY_SCROLL_UP, pressed);
                break;
        }
    }
    
    /**
     * Renders all Nuklear state that has been set up this frame. Should only be
     * called once per frame.
     */
    public static void render()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        
        Vec2i size = GLFWUtil.getWindowSize(window);
        Vec2i res = GLFWUtil.getFramebufferSize(window);
        
        //Set up OpenGL state
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_SCISSOR_TEST);
        glActiveTexture(GL_TEXTURE0);

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            DGL.useProgram(shader);
            glUniform1i(uniformTex, 0);
            glUniformMatrix4fv(uniformProjMat, false, stack.floats(
                2.0f/size.x, 0.0f, 0.0f, 0.0f,
                0.0f, -2.0f/size.y, 0.0f, 0.0f,
                0.0f, 0.0f, -1.0f, 0.0f,
                -1.0f, 1.0f, 0.0f, 1.0f
            ));
            glViewport(0, 0, res.x, res.y);
        }

        // convert from command queue into draw list and draw to screen

        // allocate vertex and element buffer
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

        glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER_SIZE, GL_STREAM_DRAW);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ELEMENT_BUFFER_SIZE, GL_STREAM_DRAW);

        // load draw vertices & elements directly into vertex + element buffer
        ByteBuffer vertices = Objects.requireNonNull(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, VERTEX_BUFFER_SIZE, null));
        ByteBuffer elements = Objects.requireNonNull(glMapBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_WRITE_ONLY, ELEMENT_BUFFER_SIZE, null));

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            // fill convert configuration
            NkConvertConfig config = NkConvertConfig.callocStack(stack)
                .vertex_layout(vertexLayout)
                .vertex_size(20)
                .vertex_alignment(4)
                .null_texture(nullTex)
                .circle_segment_count(22)
                .curve_segment_count(22)
                .arc_segment_count(22)
                .global_alpha(1.0f)
                .shape_AA(NK_ANTI_ALIASING_ON)
                .line_AA(NK_ANTI_ALIASING_ON);

            // setup buffers to load vertices and elements
            NkBuffer vBuf = NkBuffer.mallocStack(stack);
            NkBuffer eBuf = NkBuffer.mallocStack(stack);

            nk_buffer_init_fixed(vBuf, vertices);
            nk_buffer_init_fixed(eBuf, elements);
            nk_convert(context, commands, vBuf, eBuf, config);
        }

        glUnmapBuffer(GL_ELEMENT_ARRAY_BUFFER);
        glUnmapBuffer(GL_ARRAY_BUFFER);

        // iterate over and execute each draw command
        float fb_scale_x = (float)res.x/(float)size.x;
        float fb_scale_y = (float)res.y/(float)size.y;

        long offset = NULL;
        for (NkDrawCommand cmd = nk__draw_begin(context, commands); cmd != null; cmd = nk__draw_next(cmd, commands, context))
        {
            if (cmd.elem_count() == 0) continue;
            glBindTexture(GL_TEXTURE_2D, cmd.texture().id());
            glScissor((int)(cmd.clip_rect().x()*fb_scale_x),
                      (int)((size.y - (int)(cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y),
                      (int)(cmd.clip_rect().w()*fb_scale_x),
                      (int)(cmd.clip_rect().h()*fb_scale_y));
            glDrawElements(GL_TRIANGLES, cmd.elem_count(), GL_UNSIGNED_SHORT, offset);
            offset += cmd.elem_count()*2;
        }

        nk_clear(context);

        //Reset OpenGL state
        DGL.useProgram(null);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glDisable(GL_BLEND);
        glDisable(GL_SCISSOR_TEST);
    }
    
    /**
     * Frees up any resources that were allocated by Nuklear or this library.
     */
    public static void destroy()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        init = false;
        
        currentFont = null;
        
        //Free null texture
        glDeleteTextures(nullTex.texture().id());
        nullTex.free();
        
        //Free vertex layout and shader
        vertexLayout.free();
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        DGL.delete(shader);
        
        //Free context
        context.clip().copy().free();
        context.clip().paste().free();
        nk_free(context);
        nk_buffer_free(commands);
        allocator.alloc().free();
        allocator.mfree().free();
        allocator.free();
        context.free();
        commands.free();
    }
    
    private GUI()
    {
    }
}
