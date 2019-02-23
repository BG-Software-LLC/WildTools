package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.utils.BukkitUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.bgsoftware.wildtools.Locale;
import xyz.wildseries.wildtools.api.objects.ToolMode;
import xyz.wildseries.wildtools.api.objects.tools.BuilderTool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WBuilderTool extends WTool implements BuilderTool {

    public static Map<UUID, BlockFace> blockFaces = new HashMap<>();

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
    public boolean onBlockInteract(PlayerInteractEvent e) {
        BlockFace blockFace = e.getBlockFace();

        Block nextBlock = e.getClickedBlock();
        for(int i = 0; i < length; i++){
            nextBlock = nextBlock.getRelative(blockFace);

            if(nextBlock.getType() != Material.AIR || !BukkitUtil.canBreak(e.getPlayer(), nextBlock))
                break;

            if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(e.getPlayer(), nextBlock.getLocation()))
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
