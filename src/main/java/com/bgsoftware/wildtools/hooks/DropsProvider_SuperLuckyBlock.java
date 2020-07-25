package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.sickskillz.superluckyblock.listeners.BlockBreakListener;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_SuperLuckyBlock implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        BlockBreakListener.handleLuckyBlock(player, block);
        return new ArrayList<>();
    }
}
