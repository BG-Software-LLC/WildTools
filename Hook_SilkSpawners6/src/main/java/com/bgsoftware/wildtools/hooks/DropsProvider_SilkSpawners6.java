package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import de.dustplanet.util.SilkUtil;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_SilkSpawners6 implements DropsProvider {

    private final SilkUtil silkUtil;

    public DropsProvider_SilkSpawners6(){
        silkUtil = SilkUtil.hookIntoSilkSpanwers();
    }

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        List<ItemStack> drops = new ArrayList<>();

        if(!(block.getState() instanceof CreatureSpawner))
            return drops;

        String entityId = silkUtil.getSpawnerEntityID(block);
        if(entityId == null)
            entityId = silkUtil.getDefaultEntityID();

        String mobName = silkUtil.getCreatureName(entityId).toLowerCase().replace(" ", "");

        drops.add(silkUtil.newSpawnerItem(entityId, silkUtil.getCustomSpawnerName(mobName), 1, false));

        return drops;
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }

}
