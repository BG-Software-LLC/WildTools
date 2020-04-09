package com.bgsoftware.wildtools.hooks;

import de.dustplanet.util.SilkUtil;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_SilkSpawners implements DropsProvider {

    private SilkUtil silkUtil;

    public DropsProvider_SilkSpawners(){
        silkUtil = SilkUtil.hookIntoSilkSpanwers();
    }

    @Override
    public List<ItemStack> getBlockDrops(Block block) {
        List<ItemStack> drops = new ArrayList<>();

        if(!(block.getState() instanceof CreatureSpawner))
            return drops;

        short entityId = silkUtil.getSpawnerEntityID(block);
        String mobName = silkUtil.getCreatureName(entityId).toLowerCase().replace(" ", "");

        drops.add(silkUtil.newSpawnerItem(entityId, silkUtil.getCustomSpawnerName(mobName), 1, false));

        return drops;
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }

    @Override
    public boolean callEvent() {
        return false;
    }
}
