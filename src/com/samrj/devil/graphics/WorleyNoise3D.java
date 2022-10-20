package com.samrj.devil.graphics;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec3i;

import java.util.random.RandomGenerator;

/**
 * Utility methods for noise generation.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class WorleyNoise3D
{
    private final int size, size2;
    private final float[] cellData;

    public WorleyNoise3D(int size, RandomGenerator random)
    {
        if (size <= 0) throw new IllegalArgumentException();
        if (size > 1023) throw new IllegalArgumentException("Grid sizes above 1023 not supported.");
        this.size = size;
        size2 = size*size;

        int floats = size*size*size*3;
        cellData = new float[floats];
        for (int i=0; i<floats; i++) cellData[i] = random.nextFloat();
    }

    public float getMinDist(Vec3 coord)
    {
        Vec3i iCoord = new Vec3i(Util.floor(coord.x), Util.floor(coord.y), Util.floor(coord.z));
        Vec3 fCoord = new Vec3(Util.fract(coord.x), Util.fract(coord.y), Util.fract(coord.z));

        Vec3i n = new Vec3i();
        Vec3i nLoop = new Vec3i();
        Vec3 point = new Vec3(), diff = new Vec3();
        float minDist = 1.0f;
        for (n.z=-1; n.z<=1; n.z++) for (n.y=-1; n.y<=1; n.y++) for (n.x=-1; n.x<=1; n.x++)
        {
            Vec3i.add(iCoord, n, nLoop);
            Util.loop(nLoop, size);
            int index = (nLoop.x*size2 + nLoop.y*size + nLoop.z)*3;
            point.set(cellData[index], cellData[index+1], cellData[index+2]);

            diff.set(n).add(point).sub(fCoord);
            float dist = diff.length();
            minDist = Util.min(minDist, dist);
        }

        return minDist;
    }
}
