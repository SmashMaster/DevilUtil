/*
 * Copyright (c) 2015 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.model.Armature.Bone;
import com.samrj.devil.model.Pose.PoseBone;
import com.samrj.devil.model.constraint.IKConstraint;
import com.samrj.devil.util.IdentitySet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for solving armature poses with constraints.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class ArmatureSolver
{
    private final BoneSolver[] bones;
    private final Map<String, BoneSolver> nameMap;
    
    private final List<Constraint> constraints;
    private final IKConstraint[] ikConstraints;
    private final IdentitySet<BoneSolver> independent;
    private List<Constraint> solveOrder;
    
    /**
     * Creates a new armature solver from the given armature object.
     * 
     * @param object A mesh object.
     */
    public ArmatureSolver(ModelObject<Armature> object)
    {
        Armature armature = object.data.get();
        
        bones = new BoneSolver[armature.bones.length];
        for (int i=0; i<bones.length; i++) bones[i] = new BoneSolver(armature.bones[i]);
        for (BoneSolver bone : bones) bone.populate();
        nameMap = new HashMap<>(bones.length);
        for (BoneSolver bone : bones) nameMap.put(bone.bone.name, bone);
        
        ikConstraints = new IKConstraint[object.ikConstraints.length];
        for (int i=0; i<ikConstraints.length; i++)
            ikConstraints[i] = new IKConstraint(object.ikConstraints[i], this);
        constraints = new LinkedList<>();
        independent = new IdentitySet<>();
        sortSolvables();
    }
    
    public int getNumBones()
    {
        return bones.length;
    }
    
    /**
     * Returns the bone solver with the given name.
     */
    public BoneSolver getBone(String name)
    {
        return nameMap.get(name);
    }
    
    /**
     * Adds the given constraint to this solver.
     * 
     * @param c A constraint.
     */
    public void addConstraint(Constraint c)
    {
        solveOrder = null;
        constraints.add(c);
    }
    
    /**
     * Removes any constraints from this solver which were added after creation.
     * Does not remove the original IK constraints.
     */
    public void clearConstraints()
    {
        solveOrder = null;
        constraints.clear();
    }
    
    /**
     * Generates a solve order by performing a topological sort on the set of
     * bones and constraints belonging to this solver.
     */
    public void sortSolvables()
    {
        independent.clear();
        independent.addAll(Arrays.asList(bones));
        for (IKConstraint ik : ikConstraints) ik.removeSolved(independent);
        for (Constraint s : constraints) s.removeSolved(independent);
        
        DAG<Constraint> solveGraph = new DAG<>();
        for (BoneSolver bone : bones) bone.populateSolveGraph(solveGraph);
        for (IKConstraint ik : ikConstraints) ik.populateSolveGraph(solveGraph);
        for (Constraint s : constraints) s.populateSolveGraph(solveGraph);
        solveOrder = solveGraph.sort();
    }
    
    /**
     * Sets the pose of this solver.
     * 
     * @param pose The pose to use.
     */
    public void setPose(Pose pose)
    {
        for (PoseBone bone : pose.getBones())
            nameMap.get(bone.name).poseTransform.set(bone.transform);
    }
    
    /**
     * Solves each bone matrix in this solver, taking all constraints into
     * account.
     */
    public void solve()
    {
        if (solveOrder == null) throw new IllegalStateException("Unsorted. Call sortSolvables() first.");
        
        for (BoneSolver bone : independent)
        {
            bone.finalTransform.set(bone.poseTransform);
            bone.finalTransform.rotation.normalize();
        }
        
        for (Constraint s : solveOrder) s.solve();
    }
    
    /**
     * Interface for bone constraints.
     */
    public interface Constraint
    {
        public void populateSolveGraph(DAG<Constraint> graph);
        public default void removeSolved(Set<BoneSolver> independent) {}
        public void solve();
    }
    
    public class BoneSolver implements Constraint
    {
        public final Bone bone;
        
        public final Transform poseTransform, finalTransform;
        public final Mat4 skinMatrix; //object rest position -> object pose position
        public final Mat3 rotMatrix; //object rest direction -> object pose direction
        public final Mat3 invRotMat; //object pose direction -> object rest direction
        
        private BoneSolver parent;
        
        private BoneSolver(Bone bone)
        {
            this.bone = bone;
            poseTransform = new Transform();
            finalTransform = new Transform();
            skinMatrix = new Mat4();
            rotMatrix = new Mat3();
            invRotMat = new Mat3();
        }
        
        private void populate()
        {
            if (bone.parentIndex >= 0) parent = bones[bone.parentIndex];
        }
        
        public BoneSolver getParent()
        {
            return parent;
        }
        
        public Mat4 getModelMatrix()
        {
            Mat4 out = new Mat4(skinMatrix);
            out.mult(bone.tail);
            out.mult(new Mat4(bone.matrix));
            return out;
        }
        
        public Vec3 getHeadPos()
        {
            Vec3 out = new Vec3(finalTransform.position);
            out.mult(bone.matrix);
            out.add(bone.head);
            if (parent != null) out.mult(parent.skinMatrix);
            return out;
        }
        
        @Override
        public void populateSolveGraph(DAG<Constraint> graph)
        {
            if (parent != null) graph.addEdge(parent, this);
            else graph.add(this);
        }

        @Override
        public void solve()
        {
            skinMatrix.setIdentity();
            if (parent != null) skinMatrix.mult(parent.skinMatrix);
            skinMatrix.translate(bone.head);
            if (parent != null && !bone.inheritRotation) skinMatrix.mult(new Mat4(parent.invRotMat));
            skinMatrix.mult(new Mat4(bone.matrix));
            finalTransform.apply(skinMatrix);
            skinMatrix.mult(new Mat4(bone.invMat));
            skinMatrix.translate(Vec3.negate(bone.head));

            rotMatrix.setIdentity();
            if (parent != null && bone.inheritRotation) rotMatrix.mult(parent.rotMatrix);
            rotMatrix.mult(bone.matrix);
            finalTransform.apply(rotMatrix);
            rotMatrix.mult(bone.invMat);

            if (!finalTransform.scale.isZero(0.0f)) Mat3.invert(rotMatrix, invRotMat);
        }
    }
}
