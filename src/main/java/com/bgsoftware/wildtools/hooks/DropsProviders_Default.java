package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import com.bgsoftware.wildtools.utils.Materials;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Collections;
import java.util.List;

public class DropsProviders_Default implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if (!(block.getState() instanceof CreatureSpawner))
            return null;

        ItemStack dropItem = getSpawnerItem((CreatureSpawner) block.getState());

        return Collections.singletonList(dropItem);
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }

    public static ItemStack getSpawnerItem(CreatureSpawner creatureSpawner) {
        ItemStack itemStack = new ItemStack(Materials.SPAWNER.toBukkitType());

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
