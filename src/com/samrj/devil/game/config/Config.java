package com.samrj.devil.game.config;

import com.samrj.devil.game.Game;
import com.samrj.devil.json.Json;
import com.samrj.devil.json.JsonObject;
import com.samrj.devil.json.PrettyPrint;
import com.samrj.devil.json.WriterConfig;
import com.samrj.devil.math.Vec2i;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Config setting class. Saves/loads settings to/from JSON.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Config
{
    private static final WriterConfig WRITER_CONFIG = PrettyPrint.indentWithSpaces(4);

    private final Game.State gameState;
    private File file = new File("settings.json");
    
    private final Map<String, Setting> settings = new LinkedHashMap<>(); //Preserves insertion order.
    
    public final Setting<Boolean> fullscreen = new BooleanSetting("fullscreen", false);
    public final Setting<Vec2i>   resolution = new Vec2iSetting("resolution", new Vec2i(1280, 720));
    public final Setting<Boolean> vSync = new BooleanSetting("vSync", false);
    public final Setting<Integer> fpsLimit = new IntegerSetting("fpsLimit", 60);
    public final Setting<Boolean> showFPS = new BooleanSetting("showFPS", true);
    public final Setting<Integer> gamepadDeadzone = new IntegerSetting("gamepadDeadzone", 10);

    public final Controls controls;

    public Config(Game.State gameState)
    {
        gameState.requireNew();
        this.gameState = gameState;

        add(fullscreen);
        add(resolution);
        add(vSync);
        add(fpsLimit);
        add(showFPS);
        add(gamepadDeadzone);

        controls = new Controls(gameState);
    }

    public void setFile(File file)
    {
        gameState.requireNew();
        this.file = Objects.requireNonNull(file);
    }

    public <T extends Setting<?>> T add(T setting)
    {
        gameState.requireNew();

        if (settings.containsKey(setting.name) || setting.name.equals("controls"))
            throw new IllegalArgumentException("Duplicate setting name " + setting.name);

        settings.put(setting.name, setting);

        return setting;
    }

    public <T extends Setting> T get(String name, Class<T> cls)
    {
        return cls.cast(settings.get(name));
    }

    public IntegerSetting getInt(String name)
    {
        return get(name, IntegerSetting.class);
    }

    public BooleanSetting getBool(String name)
    {
        return get(name, BooleanSetting.class);
    }

    public Vec2iSetting getVec2i(String name)
    {
        return get(name, Vec2iSetting.class);
    }
    
    public void load()
    {
        gameState.requireStarted();

        try
        {
            JsonObject json = Json.parse(file).asObject();
            for (Setting setting : settings.values()) setting.load(json);
            controls.load(json, "controls");
        }
        catch (Throwable t) //Load and save functions must not crash game.
        {
            if (gameState.isDebugEnabled()) System.err.println("Failed to load config: " + t);

            for (Setting setting : settings.values()) setting.set(setting.defaultValue());
            controls.setToDefaults();
        }
    }

    public void save()
    {
        gameState.requireStarted();

        if (!file.exists()) try
        {
            file.createNewFile();
        }
        catch (Throwable t)
        {
            if (gameState.isDebugEnabled()) System.err.println("Failed to make config: " + t);
            return;
        }
        
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file)))
        {
            JsonObject obj = Json.object();
            for (Setting setting : settings.values()) setting.save(obj);
            controls.save(obj, "controls");
            writer.append(obj.toString(WRITER_CONFIG));
            writer.flush();
        }
        catch (Throwable t)
        {
            if (gameState.isDebugEnabled()) System.err.println("Failed to save config: " + t);
        }
    }
}
