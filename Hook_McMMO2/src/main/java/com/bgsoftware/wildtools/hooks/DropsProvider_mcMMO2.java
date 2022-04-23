package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.util.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class DropsProvider_mcMMO2 implements DropsProvider {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if(!ItemUtils.isCrops(block.getType()) || !shouldBonusDrops(player, block))
            return null;

        return plugin.getNMSAdapter().getCropDrops(player, block).stream()
                .peek(itemStack -> itemStack.setAmount(itemStack.getAmount() * 2)).collect(Collectors.toList());
    }

    @Override
    public boolean isSpawnersOnly() {
        return false;
    }

    private boolean shouldBonusDrops(Player player, Block block){
        return BlockUtils.checkDoubleDrops(player, block.getState(), PrimarySkillType.HERBALISM, SubSkillType.HERBALISM_DOUBLE_DROPS);
    }

}
