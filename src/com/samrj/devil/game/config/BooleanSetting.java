package com.samrj.devil.game.config;

import com.samrj.devil.json.JsonObject;

/**
 * A boolean-valued config setting.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class BooleanSetting extends Setting<Boolean>
{
    private final boolean defaultValue;

    public BooleanSetting(String name, boolean defaultValue)
    {
        super(name);
        this.defaultValue = defaultValue;
    }

    @Override
    Boolean defaultValue()
    {
        return defaultValue;
    }

    @Override
    Boolean jsonGet(JsonObject obj, String name, Boolean defaultValue)
    {
        return obj.getBoolean(name, defaultValue);
    }

    @Override
    void jsonSet(JsonObject obj, String name, Boolean value)
    {
        obj.set(name, value);
    }
}
