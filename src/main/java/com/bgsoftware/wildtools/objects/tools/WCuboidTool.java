package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.CuboidWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.CuboidTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.items.ItemsDropper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class WCuboidTool extends WTool implements CuboidTool {

    private final int breakLevel;

    public WCuboidTool(Material type, String name, int breakLevel) {
        super(type, name, ToolMode.CUBOID);
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

        Material firstType = e.getBlock().getType();
        short firstData = e.getBlock().getState().getData().toItemStack().getDurability();

        BlocksController blocksController = new BlocksController();
        ItemsDropper itemsDropper = new ItemsDropper();
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
                            !BukkitUtils.canBreakBlock(e.getPlayer(), targetBlock, firstType, firstData, this))
                        continue;

                    if (BukkitUtils.breakBlock(e.getPlayer(), blocksController, itemsDropper, targetBlock, inHand, this, itemStack -> itemStack))
                        toolUsages++;
                }
            }
        }

        CuboidWandUseEvent cuboidWandUseEvent = new CuboidWandUseEvent(e.getPlayer(), this, blocksController.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(cuboidWandUseEvent);

        blocksController.updateSession();
        itemsDropper.dropItems();

        if (toolUsages > 0)
            reduceDurablility(e.getPlayer(), usingDurability ? toolUsages : 1, inHand);

        return true;
    }
}
