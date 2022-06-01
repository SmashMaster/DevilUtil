package com.samrj.devil.game.config;

import com.samrj.devil.json.Json;
import com.samrj.devil.json.JsonArray;
import com.samrj.devil.json.JsonObject;
import com.samrj.devil.math.Vec2i;

/**
 * A 2D integer-valued config setting.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Vec2iSetting extends Setting<Vec2i>
{
    private final Vec2i defaultValue;

    public Vec2iSetting(String name, Vec2i defaultValue)
    {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    Vec2i defaultValue()
    {
        return defaultValue;
    }

    @Override
    Vec2i jsonGet(JsonObject obj, String name, Vec2i defaultValue)
    {
        try
        {
            JsonArray jarray = obj.require(name).asArray();
            return new Vec2i(jarray.get(0).asInt(), jarray.get(1).asInt());
        }
        catch (Throwable t) //Sloppy, but this works.
        {
            return new Vec2i(defaultValue);
        }
    }

    @Override
    void jsonSet(JsonObject obj, String name, Vec2i value)
    {
        JsonArray jarray = Json.array();
        jarray.add(value.x);
        jarray.add(value.y);
        obj.set(name, jarray);
    }
}
