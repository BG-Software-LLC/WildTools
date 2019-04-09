package com.bgsoftware.wildtools.config;

@SuppressWarnings("unused")
public final class LangComments {

    @Comment("####################################################")
    @Comment("##                                                ##")
    @Comment("##              WildTools Messages                ##")
    @Comment("##              Developed by Ome_R                ##")
    @Comment("##                                                ##")
    @Comment("####################################################")
    @Comment(" ")
    @Comment("If you want to disable a message, set it to '' (empty)")
    public static String HEADER = "";

    @Comment(" ")
    @Comment("Called when a player doesn't have the required block.")
    public static String BUILDER_NO_BLOCK = "BUILDER_NO_BLOCK";

    @Comment(" ")
    @Comment("Called when a player runs a command not in the required usage")
    public static String COMMAND_USAGE = "COMMAND_USAGE";

    @Comment(" ")
    @Comment("Called when a player has a cool-down on a tool.")
    public static String COOLDOWN_TIME = "COOLDOWN_TIME";

    @Comment(" ")
    @Comment("Called when a player uses a crafting wand on a chest.")
    public static String CRAFT_SUCCESS = "CRAFT_SUCCESS";

    @Comment(" ")
    @Comment("Called when a player uses a cannon-tool.")
    public static String FILLED_DISPENSERS = "FILLED_DISPENSERS";

    @Comment(" ")
    @Comment("Called when a player successfully gave a tool to a player.")
    public static String GIVE_TOOL_SUCCESS = "GIVE_TOOL_SUCCESS";

    @Comment(" ")
    @Comment("Called when a player successfully sold harvested items with a harvester-tool.")
    public static String HARVESTER_SELL_SUCCEED = "HARVESTER_SELL_SUCCEED";

    @Comment(" ")
    @Comment("Called when a player runs an invalid tool sub-command.")
    public static String HELP_COMMAND_HEADER = "HELP_COMMAND_HEADER";

    @Comment(" ")
    @Comment("Called when a player clicked on a regular block with a crafting-tool.")
    public static String INVALID_CONTAINER_CRAFTING = "INVALID_CONTAINER_CRAFTING";

    @Comment(" ")
    @Comment("Called when a player clicked on a regular block with a sell-tool.")
    public static String INVALID_CONTAINER_SELL_WAND = "INVALID_CONTAINER_SELL_WAND";

    @Comment(" ")
    @Comment("Called when a player clicked on a regular block with a sort-tool.")
    public static String INVALID_CONTAINER_SORT_WAND = "INVALID_CONTAINER_SORT_WAND";

    @Comment(" ")
    @Comment("Called when a player runs a command with a player-parameter.")
    public static String INVALID_NUMBER = "INVALID_NUMBER";

    @Comment(" ")
    @Comment("Called when a player runs a command with a player-parameter.")
    public static String INVALID_PLAYER = "INVALID_PLAYER";

    @Comment(" ")
    @Comment("Called when a player runs a command with a tool-parameter.")
    public static String INVALID_TOOL = "INVALID_TOOL";

    @Comment(" ")
    @Comment("Called when a player tries to use super-breaker with a tool.")
    public static String MCMMO_TOOL_SUPER_BREAKER = "MCMMO_TOOL_SUPER_BREAKER";

    @Comment(" ")
    @Comment("Called when a player runs a command without the required permission.")
    public static String NO_PERMISSION = "NO_PERMISSION";

    @Comment(" ")
    @Comment("Called when a player successfully reloaded all configuration files.")
    public static String RELOAD_SUCCESS = "RELOAD_SUCCESS";

    @Comment(" ")
    @Comment("Called when a player selects an area with a cannon-tool.")
    public static String SELECTION_RIGHT_CLICK = "SELECTION_RIGHT_CLICK";

    @Comment(" ")
    @Comment("Called when a player tries to use cannon-tool not inside his selection.")
    public static String SELECTION_MUST_BE_INSIDE = "SELECTION_MUST_BE_INSIDE";

    @Comment(" ")
    @Comment("Called when a player tries to use cannon-tool without a valid selection.")
    public static String SELECTION_NOT_READY = "SELECTION_NOT_READY";

    @Comment(" ")
    @Comment("Called when a player toggles sell-mode on a harvester-tool.")
    public static String SELL_MODE_ENABLED = "SELL_MODE_ENABLED";

    @Comment(" ")
    @Comment("Called when a player uses a sell-tool.")
    public static String SOLD_CHEST = "SOLD_CHEST";

    @Comment(" ")
    @Comment("Called when a player successfully sorted a container with a sort-tool.")
    public static String SORTED_CHEST = "SORTED_CHEST";

    @Comment(" ")
    @Comment("Called when a player runs the info sub-command.")
    @Comment("Optional sections will be seen only if it's applied for the tool.")
    public static String TOOL_INFO_HEADER = "TOOL_INFO_HEADER";

}
