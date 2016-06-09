package com.samrj.devil.gl;

import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import org.lwjgl.opengl.GL13;

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
    
    private static final float SQR2 = (float)(Math.sqrt(2.0)/2.0);
    
    /**
     * Returns the rotation a camera must have in order to render the given face
     * of a cubemap properly.
     * 
     * @param face The face index of a cubemap.
     * @param result A quaternion in which to store the result.
     */
    public static void getCubemapFaceDir(int face, Quat result)
    {
        switch(face)
        {
            case 0: result.set(0.0f,  SQR2,  0.0f, -SQR2); return; //+X
            case 1: result.set(0.0f,  SQR2,  0.0f,  SQR2); return; //-X
            case 2: result.set(SQR2, -SQR2,  0.0f,  0.0f); return; //+Y
            case 3: result.set(SQR2,  SQR2,  0.0f,  0.0f); return; //-Y
            case 4: result.set(0.0f,  1.0f,  0.0f,  0.0f); return; //+Z
            case 5: result.set(0.0f,  0.0f,  0.0f,  1.0f); return; //-Z
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    private DGLUtil()
    {
    }
}
