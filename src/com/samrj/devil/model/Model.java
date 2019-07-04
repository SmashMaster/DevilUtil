package com.samrj.devil.model;

import com.samrj.devil.model.DataBlock.Type;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.blender.dna.Tex;
import org.blender.dna.bAction;
import org.blender.dna.bArmature;
import org.blender.utils.MainLib;
import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.io.dna.DNAStruct;
import org.cakelab.blender.nio.CPointer;

/**
 * .DVM file loader. Corresponds with the Blender python exporter.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Model
{
    private static final boolean DEBUG = true;
    
    private final EnumMap<DataBlock.Type, ArrayMap<?>> arraymaps = new EnumMap<>(DataBlock.Type.class);
    
    final Map<Long, DebugBlock> debugMap;
    
    String debugTypeOf(CPointer ptr) throws IOException
    {
        if (ptr == null) return null;
        DebugBlock block = debugMap.get(ptr.getAddress());
        if (block == null) return null;
        return block.struct.getType().getName();
    }
    
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
        
        long t0 = System.nanoTime();
        
        try (BlenderFile file = new BlenderFile(new File(path), true))
        {
            boolean isCompatible = MainLib.doCompatibilityCheck(file.readFileGlobal());
            if (!isCompatible) throw new java.lang.IllegalArgumentException("Incompatible .blend file");
            
            if (DEBUG)
            {
                debugMap = new HashMap<>();
                DNAModel model = file.getBlenderModel();

                for (Block block : file.getBlocks())
                {
                    DNAStruct struct = model.getStruct(block.header.getSdnaIndex());
                    debugMap.put(block.header.getAddress(), new DebugBlock(block, struct));
                }
            }
            else debugMap = null;
            
            MainLib library = new MainLib(file);
            
            libraries = new ArrayMap<>();
            for (org.blender.dna.Library bLib : Blender.list(library.getLibrary()))
                libraries.put(new Library(this, bLib));
            arraymaps.put(Type.LIBRARY, libraries);
            
            actions = new ArrayMap<>();
            for (bAction bAction : Blender.list(library.getBAction()))
                actions.put(new Action(this, bAction));
            arraymaps.put(Type.ACTION, actions);
            
            armatures = new ArrayMap<>();
            for (bArmature bArm : Blender.list(library.getBArmature()))
                armatures.put(new Armature(this, bArm));
            arraymaps.put(Type.ARMATURE, armatures);
            
            curves = new ArrayMap<>();
            for (org.blender.dna.Curve bCurve : Blender.list(library.getCurve()))
                curves.put(new Curve(this, bCurve));
            arraymaps.put(Type.CURVE, curves);
            
            lamps = new ArrayMap<>();
            for (org.blender.dna.Lamp bLamp : Blender.list(library.getLamp()))
                lamps.put(new Lamp(this, bLamp));
            arraymaps.put(Type.LAMP, lamps);
            
            materials = new ArrayMap<>();
            int i = 0;
            for (org.blender.dna.Material bMat : Blender.list(library.getMaterial()))
                materials.put(new Material(this, i++, bMat));
            arraymaps.put(Type.MATERIAL, materials);
            
            meshes = new ArrayMap<>();
            for (org.blender.dna.Mesh bMesh : Blender.list(library.getMesh()))
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
