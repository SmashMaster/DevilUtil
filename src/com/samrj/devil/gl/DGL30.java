package com.samrj.devil.gl;

class DGL30 extends DGLImpl
{
    DGL30()
    {
    }
    
    @Override
    public int vertex()
    {
        ensureState(State.DEFINE_MESH, State.DRAW_MESH);
        return mesh.vertex();
    }
    
    @Override
    public void index(int index)
    {
        ensureState(State.DEFINE_MESH, State.DRAW_MESH);
        mesh.index(index);
    }
    
    @Override
    public Mesh30 define(Mesh30.Type type, Mesh30.RenderMode mode)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        state = State.DEFINE_MESH;
        mesh = new Mesh30(type, Mesh30.Usage.GL_STATIC_DRAW, mode, activeAttribs);
        return mesh;
    }
    
    @Override
    public Mesh30 define(Mesh30.Type type)
    {
        return define(type, Mesh30.RenderMode.TRIANGLES);
    }
    
    @Override
    public void draw(Mesh mesh)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        mesh.draw();
    }
    
    @Override
    public void draw(Mesh30.Type type, Mesh30.RenderMode mode)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        state = State.DRAW_MESH;
        mesh = new Mesh30(type, Mesh30.Usage.GL_STREAM_DRAW, mode, activeAttribs);
    }
    
    @Override
    public void draw(Mesh30.Type type)
    {
        draw(type, Mesh30.RenderMode.TRIANGLES);
    }
    
    @Override
    public void end()
    {
        ensureState(State.DEFINE_MESH, State.DRAW_MESH);
        boolean drawMesh = state == State.DRAW_MESH;
        mesh.complete();
        state = State.IDLE;
        if (drawMesh)
        {
            draw(mesh);
            mesh.destroy();
        }
        mesh = null;
    }
}
