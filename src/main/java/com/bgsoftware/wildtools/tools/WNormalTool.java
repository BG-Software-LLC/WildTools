package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.api.events.NormalWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.NormalTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.world.BlockMaterial;
import org.bukkit.Bukkit;
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

        NormalWandUseEvent normalWandUseEvent = new NormalWandUseEvent(e.getPlayer(), this, targetBlock.getLocation());
        Bukkit.getPluginManager().callEvent(normalWandUseEvent);

        if (normalWandUseEvent.isCancelled()) {
            e.setCancelled(true);
            return true;
        }

        boolean usingDurability = isUsingDurability();
        if (usingDurability) {
            int toolDurability = getDurability(e.getPlayer(), inHand);
            if (toolDurability <= 0) {
                e.setCancelled(true);
                return true;
            }
            reduceDurablility(e.getPlayer(), 1, inHand);
        }


        return false;
    }
}
