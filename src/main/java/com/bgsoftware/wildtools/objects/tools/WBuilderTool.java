package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.BuilderWandUseEvent;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.inventory.InventoryUtils;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        UUID taskId = ToolTaskManager.generateTaskId(e.getItem(), e.getPlayer().getInventory());
        BlockFace blockFace = e.getBlockFace();

        ItemStack blockItemStack;

        try{
            //noinspection JavaReflectionMemberAccess
            BlockData blockData = (BlockData) Block.class.getMethod("getBlockData").invoke(e.getClickedBlock());
            blockItemStack = new ItemStack(blockData.getMaterial());
        }catch(Exception ex){
            blockItemStack = e.getClickedBlock().getState().getData().toItemStack(1);
        }

        int amountOfBlocks = InventoryUtils.countItems(e.getPlayer().getInventory(), blockItemStack);

        Material firstType = e.getClickedBlock().getType();
        short firstData = e.getClickedBlock().getState().getData().toItemStack().getDurability();

        BlocksController blocksController = new BlocksController(e.getPlayer());
        boolean usingDurability = isUsingDurability();
        int toolIterations = Math.min(usingDurability ? getDurability(e.getPlayer(), taskId) : length, Math.min(amountOfBlocks, length));
        int iter;

        Location block = e.getClickedBlock().getLocation();

        Block nextBlock = e.getClickedBlock();
        for(iter = 0; iter < toolIterations; iter++){
            nextBlock = nextBlock.getRelative(blockFace);

            if(nextBlock.getType() != Material.AIR || !plugin.getProviders().canBreak(e.getPlayer(), nextBlock, firstType, firstData, this))
                break;

            blocksController.setType(nextBlock.getLocation(), block);
        }

        BuilderWandUseEvent builderWandUseEvent = new BuilderWandUseEvent(e.getPlayer(), this, blocksController.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(builderWandUseEvent);

        blocksController.updateSession();

        blockItemStack.setAmount(iter);
        e.getPlayer().getInventory().removeItem(blockItemStack);

        if(amountOfBlocks < length)
            Locale.BUILDER_NO_BLOCK.send(e.getPlayer(), e.getClickedBlock().getType().name());

        if(iter > 0)
            reduceDurablility(e.getPlayer(), usingDurability ? iter : 1, taskId);

        ToolTaskManager.removeTask(taskId);

        return true;
    }
}
