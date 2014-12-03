package com.samrj.devil.gl;

interface DGLImpl
{
    void use(ShaderProgram shader);
    Uniform getUniform(String name);
    Attribute getAttribute(String name);
    void use(Attribute... atts);
    int vertex();
    void index(int index);
    Mesh define(Mesh.Type type, Mesh.RenderMode mode);
    Mesh define(Mesh.Type type);
    void draw(Mesh mesh);
    void draw(Mesh.Type type, Mesh.RenderMode mode);
    void draw(Mesh.Type type);
    void end();
    void clearColor(float r, float g, float b, float a);
    void clear(DGL.ScreenBuffer... buffers);
}
