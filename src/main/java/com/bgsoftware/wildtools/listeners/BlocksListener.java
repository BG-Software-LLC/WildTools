package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.tools.ToolBreaksTracker;
import com.bgsoftware.wildtools.utils.ServerVersion;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BlocksListener implements Listener {

    private final Map<UUID, Map<Integer, OmniInteraction>> lastOmniInteractions = new HashMap<>();

    private final WildToolsPlugin plugin;

    public BlocksListener(WildToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        //One of the blocks that were broken by a tool
        if (ToolBreaksTracker.containsPlayer(e.getPlayer()))
            return;

        if (!e.getPlayer().hasPermission("wildtools.use"))
            return;

        ToolItemStack toolItemStack = ToolItemStack.of(plugin.getNMSAdapter().getItemInHand(e.getPlayer()));
        Tool tool = toolItemStack.getTool();

        if (tool == null)
            return;

        String world = e.getBlock().getWorld().getName();

        if (!tool.isWhitelistedWorld(world) || tool.isBlacklistedWorld(world)) {
            e.setCancelled(true);
            return;
        }

        if (!tool.canUse(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        if (!plugin.getToolsManager().isOwningTool(toolItemStack.getItem(), e.getPlayer())) {
            e.setCancelled(true);
            Locale.NOT_OWNER.send(e.getPlayer());
            return;
        }

        try {
            ToolBreaksTracker.trackPlayer(e.getPlayer());

            if (tool.onBlockBreak(e)) {
                e.setCancelled(true);
                tool.setLastUse(e.getPlayer().getUniqueId());
            }

            if (tool.isPrivate()) {
                String owner = toolItemStack.getOwner();
                if (owner.isEmpty())
                    toolItemStack.setOwner(e.getPlayer().getUniqueId().toString());
            }
        } finally {
            ToolBreaksTracker.removePlayer(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockInteract(PlayerInteractEvent e) {
        //One of the blocks that were broken by a tool
        if (e.getItem() == null || ToolBreaksTracker.containsPlayer(e.getPlayer()))
            return;

        if (!e.getPlayer().hasPermission("wildtools.use"))
            return;

        ToolItemStack toolItemStack = ToolItemStack.of(e.getItem());
        Tool tool = toolItemStack.getTool();

        if (tool == null)
            return;

        String world = e.getPlayer().getWorld().getName();

        if (!tool.isWhitelistedWorld(world) || tool.isBlacklistedWorld(world)) {
            e.setCancelled(true);
            return;
        }

        if (!tool.canUse(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        if (!plugin.getToolsManager().isOwningTool(toolItemStack.getItem(), e.getPlayer())) {
            e.setCancelled(true);
            Locale.NOT_OWNER.send(e.getPlayer());
            return;
        }

        try {
            ToolBreaksTracker.trackPlayer(e.getPlayer());

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
        } finally {
            ToolBreaksTracker.removePlayer(e.getPlayer());
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onItemDamage(PlayerItemDamageEvent e) {
        ToolItemStack toolItemStack = ToolItemStack.of(e.getItem());
        Tool tool = toolItemStack.getTool();

        if (tool == null)
            return;

        e.setCancelled(true);

        if (tool.isUnbreakable() || !tool.hasVanillaDamage())
            return;

        tool.reduceDurablility(e.getPlayer(), tool.isUsingDurability() ? e.getDamage() : 1, e.getItem());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityInteract(PlayerInteractAtEntityEvent e) {
        //One of the blocks that were broken by a tool
        if (ToolBreaksTracker.containsPlayer(e.getPlayer()))
            return;

        if (!e.getPlayer().hasPermission("wildtools.use"))
            return;

        ToolItemStack toolItemStack = ToolItemStack.of(plugin.getNMSAdapter().getItemInHand(e.getPlayer(), e));
        Tool tool = toolItemStack.getTool();

        if (tool == null)
            return;

        String world = e.getRightClicked().getWorld().getName();

        if (!tool.isWhitelistedWorld(world) || tool.isBlacklistedWorld(world)) {
            e.setCancelled(true);
            return;
        }

        if (!tool.canUse(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        if (!plugin.getToolsManager().isOwningTool(toolItemStack.getItem(), e.getPlayer())) {
            e.setCancelled(true);
            Locale.NOT_OWNER.send(e.getPlayer());
            return;
        }

        try {
            ToolBreaksTracker.trackPlayer(e.getPlayer());

            if (tool.onAirInteract(new PlayerInteractEvent(e.getPlayer(), Action.RIGHT_CLICK_AIR, toolItemStack.getItem(), null, BlockFace.UP))) {
                e.setCancelled(true);
                tool.setLastUse(e.getPlayer().getUniqueId());
            }

            if (tool.isPrivate()) {
                String owner = toolItemStack.getOwner();
                if (owner.isEmpty())
                    toolItemStack.setOwner(e.getPlayer().getUniqueId().toString());
            }
        } finally {
            ToolBreaksTracker.removePlayer(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onOmniInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Material blockType = e.getClickedBlock().getType();
        String world = e.getClickedBlock().getWorld().getName();
        ItemStack itemInHand = plugin.getNMSAdapter().getItemInHand(e.getPlayer(), e);
        int heldSlot = e.getPlayer().getInventory().getHeldItemSlot();

        Map<Integer, OmniInteraction> playerInteractions = lastOmniInteractions.get(e.getPlayer().getUniqueId());
        OmniInteraction lastInteraction = playerInteractions == null ? null : playerInteractions.get(heldSlot);
        if (lastInteraction != null && lastInteraction.matches(world, blockType, itemInHand))
            return;

        ToolItemStack toolItemStack = ToolItemStack.of(itemInHand);
        Tool tool = toolItemStack.getTool();

        if (tool == null || !tool.isOmni())
            return;

        if (!tool.isWhitelistedWorld(world) || tool.isBlacklistedWorld(world)) {
            e.setCancelled(true);
            return;
        }

        String replaceTypeName;

        switch (plugin.getNMSAdapter().getDestroySpeedCategory(blockType)) {
            case AXE:
                replaceTypeName = "AXE";
                break;
            case SHOVEL:
                replaceTypeName = ServerVersion.isLegacy() ? "SPADE" : "SHOVEL";
                break;
            default:
                replaceTypeName = "PICKAXE";
                break;
        }

        Material replaceType = Material.valueOf(toolItemStack.getType().name().split("_")[0] + "_" + replaceTypeName);

        if (toolItemStack.getType() != replaceType)
            toolItemStack.setType(replaceType);

        lastOmniInteractions.computeIfAbsent(e.getPlayer().getUniqueId(), uuid -> new HashMap<>())
                .put(heldSlot, new OmniInteraction(world, blockType, toolItemStack.getItem()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        lastOmniInteractions.remove(e.getPlayer().getUniqueId());
    }

    private static class OmniInteraction {

        private final String world;
        private final Material blockType;
        private final ItemStack itemInHand;

        private OmniInteraction(String world, Material blockType, ItemStack itemInHand) {
            this.world = world;
            this.blockType = blockType;
            this.itemInHand = itemInHand.clone();
        }

        private boolean matches(String world, Material blockType, ItemStack itemInHand) {
            return this.world.equals(world) && this.blockType == blockType && this.itemInHand.equals(itemInHand);
        }
    }

    private String getTime(long timeLeft) {
        StringBuilder timeAsString = new StringBuilder();

        // Convert time to seconds
        timeLeft = TimeUnit.MILLISECONDS.toSeconds(timeLeft);

        if (timeLeft >= 3600) {
            long hoursLeft = timeLeft / 3600;
            if (hoursLeft == 1) {
                timeAsString.append(", 1 hour");
            } else {
                timeAsString.append(", ").append(hoursLeft).append(" hours");
            }
            timeLeft %= 3600;
        }

        if (timeLeft >= 60) {
            long minutesLeft = timeLeft / 60;
            if (minutesLeft == 1) {
                timeAsString.append(", 1 minute");
            } else {
                timeAsString.append(", ").append(minutesLeft).append(" minutes");
            }
            timeLeft %= 60;
        }

        if (timeLeft == 1) {
            timeAsString.append(", 1 second");
        } else {
            timeAsString.append(", ").append(timeLeft).append(" seconds");
        }

        return timeAsString.substring(2);
    }

}
