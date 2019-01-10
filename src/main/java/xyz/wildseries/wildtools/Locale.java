package xyz.wildseries.wildtools;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Locale {

    private static Map<String, Locale> localeMap = new HashMap<>();

    public static Locale BUILDER_NO_BLOCK = new Locale("BUILDER_NO_BLOCK");
    public static Locale COMMAND_USAGE = new Locale("COMMAND_USAGE");
    public static Locale COOLDOWN_TIME = new Locale("COOLDOWN_TIME");
    public static Locale CRAFT_SUCCESS = new Locale("CRAFT_SUCCESS");
    public static Locale FILLED_DISPENSERS = new Locale("FILLED_DISPENSERS");
    public static Locale GIVE_TOOL_SUCCESS = new Locale("GIVE_TOOL_SUCCESS");
    public static Locale HARVESTER_SELL_SUCCEED = new Locale("HARVESTER_SELL_SUCCEED");
    public static Locale HELP_COMMAND_HEADER = new Locale("HELP_COMMAND_HEADER");
    public static Locale HELP_COMMAND_LINE = new Locale("HELP_COMMAND_LINE");
    public static Locale HELP_COMMAND_FOOTER = new Locale("HELP_COMMAND_FOOTER");
    public static Locale INVALID_CONTAINER_CRAFTING = new Locale("INVALID_CONTAINER_CRAFTING");
    public static Locale INVALID_CONTAINER_SELL_WAND = new Locale("INVALID_CONTAINER_SELL_WAND");
    public static Locale INVALID_CONTAINER_SORT_WAND = new Locale("INVALID_CONTAINER_SORT_WAND");
    public static Locale INVALID_NUMBER = new Locale("INVALID_NUMBER");
    public static Locale INVALID_PLAYER = new Locale("INVALID_PLAYER");
    public static Locale INVALID_TOOL = new Locale("INVALID_TOOL");
    public static Locale MCMMO_TOOL_SUPER_BREAKER = new Locale("MCMMO_TOOL_SUPER_BREAKER");
    public static Locale NO_PERMISSION = new Locale("NO_PERMISSION");
    public static Locale RELOAD_SUCCESS = new Locale("RELOAD_SUCCESS");
    public static Locale SELECTION_RIGHT_CLICK = new Locale("SELECTION_RIGHT_CLICK");
    public static Locale SELECTION_LEFT_CLICK = new Locale("SELECTION_LEFT_CLICK");
    public static Locale SELECTION_MUST_BE_INSIDE = new Locale("SELECTION_MUST_BE_INSIDE");
    public static Locale SELECTION_NOT_READY = new Locale("SELECTION_NOT_READY");
    public static Locale SELL_MODE_ENABLED = new Locale("SELL_MODE_ENABLED");
    public static Locale SELL_MODE_DISABLED = new Locale("SELL_MODE_DISABLED");
    public static Locale SOLD_CHEST = new Locale("SOLD_CHEST");
    public static Locale SORTED_CHEST = new Locale("SORTED_CHEST");
    public static Locale TOOL_INFO_HEADER = new Locale("TOOL_INFO_HEADER");
    public static Locale TOOL_INFO_NAME = new Locale("TOOL_INFO_NAME");
    public static Locale TOOL_INFO_MODE = new Locale("TOOL_INFO_MODE");
    public static Locale TOOL_INFO_TYPE = new Locale("TOOL_INFO_TYPE");
    public static Locale TOOL_INFO_WHITELISTED_BLOCKS = new Locale("TOOL_INFO_WHITELISTED_BLOCKS");
    public static Locale TOOL_INFO_WHITELISTED_DROPS = new Locale("TOOL_INFO_WHITELISTED_DROPS");
    public static Locale TOOL_INFO_BLACKLISTED_BLOCKS = new Locale("TOOL_INFO_BLACKLISTED_BLOCKS");
    public static Locale TOOL_INFO_BLACKLISTED_DROPS = new Locale("TOOL_INFO_BLACKLISTED_DROPS");
    public static Locale TOOL_INFO_UNBREAKABLE = new Locale("TOOL_INFO_UNBREAKABLE");
    public static Locale TOOL_INFO_USES = new Locale("TOOL_INFO_USES");
    public static Locale TOOL_INFO_COOLDOWN = new Locale("TOOL_INFO_COOLDOWN");
    public static Locale TOOL_INFO_AUTO_COLLECT = new Locale("TOOL_INFO_AUTO_COLLECT");
    public static Locale TOOL_INFO_SILK_TOUCH = new Locale("TOOL_INFO_SILK_TOUCH");
    public static Locale TOOL_INFO_SAME_TYPE = new Locale("TOOL_INFO_SAME_TYPE");
    public static Locale TOOL_INFO_INSIDE_CLAIM = new Locale("TOOL_INFO_INSIDE_CLAIM");
    public static Locale TOOL_INFO_BREAK_LEVEL = new Locale("TOOL_INFO_BREAK_LEVEL");
    public static Locale TOOL_INFO_RADIUS = new Locale("TOOL_INFO_RADIUS");
    public static Locale TOOL_INFO_TNT_AMOUNT = new Locale("TOOL_INFO_TNT_AMOUNT");
    public static Locale TOOL_INFO_FOOTER = new Locale("TOOL_INFO_FOOTER");

    private Locale(String identifier){
        localeMap.put(identifier, this);
    }

    private String message;

    public String getMessage(Object... objects){
        if(message != null && !message.equals("")) {
            String msg = message;

            for (int i = 0; i < objects.length; i++)
                msg = msg.replace("{" + i + "}", objects[i].toString());

            return msg;
        }

        return null;
    }

    public void send(CommandSender sender, Object... objects){
        String message = getMessage(objects);
        if(message != null && sender != null)
            sender.sendMessage(message);
    }

    private void setMessage(String message){
        this.message = message;
    }

    public static void reload(){
        WildToolsPlugin.log("Loading messages started...");
        long startTime = System.currentTimeMillis();
        int messagesAmount = 0;

        File file = new File(WildToolsPlugin.getPlugin().getDataFolder(), "lang.yml");

        if(!file.exists())
            WildToolsPlugin.getPlugin().saveResource("lang.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        //Convert old data
        if(cfg.contains("CLICKED_INVALID_CONTAINER"))
            cfg.set("INVALID_CONTAINER_SELL_WAND", cfg.getString("CLICKED_INVALID_CONTAINER"));

        for(String identifier : localeMap.keySet())
            localeMap.get(identifier).setMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString(identifier, "")));

        WildToolsPlugin.log(" - Found " + messagesAmount + " messages in lang.yml.");
        WildToolsPlugin.log("Loading messages done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

}
