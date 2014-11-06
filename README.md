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

3 vertices and 5 lines. It took me 30 seconds to write and it ran first time without error.
The "getting shit done" to "lines of code" ratio here should make you happy. It makes ME happy.

Here's how you draw the same triangle in forward-compatible OpenGL. The RIGHT way:

(I honestly couldn't fucking get this working. I'm dead serious; I tried for an hour.)

I'll have to finish this later.
