package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.api.events.PillarWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.PillarTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

public class WPillarTool extends WTool implements PillarTool {

    public WPillarTool(Material type, String name) {
        super(type, name, ToolMode.PILLAR);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        int maxY = getPoint(e.getClickedBlock(), true), minY = getPoint(e.getClickedBlock(), false),
                x = e.getClickedBlock().getLocation().getBlockX(), z = e.getClickedBlock().getLocation().getBlockZ();

        Material firstType = e.getClickedBlock().getType();
        short firstData = e.getClickedBlock().getState().getData().toItemStack().getDurability();

        World world = e.getClickedBlock().getWorld();

        WorldEditSession editSession = new WorldEditSession(world);
        int toolDurability = getDurability(e.getPlayer(), e.getItem());
        boolean usingDurability = isUsingDurability();
        int toolUsages = 0;

        for (int y = maxY; y >= minY; y--) {
            if (usingDurability && toolUsages >= toolDurability)
                break;

            Block targetBlock = world.getBlockAt(x, y, z);

            if (!BukkitUtils.canBreakBlock(e.getPlayer(), targetBlock, firstType, firstData, this))
                continue;

            if (!BukkitUtils.breakBlock(e.getPlayer(), targetBlock, e.getItem(), this, editSession, null))
                break;

            toolUsages++;
        }

        PillarWandUseEvent pillarWandUseEvent = new PillarWandUseEvent(e.getPlayer(), this, editSession.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(pillarWandUseEvent);

        if (pillarWandUseEvent.isCancelled())
            return true;

        editSession.apply();

        if (toolUsages > 0)
            reduceDurablility(e.getPlayer(), usingDurability ? toolUsages : 1, e.getItem());

        return true;
    }

    private int getPoint(Block block, boolean max) {
        Location loc = block.getLocation().clone();
        boolean isSameBlock = true;

        //Find max block
        if (max) {
            while (isSameBlock) {
                loc.add(0, 1, 0);
                isSameBlock = canBreakBlock(block, loc.getBlock().getType(), loc.getBlock().getState().getData().toItemStack().getDurability()) && loc.getBlockY() <= 256;
            }
        }

        //Find min block
        else {
            while (isSameBlock) {
                loc = loc.clone().subtract(0, 1, 0);
                isSameBlock = canBreakBlock(block, loc.getBlock().getType(), loc.getBlock().getState().getData().toItemStack().getDurability()) && loc.getBlockY() >= 0;
            }
        }

        return loc.getBlockY();
    }

}
