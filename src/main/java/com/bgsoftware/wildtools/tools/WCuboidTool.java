package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.api.events.CuboidWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolKind;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.CuboidTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import com.bgsoftware.wildtools.world.BlockMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class WCuboidTool extends WTool implements CuboidTool {

    private final int breakLevel;

    public WCuboidTool(Material type, String name, int breakLevel, ToolKind kind) {
        super(type, name, kind);
        this.breakLevel = breakLevel;
    }

    public int getBreakLevel() {
        return breakLevel;
    }

    @Override
    public boolean onBlockBreak(BlockBreakEvent e) {
        ItemStack inHand = e.getPlayer().getItemInHand();
        int radius = breakLevel / 2;

        Location max = e.getBlock().getLocation().add(radius, radius, radius),
                min = e.getBlock().getLocation().subtract(radius, radius, radius);

        BlockMaterial firstBlockMaterial = BlockMaterial.of(e.getBlock());

        WorldEditSession editSession = new WorldEditSession(e.getBlock().getWorld());
        int toolDurability = getDurability(e.getPlayer(), inHand);
        boolean usingDurability = isUsingDurability();
        int toolUsages = 0;

        outerLoop:
        for (int y = max.getBlockY(); y >= min.getBlockY(); y--) {
            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    if (usingDurability && toolUsages >= toolDurability)
                        break outerLoop;

                    Block targetBlock = e.getPlayer().getWorld().getBlockAt(x, y, z);

                    if (targetBlock.getType() == Material.AIR ||
                            !BukkitUtils.canBreakBlock(e.getPlayer(), targetBlock, firstBlockMaterial, this))
                        continue;

                    if (BukkitUtils.breakBlock(e.getPlayer(), targetBlock, inHand, this, editSession, null))
                        toolUsages++;
                }
            }
        }

        CuboidWandUseEvent cuboidWandUseEvent = new CuboidWandUseEvent(e.getPlayer(), this, editSession.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(cuboidWandUseEvent);

        if (cuboidWandUseEvent.isCancelled())
            return true;

        editSession.apply();

        if (toolUsages > 0)
            reduceDurablility(e.getPlayer(), usingDurability ? toolUsages : 1, inHand);

        return true;
    }
}
