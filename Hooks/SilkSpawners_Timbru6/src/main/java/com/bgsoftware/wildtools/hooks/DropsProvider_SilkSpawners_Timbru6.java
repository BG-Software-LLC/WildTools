package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import de.dustplanet.util.SilkUtil;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DropsProvider_SilkSpawners_Timbru6 implements DropsProvider {

    private final SilkUtil silkUtil;

    public DropsProvider_SilkSpawners_Timbru6() {
        silkUtil = SilkUtil.hookIntoSilkSpanwers();
    }

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if (!(block.getState() instanceof CreatureSpawner))
            return null;

        String entityId = silkUtil.getSpawnerEntityID(block);
        if (entityId == null)
            entityId = silkUtil.getDefaultEntityID();

        String mobName = silkUtil.getCreatureName(entityId).toLowerCase().replace(" ", "");

        ItemStack dropItem = silkUtil.newSpawnerItem(entityId, silkUtil.getCustomSpawnerName(mobName), 1, false);

        return Collections.singletonList(dropItem);
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }

}
