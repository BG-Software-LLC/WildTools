package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;

public final class DropsProviders_Default implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if (!(block.getState() instanceof CreatureSpawner))
            return null;

        List<ItemStack> drops = new ArrayList<>();

        drops.add(getSpawnerItem((CreatureSpawner) block.getState()));

        return drops;
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }

    public static ItemStack getSpawnerItem(CreatureSpawner creatureSpawner) {
        ItemStack itemStack;

        try {
            itemStack = new ItemStack(Material.MOB_SPAWNER);
        } catch (Throwable ex) {
            itemStack = new ItemStack(Material.matchMaterial("SPAWNER"));
        }

        try {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
            blockStateMeta.setBlockState(creatureSpawner);
            itemStack.setItemMeta(blockStateMeta);
        } catch (Throwable ex) {
            //noinspection deprecation
            itemStack.setDurability(creatureSpawner.getSpawnedType().getTypeId());
        }

        return itemStack;
    }

}
