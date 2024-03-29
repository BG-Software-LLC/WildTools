package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.api.events.IceWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.IceTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WIceTool extends WTool implements IceTool {

    private final int radius;

    public WIceTool(Material type, String name, int radius) {
        super(type, name, ToolMode.ICE);
        this.radius = radius;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        return handleUse(e.getPlayer(), e.getItem(), e.getPlayer().getLocation().getBlock());
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        return handleUse(e.getPlayer(), e.getItem(), e.getPlayer().getLocation().getBlock());
    }

    private boolean handleUse(Player player, ItemStack usedItem, Block block) {
        Location max = block.getLocation().clone().add(radius, radius, radius),
                min = block.getLocation().clone().subtract(radius, radius, radius);

        World world = block.getWorld();

        WorldEditSession editSession = new WorldEditSession(world);
        int toolDurability = getDurability(player, usedItem);
        boolean usingDurability = isUsingDurability();
        int toolUsages = 0;

        outerLoop:
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                for (int y = max.getBlockY(); y >= min.getBlockY(); y--) {
                    if (usingDurability && toolUsages >= toolDurability)
                        break outerLoop;

                    Block targetBlock = world.getBlockAt(x, y, z);

                    if (targetBlock.getType() != Material.ICE || !BukkitUtils.canBreakBlock(player, targetBlock, this) ||
                            !BukkitUtils.hasBreakAccess(targetBlock, player))
                        continue;

                    boolean result = editSession.setType(targetBlock.getLocation(), false,
                            vec -> targetBlock.setType(Material.WATER), WorldEditSession.SetBlockPriority.UPDATES);
                    if (result)
                        toolUsages++;
                }
            }
        }

        IceWandUseEvent iceWandUseEvent = new IceWandUseEvent(player, this, editSession.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(iceWandUseEvent);

        if (iceWandUseEvent.isCancelled())
            return true;

        editSession.apply();

        if (toolUsages > 0)
            reduceDurablility(player, usingDurability ? toolUsages : 1, usedItem);

        return true;
    }

}
