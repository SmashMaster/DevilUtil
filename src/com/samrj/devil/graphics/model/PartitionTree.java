package com.samrj.devil.graphics.model;

import com.samrj.devil.geo2d.DelaunayTriangulation.Triangle;
import com.samrj.devil.graphics.model.Mesh.Geometry;
import java.util.ArrayList;

/**
 * Spacial partition AABB tree. Implemented kind of like a k-d tree.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class PartitionTree
{
    private Node root;
    
    public PartitionTree(Mesh mesh)
    {
        Geometry geom = mesh.getGeometry();
        
        
    }
    
    private class Node
    {
        private float minX, maxX;
        private float minY, maxY;
        private float minZ, maxZ;
        private Node left, right;
        
        private Node(ArrayList<Triangle> parentTriangles)
        {
            minX = Float.POSITIVE_INFINITY; maxX = Float.NEGATIVE_INFINITY;
            minY = Float.POSITIVE_INFINITY; maxY = Float.NEGATIVE_INFINITY;
            minZ = Float.POSITIVE_INFINITY; maxZ = Float.NEGATIVE_INFINITY;
        }
    }
}
