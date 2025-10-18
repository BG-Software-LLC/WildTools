package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.events.BuilderWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.BuilderTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.Materials;
import com.bgsoftware.wildtools.utils.ServerVersion;
import com.bgsoftware.wildtools.utils.inventory.InventoryUtils;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import com.bgsoftware.wildtools.world.BlockMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WBuilderTool extends WTool implements BuilderTool {

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

        BlockMaterial firstBlockMaterial = BlockMaterial.of(e.getClickedBlock());

        if (!firstBlockMaterial.getType().isSolid())
            return false;

        BlockFace blockFace = e.getBlockFace();

        ItemStack blockItemStack;

        if (!ServerVersion.isLegacy()) {
            blockItemStack = new ItemStack(firstBlockMaterial.getType());
        } else {
            blockItemStack = e.getClickedBlock().getState().getData().toItemStack(1);
            Material itemType = blockItemStack.getType();
            short itemDurability = blockItemStack.getDurability();

            if (Materials.isSlab(itemType) && itemDurability >= 8)
                blockItemStack.setDurability((short) (itemDurability - 8));
            else if (itemType == Material.LOG && itemDurability >= 4) {
                blockItemStack.setDurability((short) (itemDurability % 4));
            } else if (itemType == Material.LOG_2 && itemDurability >= 2) {
                blockItemStack.setDurability((short) (itemDurability % 2));
            }
        }

        int amountOfBlocks = InventoryUtils.countItems(e.getPlayer().getInventory(), blockItemStack);

        WorldEditSession editSession = new WorldEditSession(e.getClickedBlock().getWorld());
        boolean usingDurability = isUsingDurability();
        int toolIterations = Math.min(usingDurability ? getDurability(e.getPlayer(), e.getItem()) : length, Math.min(amountOfBlocks, length));
        int iter;

        Block originalBlock = e.getClickedBlock();

        Block nextBlock = e.getClickedBlock();
        for (iter = 0; iter < toolIterations; iter++) {
            nextBlock = nextBlock.getRelative(blockFace);

            Material nextBlockType = nextBlock.getType();

            if (!Materials.isPlaceThroughBlock(nextBlockType) ||
                    !BukkitUtils.canBreakBlock(e.getPlayer(), nextBlock, firstBlockMaterial, this, false) ||
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

}
