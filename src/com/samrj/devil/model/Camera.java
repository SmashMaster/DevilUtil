package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;

import java.io.IOException;

/**
 * Bare-bones Blender camera object.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2025 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Camera extends DataBlockAnimatable
{
    /**
     * struct Camera {
     *     ID id;
     *     AnimData *adt;
     *     char type;
     *     char dtx;
     *     short flag;
     *     float passepartalpha;
     *     float clipsta;
     *     float clipend;
     *     float lens;
     *     float ortho_scale;
     *     float drawsize;
     *     float sensor_x;
     *     float sensor_y;
     *     float shiftx;
     *     float shifty;
     *     float YF_dofdist;
     *     Ipo *ipo;
     *     Object *dof_ob;
     *     GPUDOFSettings gpu_dof;
     *     CameraDOFSettings dof;
     *     ListBase bg_images;
     *     char sensor_fit;
     *     char _pad[7];
     *     CameraStereoSettings stereo;
     *     Camera_Runtime runtime;
     * };
     */

    public final float focalLength;

    Camera(Model model, BlendFile.Pointer bCamera)
    {
        super(model, bCamera);

        focalLength = bCamera.getField("lens").asFloat();
    }
}
