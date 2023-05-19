package com.bgsoftware.wildtools.tools;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.events.BuilderWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.BuilderTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.Materials;
import com.bgsoftware.wildtools.utils.inventory.InventoryUtils;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import com.destroystokyo.paper.util.set.OptimizedSmallEnumSet;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class WBuilderTool extends WTool implements BuilderTool {


    private static final ReflectMethod<Object> GET_BLOCK_DATA = new ReflectMethod<>(Block.class, "getBlockData");

    private final int length;

    public WBuilderTool(Material type, String name, int length) {
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
        if (hasBlacklistedMaterials() && isBlacklistedMaterial(firstType, firstData))
            return false;
        if (hasWhitelistedMaterials() && !isWhitelistedMaterial(firstType, firstData))
            return false;
        return true;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        e.setCancelled(true);

        Material firstType = e.getClickedBlock().getType();

        if (!firstType.isSolid())
            return false;

        BlockFace blockFace = e.getBlockFace();

        ItemStack blockItemStack = null;

        if (GET_BLOCK_DATA.isValid()) {
            BlockData blockData = (BlockData) GET_BLOCK_DATA.invoke(e.getClickedBlock());
            blockItemStack = new ItemStack(blockData.getMaterial());
        }

        if (blockItemStack == null) {
            blockItemStack = e.getClickedBlock().getState().getData().toItemStack(1);
            if ((blockItemStack.getType().name().contains("STEP") || blockItemStack.getType().name().contains("SLAB")) &&
                    blockItemStack.getDurability() >= 8)
                blockItemStack.setDurability((short) (blockItemStack.getDurability() - 8));
            else if (blockItemStack.getType() == Material.LOG && blockItemStack.getDurability() >= 4) {
                blockItemStack.setDurability((short) (blockItemStack.getDurability() % 4));
            } else if (blockItemStack.getType() == Material.LOG_2 && blockItemStack.getDurability() >= 2) {
                blockItemStack.setDurability((short) (blockItemStack.getDurability() % 2));
            }
        }

        int amountOfBlocks = InventoryUtils.countItems(e.getPlayer().getInventory(), blockItemStack);

        short firstData = e.getClickedBlock().getState().getData().toItemStack().getDurability();

        WorldEditSession editSession = new WorldEditSession(e.getClickedBlock().getWorld());
        boolean usingDurability = isUsingDurability();
        int toolIterations = Math.min(usingDurability ? getDurability(e.getPlayer(), e.getItem()) : length, Math.min(amountOfBlocks, length));
        int iter;

        Block originalBlock = e.getClickedBlock();

        Block nextBlock = e.getClickedBlock();
        for (iter = 0; iter < toolIterations; iter++) {
            nextBlock = nextBlock.getRelative(blockFace);

            Material nextBlockType = nextBlock.getType();

            if (!canPlaceThroughBlock(nextBlockType) ||
                    !BukkitUtils.canBreakBlock(e.getPlayer(), nextBlock, firstType, firstData, this, false) ||
                    !BukkitUtils.placeBlock(e.getPlayer(), nextBlock, originalBlock, editSession)) {
                break;
            }
        }

        BuilderWandUseEvent builderWandUseEvent = new BuilderWandUseEvent(e.getPlayer(), this, editSession.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(builderWandUseEvent);

        if (builderWandUseEvent.isCancelled())
            return true;

        editSession.apply();

        blockItemStack.setAmount(iter);
        InventoryUtils.removeItem(e.getPlayer().getInventory(), blockItemStack);

        if (amountOfBlocks < length)
            Locale.BUILDER_NO_BLOCK.send(e.getPlayer(), e.getClickedBlock().getType().name());

        if (iter > 0)
            reduceDurablility(e.getPlayer(), usingDurability ? iter : 1, e.getItem());

        return true;
    }

    private static boolean canPlaceThroughBlock(Material type) {
        return !type.isSolid() && type != Materials.COBWEB.parseMaterial() && !ItemUtils.isCrops(type);
    }

}
