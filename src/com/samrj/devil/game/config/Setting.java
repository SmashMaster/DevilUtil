package com.samrj.devil.game.config;

import com.samrj.devil.json.JsonObject;

import java.util.Objects;
import java.util.function.Consumer;


/**
 * Abstract config setting class. Saves/loads settings to/from JSON.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Setting<T>
{
    public final String name;

    private T value;
    private Consumer<T> callback;

    Setting(String name)
    {
        this.name = name;
    }

    abstract T defaultValue();
    abstract T jsonGet(JsonObject obj, String name, T defaultValue);
    abstract void jsonSet(JsonObject obj, String name, T value);

    public void set(T newValue)
    {
        if (!Objects.equals(value, newValue))
        {
            value = newValue;
            if (callback != null) callback.accept(newValue);
        }
    }

    public T get()
    {
        return value;
    }

    public void onChanged(Consumer<T> newCallback)
    {
        callback = newCallback;
    }

    void load(JsonObject json)
    {
        T newValue = value;
        try
        {
            newValue = jsonGet(json, name, defaultValue());
        }
        finally
        {
            set(newValue);
        }
    }

    void save(JsonObject json)
    {
        jsonSet(json, name, value);
    }
}
