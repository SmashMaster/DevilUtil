package com.samrj.devil.gl;

import com.samrj.devil.math.Vec2;

/**
 * A 'collection' of useful DevilGL utilities.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DGLUtil
{
    /**
     * Creates a new VertexBuffer containing a single full-screen quad. Useful
     * for rendering full-screen effects such as bloom, blur, tonemapping, etc.
     * 
     * @param posname The shader variable name for vertex positions.
     * @return A new, complete VertexBuffer.
     */
    public static VertexBuffer makeFSQ(String posname)
    {
        VertexBuffer out = DGL.genVertexBuffer(4, 0);
        Vec2 pos = out.vec2("in_pos");
        
        out.begin();
        pos.set(-1.0f, -1.0f); out.vertex();
        pos.set(-1.0f,  1.0f); out.vertex();
        pos.set( 1.0f,  1.0f); out.vertex();
        pos.set( 1.0f, -1.0f); out.vertex();
        out.end();
        
        return out;
    }
    
    private DGLUtil()
    {
    }
}
