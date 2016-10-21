package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.model.DataBlock.Type;
import com.samrj.devil.res.Resource;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;

/**
 * .DVM file loader. Corresponds with the Blender python exporter.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Model
{
    private static final byte[] MAGIC = IOUtil.hexToBytes("9F0A446576696C4D6F64656C");
    private static final int VERSION_MAJOR = 0, VERSION_MINOR = 24;
    
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
        
        try (BufferedInputStream inputStream = new BufferedInputStream(Resource.open(path)))
        {
            DataInputStream in = new DataInputStream(inputStream);
            
            byte[] header = new byte[12];
            in.read(header);
            if (!Arrays.equals(header, MAGIC))
                throw new IOException("Illegal file format specified.");
            int versionMajor = in.readShort();
            int versionMinor = in.readShort();
            if (versionMajor != VERSION_MAJOR || versionMinor != VERSION_MINOR)
                throw new IOException("Unable to load DVM version " + versionMajor + "." + versionMinor);
            
            for (Type type : Type.values())
                arraymaps.put(type, type.makeArrayMap(this, in));
            
            libraries = get(Type.LIBRARY);
            actions = get(Type.ACTION);
            armatures = get(Type.ARMATURE);
            curves = get(Type.CURVE);
            lamps = get(Type.LAMP);
            materials = get(Type.MATERIAL);
            meshes = get(Type.MESH);
            objects = get(Type.OBJECT);
            scenes = get(Type.SCENE);
            textures = get(Type.TEXTURE);
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
