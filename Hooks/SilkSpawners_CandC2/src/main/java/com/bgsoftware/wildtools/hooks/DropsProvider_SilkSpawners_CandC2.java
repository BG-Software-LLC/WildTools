package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import de.corneliusmay.silkspawners.plugin.SilkSpawners;
import de.corneliusmay.silkspawners.plugin.spawner.Spawner;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class DropsProvider_SilkSpawners_CandC2 implements DropsProvider {

    private final SilkSpawners silkSpawners;

    public DropsProvider_SilkSpawners_CandC2() {
        silkSpawners = JavaPlugin.getPlugin(SilkSpawners.class);
    }

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        Spawner spawner = new Spawner(this.silkSpawners, block);
        if (!spawner.isValid())
            return Collections.emptyList();

        return Collections.singletonList(spawner.getItemStack());
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }

}
