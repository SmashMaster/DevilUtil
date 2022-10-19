package com.samrj.devil.graphics;

import com.samrj.devil.math.*;

import java.util.Objects;
import java.util.Random;

/**
 * Utility methods for noise generation.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Noise
{
    /**
     * Returns 3D Worley (cellular) noise.
     */
    public static final float worley3d(Random random, long seed, int tiles, Vec3 coord)
    {
        Vec3i iCoord = new Vec3i(Util.floor(coord.x), Util.floor(coord.y), Util.floor(coord.z));
        Vec3 fCoord = new Vec3(Util.fract(coord.x), Util.fract(coord.y), Util.fract(coord.z));

        float minDist = 1.0f;

        for (int z=-1; z<=1; z++) for (int y=-1; y<=1; y++) for (int x=-1; x<=1; x++)
        {
            Vec3i neighbor = new Vec3i(x, y, z);

            Vec3i neighborLoop = Vec3i.add(iCoord, neighbor);
            neighborLoop.x = Util.loop(neighborLoop.x, tiles);
            neighborLoop.y = Util.loop(neighborLoop.y, tiles);
            neighborLoop.z = Util.loop(neighborLoop.z, tiles);
            random.setSeed(Objects.hash(seed, neighborLoop));
            //Random must be 'warmed up' or its first float will always be the same.
            random.nextLong();
            random.nextLong();
            Vec3 point = new Vec3(random.nextFloat(), random.nextFloat(), random.nextFloat());
            Vec3 diff = new Vec3(neighbor).add(point).sub(fCoord);
            float dist = diff.length();
            minDist = Math.min(minDist, dist);
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
}
