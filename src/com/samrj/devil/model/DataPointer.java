package com.samrj.devil.model;

import java.util.Optional;

/**
 * @author Samuel Johnson (SmashMaster)
 * @param <T> The type of data block this pointer points to.
 * @copyright 2016 Samuel Johnson
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
    
    /**
     * Returns the model data this pointer points to, or null if none is found.
     */
    public T get()
    {   
        if (dirty)
        {
            ArrayMap<T> array = model.get(type);
            
            if (name != null) data = array.get(name);
            else if (index >= 0) data = array.get(index);
            else data = null;
            
            dirty = false;
        }
        
        return data;
    }
    
    /**
     * Returns this model data casted to the given class, or null if it does not
     * exist, or is not an instance of the given class.
     */
    public <C extends DataBlock> C cast(Class<C> castClass)
    {
        T value = get();
        return castClass.isInstance(value) ? (C)value : null;
    }
    
    /**
     * Returns an optional containing the model data this pointer points to, or
     * an empty optional if no data is found.
     */
    public Optional<T> optional()
    {
        return Optional.ofNullable(get());
    }
    
    /**
     * Returns this model data in an optional, casted to the given class, or an
     * empty optional if it is not an instance of the given class.
     */
    public <C extends DataBlock> Optional<C> optional(Class<C> castClass)
    {
        return Optional.ofNullable(cast(castClass));
    }
    
    /**
     * Returns whether this model data exists, and is an instance of the given
     * class.
     */
    public boolean isInstance(Class<?> castClass)
    {
        return castClass.isInstance(get());
    }
    
    /**
     * Returns whether this data cannot be found.
     */
    public boolean isEmpty()
    {
        return get() == null;
    }
}
