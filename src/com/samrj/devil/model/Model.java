package com.samrj.devil.model;

import com.samrj.devil.model.DataBlock.Type;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import org.blender.dna.Tex;
import org.blender.utils.MainLib;
import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.dna.DNAStruct;

/**
 * Loads and parses Blender .blend files.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
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
    public final ArrayMap<Scene> scenes;
    public final ArrayMap<ModelObject> objects;
    public final ArrayMap<Texture> textures;
    
    private boolean destroyed;
    
    public Model(String path) throws IOException
    {
        this.path = path;
        
        try
        {
            BlenderFile file = new BlenderFile(new File(path));
            BlendFile fastFile = new BlendFile(new File(path));
            MainLib library = new MainLib(file);
            
            libraries = new ArrayMap<>();
            for (BlendFile.Pointer bLib : fastFile.getLibrary("Library"))
                libraries.put(new Library(this, bLib));
            arraymaps.put(Type.LIBRARY, libraries);
            
            actions = new ArrayMap<>();
           for (BlendFile.Pointer bAction : fastFile.getLibrary("bAction"))
                actions.put(new Action(this, bAction));
            arraymaps.put(Type.ACTION, actions);
            
            armatures = new ArrayMap<>();
            for (BlendFile.Pointer bArm : fastFile.getLibrary("bArmature"))
                armatures.put(new Armature(this, bArm));
            arraymaps.put(Type.ARMATURE, armatures);
            
            curves = new ArrayMap<>();
            for (BlendFile.Pointer bCurve : fastFile.getLibrary("Curve"))
                curves.put(new Curve(this, bCurve));
            arraymaps.put(Type.CURVE, curves);
            
            lamps = new ArrayMap<>();
            for (BlendFile.Pointer bLamp : fastFile.getLibrary("Lamp"))
                lamps.put(new Lamp(this, bLamp));
            arraymaps.put(Type.LAMP, lamps);
            
            materials = new ArrayMap<>();
            int i = 0;
            for (BlendFile.Pointer bMat : fastFile.getLibrary("Material"))
                materials.put(new Material(this, i++, bMat));
            arraymaps.put(Type.MATERIAL, materials);
            
            meshes = new ArrayMap<>();
            for (BlendFile.Pointer bMesh : fastFile.getLibrary("Mesh"))
                meshes.put(new Mesh(this, bMesh));
            arraymaps.put(Type.MESH, meshes);
            
            scenes = new ArrayMap<>();
            for (org.blender.dna.Scene bScene : Blender.list(library.getScene()))
                scenes.put(new Scene(this, bScene));
            arraymaps.put(Type.SCENE, scenes);
            
            objects = new ArrayMap<>();
            for (Scene scene : scenes)
                for (ModelObject object : scene.objects)
                    objects.put(object);
            arraymaps.put(Type.OBJECT, objects);
            
            textures = new ArrayMap<>();
            for (Tex bTex : Blender.list(library.getTex()))
                textures.put(new Texture(this, bTex));
            arraymaps.put(Type.TEXTURE, textures);
            
            fastFile.destroy();
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
    
    static class DebugBlock
    {
        final Block block;
        final DNAStruct struct;
        
        private DebugBlock(Block block, DNAStruct struct)
        {
            this.block = block; this.struct = struct;
        }
    }
}
