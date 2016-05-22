package com.samrj.devil.model;

/**
 * @author Samuel Johnson (SmashMaster)
 * @param <T> The type of data block this pointer points to.
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class DataPointer<T extends DataBlock>
{
    public final DataBlock.Type type;
    
    private final Model model;
    private final String name;
    private final int index;
    
    private T data;
    private boolean dirty = true;
    
    DataPointer(Model model, DataBlock.Type type, String name)
    {
        this.model = model;
        this.name = name;
        index = -1;
        this.type = type;
    }
    
    DataPointer(Model model, int typeID, String name)
    {
        this(model, DataBlock.getType(typeID), name);
    }
    
    DataPointer(Model model, DataBlock.Type type, int index)
    {
        this.model = model;
        name = null;
        this.index = index;
        this.type = type;
    }
    
    DataPointer(Model model, int typeID, int index)
    {
        this(model, DataBlock.getType(typeID), index);
    }
    
    public T get()
    {   
        if (dirty)
        {
            Model.ArrayMap<T> array = model.getMap(type);
            
            if (name != null) data = array.get(name);
            else if (index >= 0) data = array.get(index);
            else data = null;
            
            dirty = false;
        }
        
        return data;
    }
}
