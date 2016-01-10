package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.io.StreamConstructor;
import com.samrj.devil.res.Resource;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * .DVM file loader. Corresponds with the Blender python exporter.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Model
{
    private static final byte[] MAGIC = IOUtil.hexToBytes("9F0A446576696C4D6F64656C");
    
    public final int versionMajor, versionMinor;
    public final ArrayMap<Action> actions;
    public final ArrayMap<Armature> armatures;
    public final ArrayMap<Lamp> lamps;
    public final ArrayMap<Material> materials;
    public final ArrayMap<Mesh> meshes;
    public final ArrayMap<ModelObject> objects;
    public final ArrayMap<Scene> scenes;
    
    Model(BufferedInputStream inputStream) throws IOException
    {
        try
        {
            DataInputStream in = new DataInputStream(inputStream);
            
            byte[] header = new byte[12];
            in.read(header);
            if (!Arrays.equals(header, MAGIC))
                throw new IOException("Illegal file format specified.");
            versionMajor = in.readShort();
            versionMinor = in.readShort();
            if (versionMajor != 0) throw new IOException("Unable to load DVM version " + versionMajor);
            
            actions = new ArrayMap<>(in, 32, Action.class, Action::new);
            armatures = new ArrayMap<>(in, 33, Armature.class, Armature::new);
            lamps = new ArrayMap<>(in, 34, Lamp.class, Lamp::new);
            materials = new ArrayMap<>(in, 35, Material.class, Material::new);
            meshes = new ArrayMap<>(in, 36, Mesh.class, Mesh::new);
            objects = new ArrayMap<>(in, 37, ModelObject.class, ModelObject::new);
            scenes = new ArrayMap<>(in, 38, Scene.class, Scene::new);
            
            for (ModelObject object : objects) object.populate(this);
            for (Scene scene : scenes) scene.populate(this);
        }
        finally
        {
            inputStream.close();
        }
    }
    
    public Model(String path) throws IOException
    {
        this(new BufferedInputStream(Resource.open(path)));
    }
    
    /**
     * Releases any system resources (native memory) associated with this model.
     */
    public void destroy()
    {
        for (Mesh mesh : meshes) mesh.destroy();
        actions.clear();
        armatures.clear();
        lamps.clear();
        materials.clear();
        meshes.clear();
        objects.clear();
        scenes.clear();
    }
    
    public static final class ArrayMap<T extends DataBlock> implements Iterable<T>
    {
        private T[] array;
        private Map<String, T> map;
        
        private ArrayMap(DataInputStream in, int id, Class<T> type, StreamConstructor<T> constructor) throws IOException
        {
            if (in.readInt() != id) throw new IOException("Corrupt DVM.");
            in.skip(4);
            array = IOUtil.arrayFromStream(in, type, constructor);
            map = new HashMap<>(array.length);
            for (T data : array) map.put(data.getName(), data);
        }

        public T get(String name)
        {
            T out = map.get(name);
            if (out == null) throw new NoSuchElementException();
            return out;
        }
        
        public T get(int i)
        {
            return array[i];
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
