package com.samrj.devil.model;

import com.samrj.devil.model.DataBlock.Type;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import org.blender.dna.bAction;
import org.blender.dna.bArmature;
import org.blender.utils.MainLib;
import org.cakelab.blender.io.BlenderFile;

/**
 * .DVM file loader. Corresponds with the Blender python exporter.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Model
{
    private final EnumMap<DataBlock.Type, ArrayMap<?>> arraymaps = new EnumMap<>(DataBlock.Type.class);
    
    public final String path;
    
    public final ArrayMap<Library> libraries;
    public final ArrayMap<Action> actions;
    public final ArrayMap<Armature> armatures;
    public final ArrayMap<Curve> curves;
    public final ArrayMap<Lamp> lamps;
    public final ArrayMap<Material> materials;
    public final ArrayMap<Mesh> meshes;
    public final ArrayMap<ModelObject> objects;
    public final ArrayMap<Scene> scenes;
    public final ArrayMap<Texture> textures;
    
    private boolean destroyed;
    
    public Model(String path) throws IOException
    {
        this.path = path;
        
        try (BlenderFile file = new BlenderFile(new File(path)))
        {
            boolean isCompatible = MainLib.doCompatibilityCheck(file.readFileGlobal());
            if (!isCompatible) throw new java.lang.IllegalArgumentException("Incompatible .blend file");
                
            MainLib lib = new MainLib(file);
            
            libraries = new ArrayMap<>();
            arraymaps.put(Type.LIBRARY, libraries);
            
            actions = new ArrayMap<>();
            for (bAction bAction : Blender.blendList(lib.getBAction()))
            {
                Action action = new Action(this, bAction);
                actions.put(action.name, action);
            }
            arraymaps.put(Type.ACTION, actions);
            
            armatures = new ArrayMap<>();
            for (bArmature bArm : Blender.blendList(lib.getBArmature()))
            {
                Armature armature = new Armature(this, bArm);
                armatures.put(armature.name, armature);
            }
            arraymaps.put(Type.ARMATURE, armatures);
            
            curves = new ArrayMap<>();
            for (org.blender.dna.Curve bCurve : Blender.blendList(lib.getCurve()))
            {
                Curve curve = new Curve(this, bCurve);
                curves.put(curve.name, curve);
            }
            arraymaps.put(Type.CURVE, curves);
            
            lamps = new ArrayMap<>();
            arraymaps.put(Type.LAMP, lamps);
            
            materials = new ArrayMap<>();
            arraymaps.put(Type.MATERIAL, materials);
            
            meshes = new ArrayMap<>();
            arraymaps.put(Type.MESH, meshes);
            
            objects = new ArrayMap<>();
            arraymaps.put(Type.OBJECT, objects);
            
            scenes = new ArrayMap<>();
            arraymaps.put(Type.SCENE, scenes);
            
            textures = new ArrayMap<>();
            arraymaps.put(Type.TEXTURE, textures);
        }
        catch (IOException e)
        {
            throw new IOException("in " + path, e);
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("in " + path, e);
        }
    }
    
    public <T extends DataBlock> ArrayMap<T> get(DataBlock.Type dataType)
    {
        return (ArrayMap<T>)arraymaps.get(dataType);
    }
    
    /**
     * Releases any system resources (native memory) associated with this model.
     */
    public void destroy()
    {
        if (destroyed) throw new IllegalStateException("Already destroyed.");
        arraymaps.forEach((t, m) -> m.destroy());
        destroyed = true;
    }
}
