package com.samrj.devil.graphics.model;

import com.samrj.devil.graphics.model.Mesh.Geometry;
import com.samrj.devil.graphics.model.Mesh.Triangle;
import com.samrj.devil.math.Vec3;
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
    private int numTris;
    private Vec3[] vertices;
    private Triangle[] triangles;
    private Vec3[] centroids;
    
    public PartitionTree(Mesh mesh)
    {
        Geometry geom = mesh.getGeometry();
        vertices = geom.vertices;
        triangles = geom.triangles;
        numTris = triangles.length;
        centroids = new Vec3[numTris];
        for (int i=0; i<numTris; i++) centroids[i] = triangles[i].getCentroid();
        
        root = new Node();
    }
    
    //If i'm going to half-ass this, why do it at all?
    
    private class Node
    {
        private float minX, maxX;
        private float minY, maxY;
        private float minZ, maxZ;
        private Node left, right;
        
        private Node()
        {
            minX = Float.POSITIVE_INFINITY; maxX = Float.NEGATIVE_INFINITY;
            minY = Float.POSITIVE_INFINITY; maxY = Float.NEGATIVE_INFINITY;
            minZ = Float.POSITIVE_INFINITY; maxZ = Float.NEGATIVE_INFINITY;
        }
        
        private Node(ArrayList<Triangle> parentTriangles)
        {
        }
    }
}
