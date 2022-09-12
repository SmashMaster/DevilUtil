package com.samrj.devil.phys;

import com.samrj.devil.geo3d.Triangle3;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;

import java.util.Collection;

/**
 * Can be used to calculate the mass, center of mass, and moment of inertia of
 * meshes.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class RigidBodyProperties
{
    /**
     * Calculates the physical properties of the given mesh. This method will be
     * less accurate if the mesh is not closed (if there are any holes leading
     * into the 'solid' volume of the mesh).
     */
    public static RigidBodyProperties ofMesh(Collection<Triangle3> mesh, double density)
    {
        double volume = 0.0;
        double comX = 0.0, comY = 0.0, comZ = 0.0;
        double moiXX = 0.0, moiYY = 0.0, moiZZ = 0.0;
        double moiXY = 0.0, moiYZ = 0.0, moiXZ = 0.0;
        
        for (Triangle3 face : mesh)
        {
            //Consider a tetrahedron sharing 3 vertices with the triangle, and one at the origin.
            //We can simply sum most of the properties of each of these tetrahedrons.
            
            double x1 = face.a().x, y1 = face.a().y, z1 = face.a().z;
            double x2 = face.b().x, y2 = face.b().y, z2 = face.b().z;
            double x3 = face.c().x, y3 = face.c().y, z3 = face.c().z;
            
            double x1y2 = x1*y2;
            double x3y1 = x3*y1;
            double x2y3 = x2*y3;
            double x3y2 = x3*y2;
            double x1y3 = x1*y3;
            double x2y1 = x2*y1;
            double determinant = x1y2*z3 + x3y1*z2 + x2y3*z1 - x3y2*z1 - x1y3*z2 - x2y1*z3;
            
            double moiXTemp = x1*x1 + x1*x2 + x2*x2 + x1*x3 + x2*x3 + x3*x3;
            double moiYTemp = y1*y1 + y1*y2 + y2*y2 + y1*y3 + y2*y3 + y3*y3;
            double moiZTemp = z1*z1 + z1*z2 + z2*z2 + z1*z3 + z2*z3 + z3*z3;
            
            volume += determinant;
            comX += determinant*(x1 + x2 + x3);
            comY += determinant*(y1 + y2 + y3);
            comZ += determinant*(z1 + z2 + z3);
            moiXX += determinant*(moiYTemp + moiZTemp);
            moiYY += determinant*(moiXTemp + moiZTemp);
            moiZZ += determinant*(moiXTemp + moiYTemp);
            moiXY -= determinant*(2.0*x1*y1 +  x2y1 +  x3y1 +  x1y2 + 2.0*x2*y2 +  x3y2 +  x1y3 +  x2y3 + 2.0*x3*y3);
            moiYZ -= determinant*(2.0*y1*z1 + y2*z1 + y3*z1 + y1*z2 + 2.0*y2*z2 + y3*z2 + y1*z3 + y2*z3 + 2.0*y3*z3);
            moiXZ -= determinant*(2.0*x1*z1 + x2*z1 + x3*z1 + x1*z2 + 2.0*x2*z2 + x3*z2 + x1*z3 + x2*z3 + 2.0*x3*z3);
        }
        
        if (volume != 0.0)
        {
            double volumeMult4 = volume*4.0;
            comX /= volumeMult4;
            comY /= volumeMult4;
            comZ /= volumeMult4;
            volume /= 6.0;
        }
        double densityDiv60 = density/60.0;
        moiXX *= densityDiv60;
        moiYY *= densityDiv60;
        moiZZ *= densityDiv60;
        double densityDiv120 = density/120.0f;
        moiXY *= densityDiv120;
        moiYZ *= densityDiv120;
        moiXZ *= densityDiv120;
        
        //As the last step, we need to apply the parallel axis theorem to move the moment of inertia to the center of mass.
        double mass = density*volume;
        double comXX = comX*comX;
        double comYY = comY*comY;
        double comZZ = comZ*comZ;
        double actualMoiXX = moiXX - mass*(comYY + comZZ);
        double actualMoiYY = moiYY - mass*(comXX + comZZ);
        double actualMoiZZ = moiZZ - mass*(comXX + comYY);
        double actualMoiXY = moiXY + mass*comX*comY;
        double actualMoiYZ = moiYZ + mass*comY*comZ;
        double actualMoiXZ = moiXZ + mass*comX*comZ;
        
        return new RigidBodyProperties(mass, comX, comY, comZ, actualMoiXX, actualMoiYY, actualMoiZZ, actualMoiXY, actualMoiYZ, actualMoiXZ);
    }
    
    /**
     * Calculates the sum of a number of given physical properties.
     */
    public static RigidBodyProperties sum(Collection<RigidBodyProperties> propCollection)
    {
        double mass = 0.0;
        double comX = 0.0, comY = 0.0, comZ = 0.0;
        double moiXX = 0.0, moiYY = 0.0, moiZZ = 0.0;
        double moiXY = 0.0, moiYZ = 0.0, moiXZ = 0.0;
        
        for (RigidBodyProperties props : propCollection)
        {
            mass += props.mass;
            comX += props.mass*props.comX;
            comY += props.mass*props.comY;
            comZ += props.mass*props.comZ;
        }
        
        comX /= mass;
        comY /= mass;
        comZ /= mass;
        
        for (RigidBodyProperties props : propCollection)
        {
            double dx = comX - props.comX;
            double dy = comY - props.comY;
            double dz = comZ - props.comZ;
            
            double dxsq = dx*dx;
            double dysq = dy*dy;
            double dzsq = dz*dz;
            
            moiXX += props.moiXX + props.mass*(dysq + dzsq);
            moiYY += props.moiYY + props.mass*(dxsq + dzsq);
            moiZZ += props.moiZZ + props.mass*(dxsq + dysq);
            moiXY += props.moiXY - props.mass*dx*dy;
            moiYZ += props.moiYZ - props.mass*dy*dz;
            moiXZ += props.moiXZ - props.mass*dx*dz;
        }
        
        return new RigidBodyProperties(mass, comX, comY, comZ, moiXX, moiYY, moiZZ, moiXY, moiYZ, moiXZ);
    }
    
    /**
     * Returns the properties of an object with zero mass.
     */
    public static RigidBodyProperties zero()
    {
        return new RigidBodyProperties(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
    
    public final double mass;
    public final double comX, comY, comZ;
    public final double moiXX, moiYY, moiZZ;
    public final double moiXY, moiYZ, moiXZ;
    
    private RigidBodyProperties(double mass, double comX, double comY, double comZ,
            double moiXX, double moiYY, double moiZZ, double moiXY, double moiYZ, double moiXZ)
    {
        this.mass = mass;
        this.comX = comX;
        this.comY = comY;
        this.comZ = comZ;
        this.moiXX = moiXX;
        this.moiYY = moiYY;
        this.moiZZ = moiZZ;
        this.moiXY = moiXY;
        this.moiYZ = moiYZ;
        this.moiXZ = moiXZ;
    }
    
    public Vec3 getCenterOfMass()
    {
        return new Vec3((float)comX, (float)comY, (float)comZ);
    }
    
    public Mat3 getMomentOfInertia()
    {
        return new Mat3((float)moiXX, (float)moiXY, (float)moiXZ, 
                        (float)moiXY, (float)moiYY, (float)moiYZ,
                        (float)moiXZ, (float)moiYZ, (float)moiZZ);
    }
}
