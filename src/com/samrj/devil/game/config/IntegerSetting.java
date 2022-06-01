package com.samrj.devil.game.config;

import com.samrj.devil.json.JsonObject;

/**
 * A integer-valued config setting.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class IntegerSetting extends Setting<Integer>
{
    private final int defaultValue;

    public IntegerSetting(String name, int defaultValue)
    {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    Integer defaultValue()
    {
        return defaultValue;
    }

    @Override
    Integer jsonGet(JsonObject obj, String name, Integer defaultValue)
    {
        return obj.getInt(name, defaultValue);
    }

    @Override
    void jsonSet(JsonObject obj, String name, Integer value)
    {
        obj.set(name, value);
    }
}
