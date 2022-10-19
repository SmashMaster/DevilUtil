package com.samrj.devil.graphics;

import com.samrj.devil.math.*;
import com.samrj.devil.math.zorder.ZOrderCurve;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.lwjgl.system.MemoryUtil.*;

/**
 * Utility methods for noise generation.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class WorleyNoise3D
{
    /**
     * Returns 3D Worley (cellular) noise.
     */
    public static final float worley3d(Random random, long seed, int tiles, Vec3 coord)
    {
        Vec3i iCoord = new Vec3i(Util.floor(coord.x), Util.floor(coord.y), Util.floor(coord.z));
        Vec3 fCoord = new Vec3(Util.fract(coord.x), Util.fract(coord.y), Util.fract(coord.z));

        float minDist = 1.0f;

        Vec3i n = new Vec3i();
        Vec3i nLoop = new Vec3i();
        Vec3 point = new Vec3(), diff = new Vec3();
        for (n.z=-1; n.z<=1; n.z++) for (n.y=-1; n.y<=1; n.y++) for (n.x=-1; n.x<=1; n.x++)
        {
            Vec3i.add(iCoord, n, nLoop);
            Util.loop(nLoop, tiles);
            random.setSeed(Objects.hash(seed, nLoop));
            //Random must be 'warmed up' or its first float will always be the same.
            random.nextLong();
            point.set(random.nextFloat(), random.nextFloat(), random.nextFloat());
            diff.set(n).add(point).sub(fCoord);
            float dist = diff.length();
            minDist = Util.min(minDist, dist);
        }

        return minDist;
    }

    /**
     * Returns 3D Worley (cellular) noise.
     */
    public static final float worley3d(Random random, long seed, int tiles, int resolution, int pixelX, int pixelY, int pixelZ)
    {
        Vec3 coord = new Vec3(pixelX + 0.5f, pixelY + 0.5f, pixelZ + 0.5f).mult(((float)tiles)/resolution);
        return worley3d(random, seed, tiles, coord);
    }

    /**
     * Returns 2D Worley (cellular) noise.
     */
    private static final float worley2d(Random random, long seed, int tiles, Vec2 coord)
    {
        Vec2i iCoord = new Vec2i(Util.floor(coord.x), Util.floor(coord.y));
        Vec2 fCoord = new Vec2(Util.fract(coord.x), Util.fract(coord.y));

        float minDist = 1.0f;

        for (int y=-1; y<=1; y++) for (int x=-1; x<=1; x++)
        {
            Vec2i neighbor = new Vec2i(x, y);

            Vec2i neighborLoop = Vec2i.add(iCoord, neighbor);
            neighborLoop.x = Util.loop(neighborLoop.x, tiles);
            neighborLoop.y = Util.loop(neighborLoop.y, tiles);
            random.setSeed(Objects.hash(seed, neighborLoop));
            //Random must be 'warmed up' or its first float will always be the same.
            random.nextLong();
            Vec2 point = new Vec2(random.nextFloat(), random.nextFloat());
            Vec2 diff = new Vec2(neighbor).add(point).sub(fCoord);
            float dist = diff.length();
            minDist = Math.min(minDist, dist);
        }

        return minDist;
    }

    /**
     * Returns 2D Worley (cellular) noise.
     */
    private static final float worley2d(Random random, long seed, int tiles, int resolution, int pixelX, int pixelY)
    {
        Vec2 coord = new Vec2(pixelX + 0.5f, pixelY + 0.5f).mult(((float)tiles)/resolution);
        return worley2d(random, seed, tiles, coord);
    }

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
