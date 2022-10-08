package com.samrj.devil.gl;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;

public final class ImageCubemap extends DGLObj
{
    private static final int POS_X = 0;
    private static final int NEG_X = 1;
    private static final int POS_Y = 2;
    private static final int NEG_Y = 3;
    private static final int POS_Z = 4;
    private static final int NEG_Z = 5;

    final Image[] images = new Image[6];

    ImageCubemap(int resolution, int bands, Util.PrimType type)
    {
        for (int i=0; i<6; i++) images[i] = new Image(resolution, resolution, bands, type);
    }

    public void sample(Vec3 coord, Vec4 result)
    {
        float absX = Math.abs(coord.x);
        float absY = Math.abs(coord.y);
        float absZ = Math.abs(coord.z);

        float max, u, v;
        int face;

        if (absX >= absY && absX >= absZ)
        {
            max = absX;
            if (coord.x > 0) // POSITIVE X
            {
                u = -coord.z;
                v = coord.y;
                face = POS_X;
            }
            else // NEGATIVE X
            {
                u = coord.z;
                v = coord.y;
                face = NEG_X;
            }
        }
        else if (absY >= absX && absY >= absZ)
        {
            max = absY;
            if (coord.y > 0) // POSITIVE Y
            {
                u = coord.x;
                v = -coord.z;
                face = POS_Y;
            }
            else // NEGATIVE Y
            {
                u = coord.x;
                v = coord.z;
                face = NEG_Y;
            }
        }
        else
        {
            max = absZ;
            if (coord.z > 0) // POSITIVE Z
            {
                u = coord.x;
                v = coord.y;
                face = POS_Z;
            }
            else // NEGATIVE Z
            {
                u = -coord.x;
                v = coord.y;
                face = NEG_Z;
            }
        }

        u = 0.5f*(u/max + 1.0f);
        v = 0.5f*(v/max + 1.0f);

        Image image = images[face];

        int pixelU = (int)Math.floor(u*image.width);
        int pixelV = (int)Math.floor(v*image.height);

        for (int band=0; band<image.bands; band++)
            result.setComponent(band, image.getFloat(pixelU, pixelV, band));
    }

    @Override
    void delete()
    {
        for (Image image : images) image.delete();
    }
}
