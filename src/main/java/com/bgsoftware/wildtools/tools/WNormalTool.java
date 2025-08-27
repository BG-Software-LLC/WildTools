package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.NormalTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.world.BlockMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class WNormalTool extends WTool implements NormalTool {
    public WNormalTool(Material type, String name) {
        super(type, name, ToolMode.NORMAL);
    }
    @Override
    public boolean onBlockBreak(BlockBreakEvent e) {
        ItemStack inHand = e.getPlayer().getItemInHand();
        Block targetBlock = e.getBlock();

        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            return false;
        }

        BlockMaterial firstBlockMaterial = BlockMaterial.of(targetBlock);

        if (!BukkitUtils.canBreakBlock(e.getPlayer(), targetBlock, firstBlockMaterial, this)) {
            e.setCancelled(true);
            return true;
        }

        return false;
    }
}
