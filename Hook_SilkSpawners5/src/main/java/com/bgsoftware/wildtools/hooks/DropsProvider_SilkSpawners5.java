package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import de.dustplanet.util.SilkUtil;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_SilkSpawners5 implements DropsProvider {

    private final SilkUtil silkUtil;

    public DropsProvider_SilkSpawners5(){
        silkUtil = SilkUtil.hookIntoSilkSpanwers();
    }

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        List<ItemStack> drops = new ArrayList<>();

        if(!(block.getState() instanceof CreatureSpawner))
            return drops;

        silkUtil.getSpawnerEntityID(block);

        short entityId = silkUtil.getSpawnerEntityID(block);
        if(entityId == 0)
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
