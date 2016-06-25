package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.res.Resource;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * .DVM file loader. Corresponds with the Blender python exporter.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Model
{
    private static final byte[] MAGIC = IOUtil.hexToBytes("9F0A446576696C4D6F64656C");
    
    public final int versionMajor, versionMinor;
    public final ArrayMap<Library> libraries;
    public final ArrayMap<Action> actions;
    public final ArrayMap<Armature> armatures;
    public final ArrayMap<Curve> curves;
    public final ArrayMap<Lamp> lamps;
    public final ArrayMap<Material> materials;
    public final ArrayMap<Mesh> meshes;
    public final ArrayMap<ModelObject> objects;
    public final ArrayMap<Scene> scenes;
    
    private boolean destroyed;
    
    public Model(String path) throws IOException
    {
        try  (BufferedInputStream inputStream = new BufferedInputStream(Resource.open(path)))
        {
            DataInputStream in = new DataInputStream(inputStream);
            
            byte[] header = new byte[12];
            in.read(header);
            if (!Arrays.equals(header, MAGIC))
                throw new IOException("Illegal file format specified.");
            versionMajor = in.readShort();
            versionMinor = in.readShort();
            if (versionMajor != 0 || versionMinor != 20)
                throw new IOException("Unable to load DVM version " + versionMajor + "." + versionMinor);
            
            libraries = new ArrayMap<>(in, 1112276993, Library.class, Library::new);
            actions   = new ArrayMap<>(in, 1112276994, Action.class, Action::new);
            armatures = new ArrayMap<>(in, 1112276995, Armature.class, Armature::new);
            curves    = new ArrayMap<>(in, 1112276996, Curve.class, Curve::new);
            lamps     = new ArrayMap<>(in, 1112276997, Lamp.class, Lamp::new);
            materials = new ArrayMap<>(in, 1112276998, Material.class, Material::new);
            meshes    = new ArrayMap<>(in, 1112276999, Mesh.class, Mesh::new);
            objects   = new ArrayMap<>(in, 1112277000, ModelObject.class, ModelObject::new);
            scenes    = new ArrayMap<>(in, 1112277001, Scene.class, Scene::new);
        }
    }
    
    public <T extends DataBlock> ArrayMap<T> getMap(DataBlock.Type dataType)
    {
        switch (dataType)
        {
            case LIBRARY:  return (ArrayMap<T>)libraries;
            case ACTION:   return (ArrayMap<T>)actions;
            case ARMATURE: return (ArrayMap<T>)armatures;
            case CURVE:    return (ArrayMap<T>)curves;
            case LAMP:     return (ArrayMap<T>)lamps;
            case MESH:     return (ArrayMap<T>)meshes;
            case OBJECT:   return (ArrayMap<T>)objects;
            case SCENE:    return (ArrayMap<T>)scenes;
            default: throw new IllegalArgumentException();
        }
    }
    
    /**
     * Releases any system resources (native memory) associated with this model.
     */
    public void destroy()
    {
        if (destroyed) throw new IllegalStateException("Already destroyed.");
        
        for (Mesh mesh : meshes) mesh.destroy();
        
        libraries.clear();
        actions.clear();
        armatures.clear();
        curves.clear();
        lamps.clear();
        meshes.clear();
        objects.clear();
        scenes.clear();
        
        destroyed = true;
    }
    
    @FunctionalInterface
    interface ModelConstructor<T>
    {
        T construct(Model model, DataInputStream in) throws IOException;
    }
    
    public final class ArrayMap<T extends DataBlock> implements Iterable<T>
    {
        private T[] array;
        private Map<String, T> map;
        
        private ArrayMap(DataInputStream in, int id, Class<T> type, ModelConstructor<T> constructor) throws IOException
        {
            int rID = in.readInt();
            if (rID != id) throw new IOException("Expected " + type + " id " + id + ", read " + rID);
            in.skip(4);
            array = (T[])Array.newInstance(type, in.readInt());
            for (int i=0; i<array.length; i++) array[i] = constructor.construct(Model.this, in);
            map = new HashMap<>(array.length);
            for (T data : array) map.put(data.name, data);
        }
        
        public boolean contains(String name)
        {
            return map.containsKey(name);
        }

        public T get(String name)
        {
            T out = map.get(name);
            if (out == null) throw new NoSuchElementException(name);
            return out;
        }
        
        public T get(int i)
        {
            return array[i];
        }
        
        public int size()
        {
            return array.length;
        }
        
        @Override
        public Iterator<T> iterator()
        {
            return new IOUtil.ArrayIterator<>(array);
        }
        
        private void clear()
        {
            array = null;
            map = null;
        }
    }
}
