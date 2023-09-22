package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class ToolBreaksTracker {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private static final String TOOL_BREAK_IDENTIFIER = "WildTools-ToolBreak";
    private static final FixedMetadataValue TOOL_BREAK_VALUE = new FixedMetadataValue(plugin, true);

    private ToolBreaksTracker() {

    }

    public static void trackPlayer(Player player) {
        player.setMetadata(TOOL_BREAK_IDENTIFIER, TOOL_BREAK_VALUE);
    }

    public static boolean containsPlayer(Player player) {
        return player.hasMetadata(TOOL_BREAK_IDENTIFIER);
    }

    public static void removePlayer(Player player) {
        player.removeMetadata(TOOL_BREAK_IDENTIFIER, plugin);
    }

}
