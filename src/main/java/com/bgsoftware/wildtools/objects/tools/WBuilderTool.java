package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.BuilderTool;

import java.util.UUID;

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
    @SuppressWarnings("all")
    public boolean canBreakBlock(Block block, Material firstType, short firstData) {
        if(hasBlacklistedMaterials() && isBlacklistedMaterial(firstType, firstData))
            return false;
        if(hasWhitelistedMaterials() && !isWhitelistedMaterial(firstType, firstData))
            return false;
        return true;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        BlockFace blockFace = e.getBlockFace();

        boolean reduceDurablity = false;
        Material firstType = e.getClickedBlock().getType();
        short firstData = e.getClickedBlock().getState().getData().toItemStack().getDurability();
        UUID taskId = ToolTaskManager.generateTaskId(e.getItem(), e.getPlayer().getInventory());

        Block nextBlock = e.getClickedBlock();
        for(int i = 0; i < length; i++){
            nextBlock = nextBlock.getRelative(blockFace);

            if(nextBlock.getType() != Material.AIR || !plugin.getProviders().canBreak(e.getPlayer(), nextBlock, firstType, firstData, this))
                break;

            ItemStack blockItemStack;

            try{
                //noinspection JavaReflectionMemberAccess
                BlockData blockData = (BlockData) Block.class.getMethod("getBlockData").invoke(e.getClickedBlock());
                blockItemStack = new ItemStack(blockData.getMaterial());
            }catch(Exception ex){
                blockItemStack = e.getClickedBlock().getState().getData().toItemStack(1);
            }

            if(!e.getPlayer().getInventory().containsAtLeast(blockItemStack, 1)){
                Locale.BUILDER_NO_BLOCK.send(e.getPlayer(), e.getClickedBlock().getType().name());
                break;
            }

            e.getPlayer().getInventory().removeItem(blockItemStack);
            plugin.getNMSAdapter().copyBlock(e.getClickedBlock(), nextBlock);

            if(isUsingDurability())
                reduceDurablility(e.getPlayer(), taskId);

            if(plugin.getNMSAdapter().getItemInHand(e.getPlayer()).getType() == Material.AIR)
                break;

            reduceDurablity = true;
        }

        if(reduceDurablity && !isUsingDurability())
            reduceDurablility(e.getPlayer(), taskId);

        ToolTaskManager.removeTask(taskId);

        return true;
    }
}
