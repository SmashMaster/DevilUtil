package com.samrj.devil.model;

import com.samrj.devil.model.DataBlock.Type;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;

/**
 * Loads and parses Blender .blend files.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Model
{
    private final EnumMap<DataBlock.Type, ArrayMap<?>> arraymaps = new EnumMap<>(DataBlock.Type.class);
    
    public final Path path;
    
    public final ArrayMap<Library> libraries;
    public final ArrayMap<Action> actions;
    public final ArrayMap<Armature> armatures;
    public final ArrayMap<Curve> curves;
    public final ArrayMap<Lamp> lamps;
    public final ArrayMap<Material> materials;
    public final ArrayMap<Mesh> meshes;
    public final ArrayMap<ModelObject> objects;
    public final ArrayMap<ModelCollection> collections;
    public final ArrayMap<Scene> scenes;
    public final ArrayMap<Image> images;
    
    private boolean destroyed;
    
    public Model(Path path) throws IOException
    {
        this.path = path;
        
        try
        {
            BlendFile blend = new BlendFile(path.toFile());
            
            libraries = new ArrayMap<>();
            for (BlendFile.Pointer bLib : blend.getLibrary("Library"))
                libraries.put(new Library(this, bLib));
            arraymaps.put(Type.LIBRARY, libraries);
            
            actions = new ArrayMap<>();
           for (BlendFile.Pointer bAction : blend.getLibrary("bAction"))
                actions.put(new Action(this, bAction));
            arraymaps.put(Type.ACTION, actions);
            
            armatures = new ArrayMap<>();
            for (BlendFile.Pointer bArm : blend.getLibrary("bArmature"))
                armatures.put(new Armature(this, bArm));
            arraymaps.put(Type.ARMATURE, armatures);
            
            curves = new ArrayMap<>();
            for (BlendFile.Pointer bCurve : blend.getLibrary("Curve"))
                curves.put(new Curve(this, bCurve));
            arraymaps.put(Type.CURVE, curves);
            
            lamps = new ArrayMap<>();
            for (BlendFile.Pointer bLamp : blend.getLibrary("Lamp"))
                lamps.put(new Lamp(this, bLamp));
            arraymaps.put(Type.LAMP, lamps);
            
            materials = new ArrayMap<>();
            int i = 0;
            for (BlendFile.Pointer bMat : blend.getLibrary("Material"))
                materials.put(new Material(this, i++, bMat));
            arraymaps.put(Type.MATERIAL, materials);
            
            meshes = new ArrayMap<>();
            for (BlendFile.Pointer bMesh : blend.getLibrary("Mesh"))
                meshes.put(new Mesh(this, bMesh));
            arraymaps.put(Type.MESH, meshes);
            
            objects = new ArrayMap<>();
            for (BlendFile.Pointer bObject : blend.getLibrary("Object"))
                objects.put(new ModelObject(this, bObject));
            arraymaps.put(Type.OBJECT, objects);

            collections = new ArrayMap<>();
            for (BlendFile.Pointer bObject : blend.getLibrary("Collection"))
                collections.put(new ModelCollection(this, bObject));
            arraymaps.put(Type.COLLECTION, collections);

            scenes = new ArrayMap<>();
            for (BlendFile.Pointer bScene : blend.getLibrary("Scene"))
                scenes.put(new Scene(this, bScene));
            arraymaps.put(Type.SCENE, scenes);

            images = new ArrayMap<>();
            for (BlendFile.Pointer bImage : blend.getLibrary("Image"))
                images.put(new Image(this, bImage));
            arraymaps.put(Type.IMAGE, images);

            blend.destroy();
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

    public Model(String path) throws IOException
    {
        this(Path.of(path));
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
