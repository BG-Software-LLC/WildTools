package com.bgsoftware.wildtools.objects.tools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.BuilderTool;

public final class WBuilderTool extends WTool implements BuilderTool {

    private int length;

    public WBuilderTool(Material type, String name, int length){
        super(type, name, ToolMode.BUILDER);
        this.length = length;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean canBreakBlock(Block block, Material firstType, short firstData) {
        if(isOnlySameType() && (firstType != block.getType() || firstData != block.getData()))
            return false;
        if(hasBlacklistedMaterials() && isBlacklistedMaterial(block.getType(), block.getData()))
            return false;
        if(hasWhitelistedMaterials() && !isWhitelistedMaterial(block.getType(), block.getData()))
            return false;
        return true;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        BlockFace blockFace = e.getBlockFace();

        Block nextBlock = e.getClickedBlock();
        for(int i = 0; i < length; i++){
            nextBlock = nextBlock.getRelative(blockFace);

            if(nextBlock.getType() != Material.AIR || !plugin.getProviders().canBreak(e.getPlayer(), nextBlock, this))
                break;

            ItemStack blockItemStack = e.getClickedBlock().getState().getData().toItemStack(1);

            if(!e.getPlayer().getInventory().containsAtLeast(blockItemStack, 1)){
                Locale.BUILDER_NO_BLOCK.send(e.getPlayer(), e.getClickedBlock().getType().name());
                break;
            }

            e.getPlayer().getInventory().removeItem(blockItemStack);
            plugin.getNMSAdapter().copyBlock(e.getClickedBlock(), nextBlock);
        }

        return true;
    }
}
