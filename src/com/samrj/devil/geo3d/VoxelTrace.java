package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec3i;

/**
 * Incrementally traces through a 3D unit voxel grid, and can be polled for its
 * current position as it goes. Does not handle hit detection against voxels.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2021 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class VoxelTrace
{
    /**
     * The 6 sides of a voxel.
     */
    public enum Side
    {
        X0, X1, Y0, Y1, Z0, Z1;
        
        /**
         * Returns which side of a voxel the given direction would hit if it
         * started in the middle of the voxel.
         * 
         * @param dir A 3D vector.
         * @return The side corresponding to the given vector.
         */
        public static Side fromDir(Vec3 dir)
        {
            float max = Math.abs(dir.x);
            Side side = dir.x > 0.0f ? X1 : X0;
            
            float maxY = Math.abs(dir.y);
            if (maxY > max)
            {
                max = maxY;
                side = dir.y > 0.0f ? Y1 : Y0;
            }
            
            float maxZ = Math.abs(dir.z);
            if (maxZ > max) side = dir.z > 0.0f ? Z1 : Z0;
            
            return side;
        }
    }
    
    /**
     * True if the trace hit the bounds of the voxel grid.
     */
    public final boolean hit;
    
    private final Box3i bounds;
    private final Vec3 ray, dir;
    private final boolean startedOutside;
    private final Side startSide;
    private final Vec3i voxel = new Vec3i();
    private Side side;
    private int stepX, stepY, stepZ;
    private float tMaxX, tMaxY, tMaxZ;
    private float tDX, tDY, tDZ;
    
    /**
     * Sets up a VoxelTrace with the given parameters.
     * 
     * @param bounds The bounding volume to trace in.
     * @param ray The starting position of the ray.
     * @param dir The direction of the ray.
     */
    public VoxelTrace(Box3i bounds, Vec3 ray, Vec3 dir)
    {
        this.bounds = new Box3i(bounds);
        this.ray = new Vec3(ray);
        this.dir = new Vec3(dir);
        
        Vec3 start, end;
        
        //Trace against the whole volume, determine if we hit and the points of
        //intersection.
        {
            float tx0 = (bounds.min.x - ray.x)/dir.x;
            float tx1 = (bounds.max.x - ray.x)/dir.x;
            float ty0 = (bounds.min.y - ray.y)/dir.y;
            float ty1 = (bounds.max.y - ray.y)/dir.y;
            float tz0 = (bounds.min.z - ray.z)/dir.z;
            float tz1 = (bounds.max.z - ray.z)/dir.z;
            
            if (Float.isNaN(tx0)) tx0 = Float.NEGATIVE_INFINITY;
            if (Float.isNaN(tx1)) tx1 = Float.POSITIVE_INFINITY;
            if (Float.isNaN(ty0)) ty0 = Float.NEGATIVE_INFINITY;
            if (Float.isNaN(ty1)) ty1 = Float.POSITIVE_INFINITY;
            if (Float.isNaN(tz0)) tz0 = Float.NEGATIVE_INFINITY;
            if (Float.isNaN(tz1)) tz1 = Float.POSITIVE_INFINITY;
            
            float tMinX, tMinY, tMinZ; //Unrelated to the similarly named variables above.
            float tMaxX, tMaxY, tMaxZ;
            Side tMinSideX, tMinSideY, tMinSideZ;

            if (tx0 < tx1)
            {
                tMinX = tx0;
                tMinSideX = Side.X0;
                tMaxX = tx1;
            }
            else
            {
                tMinX = tx1;
                tMinSideX = Side.X1;
                tMaxX = tx0;
            }

            if (ty0 < ty1)
            {
                tMinY = ty0;
                tMinSideY = Side.Y0;
                tMaxY = ty1;
            }
            else
            {
                tMinY = ty1;
                tMinSideY = Side.Y1;
                tMaxY = ty0;
            }

            if (tz0 < tz1)
            {
                tMinZ = tz0;
                tMinSideZ = Side.Z0;
                tMaxZ = tz1;
            }
            else
            {
                tMinZ = tz1;
                tMinSideZ = Side.Z1;
                tMaxZ = tz0;
            }

            float tMin = tMinX;
            Side startSideLocal = tMinSideX;

            if (tMinY > tMin)
            {
                tMin = tMinY;
                startSideLocal = tMinSideY;
            }

            if (tMinZ > tMin)
            {
                tMin = tMinZ;
                startSideLocal = tMinSideZ;
            }

            float tMax = tMaxX;
            if (tMaxY < tMax) tMax = tMaxY;
            if (tMaxZ < tMax) tMax = tMaxZ;
            
            hit = tMax >= tMin && tMax >= 0.0f;
            startedOutside = tMin >= 0.0f;
            startSide = startSideLocal;
            start = startedOutside ? Vec3.madd(ray, dir, tMin) : new Vec3(ray);
            end = Vec3.madd(ray, dir, tMax);
        }
        
        //Set up tracing within the grid itself.
        if (hit)
        {
            voxel.set((int)Math.floor(start.x),
                      (int)Math.floor(start.y),
                      (int)Math.floor(start.z));
            
            if (voxel.x < bounds.min.x) voxel.x = bounds.min.x;
            if (voxel.y < bounds.min.y) voxel.y = bounds.min.y;
            if (voxel.z < bounds.min.z) voxel.z = bounds.min.z;
            if (voxel.x >= bounds.max.x) voxel.x = bounds.max.x - 1;
            if (voxel.y >= bounds.max.y) voxel.y = bounds.max.y - 1;
            if (voxel.z >= bounds.max.z) voxel.z = bounds.max.z - 1;
            
            Vec3i endPos = new Vec3i((int)Math.floor(end.x),
                                     (int)Math.floor(end.y),
                                     (int)Math.floor(end.z));
            
            if (endPos.x < bounds.min.x) endPos.x = bounds.min.x;
            if (endPos.y < bounds.min.y) endPos.y = bounds.min.y;
            if (endPos.z < bounds.min.z) endPos.z = bounds.min.z;
            if (endPos.x >= bounds.max.x) endPos.x = bounds.max.x - 1;
            if (endPos.y >= bounds.max.y) endPos.y = bounds.max.y - 1;
            if (endPos.z >= bounds.max.z) endPos.z = bounds.max.z - 1;
            
            side = startSide;
            
            stepX = dir.x >= 0 ? 1 : -1;
            stepY = dir.y >= 0 ? 1 : -1;
            stepZ = dir.z >= 0 ? 1 : -1;
            
            float nextX = (stepX >= 0) ? (voxel.x + 1) - start.x : voxel.x - start.x;
            float nextY = (stepY >= 0) ? (voxel.y + 1) - start.y : voxel.y - start.y;
            float nextZ = (stepZ >= 0) ? (voxel.z + 1) - start.z : voxel.z - start.z;
            
            tMaxX = (dir.x != 0.0f) ? nextX/dir.x : Float.POSITIVE_INFINITY;
            tMaxY = (dir.y != 0.0f) ? nextY/dir.y : Float.POSITIVE_INFINITY;
            tMaxZ = (dir.z != 0.0f) ? nextZ/dir.z : Float.POSITIVE_INFINITY;
        
            tDX = stepX/dir.x;
            tDY = stepY/dir.y;
            tDZ = stepZ/dir.z;
        }
    }
    
    /**
     * Returns true if the trace has another voxel.
     */
    public boolean hasNext()
    {
        if (!hit) return false;
        if (voxel.x < bounds.min.x) return false;
        if (voxel.y < bounds.min.y) return false;
        if (voxel.z < bounds.min.z) return false;
        if (voxel.x >= bounds.max.x) return false;
        if (voxel.y >= bounds.max.y) return false;
        if (voxel.z >= bounds.max.z) return false;
        return true;
    }
    
    /**
     * Returns T such that ray + dir*T equals the current trace position.
     */
    public float time()
    {
        switch (side)
        {
            case X0: return (voxel.x - ray.x)/dir.x;
            case X1: return (voxel.x - ray.x + 1)/dir.x;
            case Y0: return (voxel.y - ray.y)/dir.y;
            case Y1: return (voxel.y - ray.y + 1)/dir.y;
            case Z0: return (voxel.z - ray.z)/dir.z;
            case Z1: return (voxel.z - ray.z + 1)/dir.z;
            default: throw new IllegalStateException();
        }
    }
    
    /**
     * Returns which voxel face was hit at the current position.
     */
    public Side side()
    {
        return side;
    }
    
    /**
     * Steps the trace forward to the next voxel it will hit. The first time
     * this is called will return the first voxel the trace hit.
     */
    public Vec3i next()
    {
        Vec3i result = new Vec3i(voxel);
        
        if (tMaxX < tMaxY)
        {
            if (tMaxX < tMaxZ)
            {
                tMaxX += tDX;
                voxel.x += stepX;
                side = stepX > 0 ? Side.X0 : Side.X1;
            }
            else
            {
                tMaxZ += tDZ;
                voxel.z += stepZ;
                side = stepZ > 0 ? Side.Z0 : Side.Z1;
            }
        }
        else
        {
            if (tMaxY < tMaxZ)
            {
                tMaxY += tDY;
                voxel.y += stepY;
                side = stepY > 0 ? Side.Y0 : Side.Y1;
            }
            else
            {
                tMaxZ += tDZ;
                voxel.z += stepZ;
                side = stepZ > 0 ? Side.Z0 : Side.Z1;
            }
        }
        
        return result;
    }
}
