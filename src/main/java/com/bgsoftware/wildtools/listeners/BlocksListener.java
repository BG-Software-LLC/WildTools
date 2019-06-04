package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.objects.tools.WTool;

import com.bgsoftware.wildtools.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private static Map<UUID, Material> lastClickedType = new HashMap<>();
    private static String[] shovelMaterials = new String[] {"CLAY", "SOIL", "GRASS", "GRASS_PATH", "GRAVEL", "MYCEL",
            "SOUL_SAND", "SAND", "DIRT", "SNOW_BLOCK", "SNOW", "FARMLAND", "COARSE_DIRT", "GRASS_BLOCK",
            "MYCELIUM", "PODZOL", "RED_SAND", ""};
    private static String[] axeMaterials = new String[] {"TRAP_DOOR", "WOODEN_DOOR", "SPRUCE_DOOR", "BIRCH_DOOR",
            "JUNGLE_DOOR", "ACACIA_DOOR", "DARK_OAK_DOOR", "TRAPPED_CHEST", "FENCE", "SPRUCE_FENCE", "BIRCH_FENCE",
            "JUNGLE_FENCE", "ACACIA_FENCE", "DARK_OAK_FENCE", "CHEST", "FENCE_GATE", "SPRUCE_FENCE_GATE", "BIRCH_FENCE_GATE",
            "JUNGLE_FENCE_GATE", "ACACIA_FENCE_GATE", "DARK_OAK_FENCE_GATE", "WORKBENCH", "LOG", "LOG_2", "JUKEBOX",
            "WOOD", "WOOD_STEP", "WOOD_STAIRS", "SPRUCE_WOOD_STAIRS", "BIRCH_WOOD_STAIRS", "JUNGLE_WOOD_STAIRS",
            "ACACIA_STAIRS", "DARK_OAK_STAIRS", "BOOKSHELF", "STANDING_BANNER", "PUMPKIN", "JACK_O_LANTERN", "NOTE_BLOCK",
            "SIGN_POST", "WALL_SIGN", "DAYLIGHT_DETECTOR", "HUGE_MUSHROOM_1", "HUGE_MUSHROOM_2", "OAK_DOOR", "OAK_TRAPDOOR",
            "SPRUCE_TRAPDOOR", "BIRCH_TRAPDOOR", "JUNGLE_TRAPDOOR", "ACACIA_TRAPDOOR", "DARK_OAK_TRAPDOOR", "OAK_FENCE",
            "OAK_FENCE_GATE", "OAK_LOG", "SPRUCE_LOG", "BIRCH_LOG", "JUNGLE_LOG", "ACACIA_LOG", "DARK_OAK_LOG", "STRIPPED_OAK_LOG",
            "STRIPPED_SPRUCE_LOG", "STRIPPED_BIRCH_LOG", "STRIPPED_JUNGLE_LOG", "STRIPPED_ACACIA_LOG", "STRIPPED_DARK_OAK_LOG",
            "OAK_PLANKS", "SPRUCE_PLANKS", "BIRCH_PLANKS", "JUNGLE_PLANKS", "ACACIA_PLANKS", "DARK_OAK_PLANKS", "OAK_SLAB",
            "SPRUCE_SLAB", "BIRCH_SLAB", "JUNGLE_SLAB", "ACACIA_SLAB", "DARK_OAK_SLAB", "OAK_STAIRS", "BIRCH_STAIRS",
            "JUNGLE_STAIRS", "OAK_PRESSURE_PLATE", "SPRUCE_PRESSURE_PLATE", "BIRCH_PRESSURE_PLATE", "JUNGLE_PRESSURE_PLATE",
            "ACACIA_PRESSURE_PLATE", "DARK_OAK_PRESSURE_PLATE", "CRAFTING_TABLE", "CARVED_PUMPKIN", "WHITE_BANNER",
            "ORANGE_BANNER", "MAGENTA_BANNER", "LIGHT_BLUE_BANNER", "YELLOW_BANNER", "LIME_BANNER", "PINK_BANNER",
            "GRAY_BANNER", "LIGHT_GRAY_BANNER", "CYAN_BANNER", "PURPLE_BANNER", "BLUE_BANNER", "BROWN_BANNER", "SPRUCE_STAIRS",
            "GREEN_BANNER", "RED_BANNER", "BLACK_BANNER", "SIGN", "MUSHROOM_STEM", "RED_MUSHROOM_BLOCK", "BROWN_MUSHROOM_BLOCK"};

    private WildToolsPlugin plugin;

    public BlocksListener(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        //One of the blocks that were broken by a tool
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()))
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        ItemStack inHand = plugin.getNMSAdapter().getItemInHand(e.getPlayer());
        Tool tool = plugin.getToolsManager().getTool(inHand);

        if(tool == null)
            return;

        if(!tool.canUse(e.getPlayer().getUniqueId())){
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        if(!plugin.getToolsManager().isOwningTool(inHand, e.getPlayer())){
            e.setCancelled(true);
            Locale.NOT_OWNER.send(e.getPlayer());
            return;
        }

        WTool.toolBlockBreak.add(e.getPlayer().getUniqueId());

        if(tool.onBlockBreak(e)){
            e.setCancelled(true);
            tool.setLastUse(e.getPlayer().getUniqueId());
        }

        if(tool.isPrivate()) {
            String owner = plugin.getNMSAdapter().getTag(inHand, "tool-owner", "");
            if(owner.isEmpty()) {
                inHand = plugin.getNMSAdapter().setTag(inHand, "tool-owner", e.getPlayer().getUniqueId().toString());
                ItemUtil.formatItemStack(tool, inHand, tool.getDefaultUses(), false);
                plugin.getNMSAdapter().setItemInHand(e.getPlayer(), inHand);
            }
        }

        WTool.toolBlockBreak.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent e){
        //One of the blocks that were broken by a tool
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()) || e.getItem() == null)
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        ItemStack inHand = plugin.getNMSAdapter().getItemInHand(e.getPlayer());
        Tool tool = plugin.getToolsManager().getTool(inHand);

        if(tool == null)
            return;

        if(!tool.canUse(e.getPlayer().getUniqueId())){
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        if(!plugin.getToolsManager().isOwningTool(inHand, e.getPlayer())){
            e.setCancelled(true);
            Locale.NOT_OWNER.send(e.getPlayer());
            return;
        }

        WTool.toolBlockBreak.add(e.getPlayer().getUniqueId());

        boolean toolInteract = false;

        switch (e.getAction()){
            case RIGHT_CLICK_AIR:
                toolInteract = tool.onAirInteract(e);
                break;
            case RIGHT_CLICK_BLOCK:
                if(!e.isCancelled())
                    toolInteract = tool.onBlockInteract(e);
                break;
            case LEFT_CLICK_BLOCK:
                if(!e.isCancelled())
                    toolInteract = tool.onBlockHit(e);
                break;
        }

        if(toolInteract){
            e.setCancelled(true);
            tool.setLastUse(e.getPlayer().getUniqueId());
        }

        if(tool.isPrivate()) {
            String owner = plugin.getNMSAdapter().getTag(inHand, "tool-owner", "");
            if(owner.isEmpty()) {
                inHand = plugin.getNMSAdapter().setTag(inHand, "tool-owner", e.getPlayer().getUniqueId().toString());
                ItemUtil.formatItemStack(tool, inHand, tool.getDefaultUses(), false);
                plugin.getNMSAdapter().setItemInHand(e.getPlayer(), inHand);
            }
        }

        WTool.toolBlockBreak.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onOmniInteract(PlayerInteractEvent e){
        if(e.getAction() != Action.LEFT_CLICK_BLOCK ||
                (lastClickedType.containsKey(e.getPlayer().getUniqueId()) && lastClickedType.get(e.getPlayer().getUniqueId()) == e.getClickedBlock().getType()))
            return;

        Tool tool = plugin.getToolsManager().getTool(plugin.getNMSAdapter().getItemInHand(e.getPlayer()));

        if(tool == null || !tool.isOmni())
            return;

        lastClickedType.put(e.getPlayer().getUniqueId(), e.getClickedBlock().getType());

        String replaceType = "PICKAXE";
        if(Arrays.asList(shovelMaterials).contains(e.getClickedBlock().getType().name())){
            replaceType = plugin.getNMSAdapter().isLegacy() ? "SPADE" : "SHOVEL";
        }
        else if(Arrays.asList(axeMaterials).contains(e.getClickedBlock().getType().name())){
            replaceType = "AXE";
        }

        ItemStack itemStack = plugin.getNMSAdapter().getItemInHand(e.getPlayer());
        replaceType = itemStack.getType().name().split("_")[0] + "_" + replaceType;

        if(itemStack.getType().name().equals(replaceType))
            return;

        itemStack.setType(Material.valueOf(replaceType));
        plugin.getNMSAdapter().setItemInHand(e.getPlayer(), itemStack);
    }

    private String getTime(long timeLeft){
        String time = "";

        // Get rid of miliseconds
        timeLeft = timeLeft / 1000;

        if(timeLeft >= 3600) {
            if (timeLeft / 3600 == 1)
                time += "1 hour, ";
            else time += (timeLeft / 3600) + " hours, ";
            timeLeft %= 3600;
        }

        if(timeLeft >= 60){
            if (timeLeft / 60 == 1)
                time += "1 minute, ";
            else time += (timeLeft / 60) + " minutes, ";
            timeLeft %= 60;
        }

        if(timeLeft != 0) {
            if (timeLeft == 1)
                time += timeLeft + " second";
            else time += timeLeft + " seconds";
            return time;
        }

        return time.substring(0, time.length() - 2);
    }

}
