DevilGL
=======

An easier way to use modern OpenGL.

Depcrecated OpenGL is easy. It's simple, straight-forward, and understandable.
Here's how you draw a triangle in immediate mode, using LWJGL:

    GL11.glBegin(GL11.GL_TRIANGLES);
    GL11.glVertex2f(-1.0f, -1.0f);
    GL11.glVertex2f( 0.0f,  1.0f);
    GL11.glVertex2f( 1.0f, -1.0f);
    GL11.glEnd();

3 vertices and 5 lines. It took me 30 seconds to write. The "getting shit done" to "lines of code" ratio here should make you happy. It makes *me* happy.

Here's how you draw the same triangle in forward-compatible OpenGL. The 'right' way:

    float[] vertices = new float[]{
        -1.0f, -1.0f,
         0.0f,  1.0f,
         1.0f, -1.0f
    };

    FloatBuffer vBuffer = ByteBuffer.allocateDirect(vertices.length*4)
                                .order(ByteOrder.nativeOrder())
                                .asFloatBuffer();
    vBuffer.put(vertices);
    vBuffer.rewind();

    int glvBuffer = GL15.glGenBuffers();
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glvBuffer);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuffer, GL15.GL_STREAM_DRAW);

    GL20.glUseProgram(shader.getID());

    int glvArray = GL30.glGenVertexArrays();
    GL30.glBindVertexArray(glvArray);
    int glvAttribLoc = GL20.glGetAttribLocation(shader.getID(), "in_pos");
    GL20.glEnableVertexAttribArray(glvAttribLoc);
    GL20.glVertexAttribPointer(glvAttribLoc, 2, GL11.GL_FLOAT, false, 0, 0);

    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

    GL30.glBindVertexArray(0);
    GL30.glDeleteVertexArrays(glvArray);

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    GL15.glDeleteBuffers(glvBuffer);

Clear, consice, simple, and elegant. Every programmer's dream! And it only took 1 bottle of vodka to write! That's efficiency.

This is a problem.

This is a barrier to entry; this is an extra hour of work in between inspiration and your idea appearing on screen. You simply have to deal with this if you want to use OpenGL properly. It's *bullshit*.

So here's my alternative:

    VertexData vData = new VertexData(3);
    Vec2 pos = vData.vec2("in_pos");
    
    vData.begin();
        pos.set(-1.0f, -1.0f); vData.vertex();
        pos.set( 0.0f,  1.0f); vData.vertex();
        pos.set( 1.0f, -1.0f); vData.vertex();
    vData.upload();
    
    vData.bind(shader);
    vData.draw(GL11.GL_TRIANGLES);
    vData.destroy();
    
That's more like it. DevilGL is doing all the hard work in the background. This retains the power and speed of modern OpenGL, without sacrificing the simplicity or ease of use of classic OpenGL.
