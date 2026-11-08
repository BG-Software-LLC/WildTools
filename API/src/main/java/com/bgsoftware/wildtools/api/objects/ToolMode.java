package com.bgsoftware.wildtools.api.objects;

import com.google.common.base.Preconditions;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class ToolMode {

    private static final Map<String, ToolMode> toolModes = new LinkedHashMap<>();
    private static int ordinalCounter = 0;

    public static final ToolMode BUILDER = registerBuiltin("BUILDER");
    public static final ToolMode CANNON = registerBuiltin("CANNON");
    public static final ToolMode CRAFTING = registerBuiltin("CRAFTING");
    public static final ToolMode CROWBAR = registerBuiltin("CROWBAR");
    public static final ToolMode CUBOID = registerBuiltin("CUBOID");
    public static final ToolMode DRAIN = registerBuiltin("DRAIN");
    public static final ToolMode HARVESTER = registerBuiltin("HARVESTER");
    public static final ToolMode ICE = registerBuiltin("ICE");
    public static final ToolMode LIGHTNING = registerBuiltin("LIGHTNING");
    public static final ToolMode MAGNET = registerBuiltin("MAGNET");
    public static final ToolMode PILLAR = registerBuiltin("PILLAR");
    public static final ToolMode SELL = registerBuiltin("SELL");
    public static final ToolMode SORT = registerBuiltin("SORT");

    private final String name;
    private final int ordinal;
    private final boolean builtin;

    private ToolMode(String name, boolean builtin) {
        this.name = name;
        this.ordinal = ordinalCounter++;
        this.builtin = builtin;
    }

    public String name() {
        return name;
    }

    public int ordinal() {
        return ordinal;
    }

    public static synchronized ToolMode[] values() {
        return toolModes.values().toArray(new ToolMode[toolModes.size()]);
    }

    public static synchronized ToolMode valueOf(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        ToolMode mode = getByName(name);
        Preconditions.checkArgument(mode != null, "Couldn't find a ToolMode with the name " + name + ".");
        return mode;
    }

    public static synchronized ToolMode getByName(String name) {
        return name == null ? null : toolModes.get(name.toUpperCase(Locale.ENGLISH));
    }

    public static synchronized ToolMode register(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        name = name.toUpperCase(Locale.ENGLISH);
        Preconditions.checkArgument(name.matches("[A-Z0-9_:-]+"), "Invalid tool mode name " + name + ".");
        Preconditions.checkState(!toolModes.containsKey(name), "ToolMode with the name " + name + " already exists.");

        ToolMode mode = new ToolMode(name, false);
        toolModes.put(name, mode);
        return mode;
    }

    public static synchronized void unregister(ToolMode mode) {
        Preconditions.checkNotNull(mode, "mode parameter cannot be null.");
        Preconditions.checkArgument(!mode.builtin, "Cannot unregister builtin tool mode " + mode.name + ".");
        if (toolModes.get(mode.name) == mode)
            toolModes.remove(mode.name);
    }

    private static ToolMode registerBuiltin(String name) {
        ToolMode mode = new ToolMode(name, true);
        toolModes.put(name, mode);
        return mode;
    }

    @Override
    public String toString() {
        return name;
    }

}