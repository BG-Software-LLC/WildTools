package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.BuilderWandUseEvent;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.inventory.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.BuilderTool;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public final class WBuilderTool extends WTool implements BuilderTool {

    private static final List<String> NON_TRANSPARENT_BLOCKS = Arrays.asList("WEB", "COBWEB");
    private static Method GET_BLOCK_DATA = null;

    static {
        try {
            //noinspection JavaReflectionMemberAccess
            GET_BLOCK_DATA = Block.class.getMethod("getBlockData");
        }catch (Throwable ignored){}
    }

    private final int length;

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
        e.setCancelled(true);

        if(!e.getClickedBlock().getType().isSolid())
            return false;

        BlockFace blockFace = e.getBlockFace();

        ItemStack blockItemStack = null;

        if(GET_BLOCK_DATA != null){
            try {
                BlockData blockData = (BlockData) GET_BLOCK_DATA.invoke(e.getClickedBlock());
                blockItemStack = new ItemStack(blockData.getMaterial());
            }catch (Exception ignored){}
        }

        if(blockItemStack == null){
            blockItemStack = e.getClickedBlock().getState().getData().toItemStack(1);
            if((blockItemStack.getType().name().contains("STEP") || blockItemStack.getType().name().contains("SLAB")) &&
                    blockItemStack.getDurability() >= 8)
                blockItemStack.setDurability((short) (blockItemStack.getDurability() - 8));
            else if(blockItemStack.getType() == Material.LOG && blockItemStack.getDurability() >= 4) {
                blockItemStack.setDurability((short) (blockItemStack.getDurability() % 4));
            }
            else if(blockItemStack.getType() == Material.LOG_2 && blockItemStack.getDurability() >= 2){
                blockItemStack.setDurability((short) (blockItemStack.getDurability() % 2));
            }
        }

        int amountOfBlocks = InventoryUtils.countItems(e.getPlayer().getInventory(), blockItemStack);

        Material firstType = e.getClickedBlock().getType();
        short firstData = e.getClickedBlock().getState().getData().toItemStack().getDurability();

        BlocksController blocksController = new BlocksController();
        boolean usingDurability = isUsingDurability();
        int toolIterations = Math.min(usingDurability ? getDurability(e.getPlayer(), e.getItem()) : length, Math.min(amountOfBlocks, length));
        int iter;

        Block originalBlock = e.getClickedBlock();

        Block nextBlock = e.getClickedBlock();
        for(iter = 0; iter < toolIterations; iter++){
            nextBlock = nextBlock.getRelative(blockFace);

            Material nextBlockType = nextBlock.getType();

            if(nextBlockType.isSolid() || NON_TRANSPARENT_BLOCKS.contains(nextBlockType.name()) ||
                    !BukkitUtils.canBreakBlock(nextBlock, firstType, firstData, this) ||
                    !BukkitUtils.placeBlock(e.getPlayer(), blocksController, nextBlock, originalBlock))
                break;
        }

        BuilderWandUseEvent builderWandUseEvent = new BuilderWandUseEvent(e.getPlayer(), this, blocksController.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(builderWandUseEvent);

        blocksController.updateSession();

        blockItemStack.setAmount(iter);
        InventoryUtils.removeItem(e.getPlayer().getInventory(), blockItemStack);

        if(amountOfBlocks < length)
            Locale.BUILDER_NO_BLOCK.send(e.getPlayer(), e.getClickedBlock().getType().name());

        if(iter > 0)
            reduceDurablility(e.getPlayer(), usingDurability ? iter : 1, e.getItem());

        return true;
    }

}
