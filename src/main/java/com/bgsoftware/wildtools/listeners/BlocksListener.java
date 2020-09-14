package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.objects.tools.WTool;

import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bgsoftware.wildtools.api.objects.tools.Tool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private static final Map<UUID, Material> lastClickedType = new HashMap<>();

    private final WildToolsPlugin plugin;

    public BlocksListener(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        //One of the blocks that were broken by a tool
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()))
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        ToolItemStack toolItemStack = ToolItemStack.of(plugin.getNMSAdapter().getItemInHand(e.getPlayer()));
        Tool tool = toolItemStack.getTool();

        if(tool == null)
            return;

        String world = e.getBlock().getWorld().getName();

        if(!tool.isWhitelistedWorld(world) || tool.isBlacklistedWorld(world)){
            e.setCancelled(true);
            return;
        }

        if(!tool.canUse(e.getPlayer().getUniqueId())){
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        if(!plugin.getToolsManager().isOwningTool(toolItemStack.getItem(), e.getPlayer())){
            e.setCancelled(true);
            Locale.NOT_OWNER.send(e.getPlayer());
            return;
        }

        try {
            WTool.toolBlockBreak.add(e.getPlayer().getUniqueId());

            if (tool.onBlockBreak(e)) {
                e.setCancelled(true);
                tool.setLastUse(e.getPlayer().getUniqueId());
            }

            if (tool.isPrivate()) {
                String owner = toolItemStack.getOwner();
                if (owner.isEmpty())
                    toolItemStack.setOwner(e.getPlayer().getUniqueId().toString());
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }finally {
            WTool.toolBlockBreak.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockInteract(PlayerInteractEvent e){
        //One of the blocks that were broken by a tool
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()) || e.getItem() == null)
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        ToolItemStack toolItemStack = ToolItemStack.of(e.getItem());
        Tool tool = toolItemStack.getTool();

        if(tool == null)
            return;

        String world = e.getPlayer().getWorld().getName();

        if(!tool.isWhitelistedWorld(world) || tool.isBlacklistedWorld(world)){
            e.setCancelled(true);
            return;
        }

        if(!tool.canUse(e.getPlayer().getUniqueId())){
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        if(!plugin.getToolsManager().isOwningTool(toolItemStack.getItem(), e.getPlayer())){
            e.setCancelled(true);
            Locale.NOT_OWNER.send(e.getPlayer());
            return;
        }

        try {
            WTool.toolBlockBreak.add(e.getPlayer().getUniqueId());

            boolean toolInteract = false;

            switch (e.getAction()) {
                case RIGHT_CLICK_AIR:
                    toolInteract = tool.onAirInteract(e);
                    break;
                case RIGHT_CLICK_BLOCK:
                    if (!e.isCancelled())
                        toolInteract = tool.onBlockInteract(e);
                    break;
                case LEFT_CLICK_BLOCK:
                    if (!e.isCancelled())
                        toolInteract = tool.onBlockHit(e);
                    break;
            }

            if (toolInteract) {
                e.setCancelled(true);
                tool.setLastUse(e.getPlayer().getUniqueId());
            }

            if (tool.isPrivate()) {
                String owner = toolItemStack.getOwner();
                if (owner.isEmpty())
                    toolItemStack.setOwner(e.getPlayer().getUniqueId().toString());
            }
        }finally {
            WTool.toolBlockBreak.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityInteract(PlayerInteractAtEntityEvent e){
        //One of the blocks that were broken by a tool
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()))
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        ToolItemStack toolItemStack = ToolItemStack.of(plugin.getNMSAdapter().getItemInHand(e.getPlayer(), e));
        Tool tool = toolItemStack.getTool();

        if(tool == null)
            return;

        String world = e.getRightClicked().getWorld().getName();

        if(!tool.isWhitelistedWorld(world) || tool.isBlacklistedWorld(world)){
            e.setCancelled(true);
            return;
        }

        if(!tool.canUse(e.getPlayer().getUniqueId())){
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        if(!plugin.getToolsManager().isOwningTool(toolItemStack.getItem(), e.getPlayer())){
            e.setCancelled(true);
            Locale.NOT_OWNER.send(e.getPlayer());
            return;
        }

        try {
            WTool.toolBlockBreak.add(e.getPlayer().getUniqueId());

            if (tool.onAirInteract(new PlayerInteractEvent(e.getPlayer(), Action.RIGHT_CLICK_AIR, toolItemStack.getItem(), null, BlockFace.UP))) {
                e.setCancelled(true);
                tool.setLastUse(e.getPlayer().getUniqueId());
            }

            if (tool.isPrivate()) {
                String owner = toolItemStack.getOwner();
                if (owner.isEmpty())
                    toolItemStack.setOwner(e.getPlayer().getUniqueId().toString());
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }finally {
            WTool.toolBlockBreak.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onOmniInteract(PlayerInteractEvent e){
        if(e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Material blockType = e.getClickedBlock().getType();

        if(lastClickedType.containsKey(e.getPlayer().getUniqueId()) && lastClickedType.get(e.getPlayer().getUniqueId()) == blockType)
            return;

        ToolItemStack toolItemStack = ToolItemStack.of(plugin.getNMSAdapter().getItemInHand(e.getPlayer(), e));
        Tool tool = toolItemStack.getTool();

        if(tool == null || !tool.isOmni())
            return;

        String world = e.getClickedBlock().getWorld().getName();

        if(!tool.isWhitelistedWorld(world) || tool.isBlacklistedWorld(world)){
            e.setCancelled(true);
            return;
        }

        lastClickedType.put(e.getPlayer().getUniqueId(), blockType);

        String replaceType = "PICKAXE";

        if(plugin.getNMSAdapter().isShovelType(blockType)) {
            replaceType = plugin.getNMSAdapter().isLegacy() ? "SPADE" : "SHOVEL";
        }
        else if(plugin.getNMSAdapter().isAxeType(blockType)) {
            replaceType = "AXE";
        }

        replaceType = toolItemStack.getType().name().split("_")[0] + "_" + replaceType;

        if(toolItemStack.getType().name().equals(replaceType))
            return;

        toolItemStack.setType(Material.valueOf(replaceType));
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
