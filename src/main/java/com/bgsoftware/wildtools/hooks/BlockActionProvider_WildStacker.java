package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BlockActionProvider_WildStacker implements BlockActionProvider {

    public static ItemStack getItemStack(Item item){
        return WildStackerAPI.getStackedItem(item).getItemStack();
    }

    public static void setItemStack(Item item, ItemStack itemStack){
        WildStackerAPI.getStackedItem(item).setStackAmount(itemStack.getAmount(), true);
    }

    @Override
    public void onBlockBreak(Player player, Block block, ItemStack usedItem) {
        if(block.getType().name().contains("SPAWNER"))
            WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawner(block.getLocation()).remove();
    }
}
