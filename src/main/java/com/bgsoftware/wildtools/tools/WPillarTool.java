package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.api.events.PillarWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.PillarTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.ServerVersion;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import com.bgsoftware.wildtools.world.BlockMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

public class WPillarTool extends WTool implements PillarTool {

    private static final int MIN_WORLD_HEIGHT = ServerVersion.isAtLeast(ServerVersion.v1_18) ? -64 : 0;

    public WPillarTool(Material type, String name) {
        super(type, name, ToolMode.PILLAR);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        int maxY = getPoint(e.getClickedBlock(), true), minY = getPoint(e.getClickedBlock(), false),
                x = e.getClickedBlock().getLocation().getBlockX(), z = e.getClickedBlock().getLocation().getBlockZ();

        BlockMaterial firstBlockMaterial = BlockMaterial.of(e.getClickedBlock());

        World world = e.getClickedBlock().getWorld();

        WorldEditSession editSession = new WorldEditSession(world);
        int toolDurability = getDurability(e.getPlayer(), e.getItem());
        boolean usingDurability = isUsingDurability();
        int toolUsages = 0;

        for (int y = maxY; y >= minY; y--) {
            if (usingDurability && toolUsages >= toolDurability)
                break;

            Block targetBlock = world.getBlockAt(x, y, z);

            if (!BukkitUtils.canBreakBlock(e.getPlayer(), targetBlock, firstBlockMaterial, this))
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
                if (loc.getBlockY() > block.getWorld().getMaxHeight()) {
                    isSameBlock = false;
                } else {
                    BlockMaterial blockMaterial = BlockMaterial.of(loc.getBlock());
                    isSameBlock = canBreakBlock(block, blockMaterial.getType(), blockMaterial.getData());
                }
            }
        }

        //Find min block
        else {
            while (isSameBlock) {
                loc = loc.subtract(0, 1, 0);
                if (loc.getBlockY() < MIN_WORLD_HEIGHT) {
                    isSameBlock = false;
                } else {
                    BlockMaterial blockMaterial = BlockMaterial.of(loc.getBlock());
                    isSameBlock = canBreakBlock(block, blockMaterial.getType(), blockMaterial.getData());
                }
            }
        }

        return loc.getBlockY();
    }

}
