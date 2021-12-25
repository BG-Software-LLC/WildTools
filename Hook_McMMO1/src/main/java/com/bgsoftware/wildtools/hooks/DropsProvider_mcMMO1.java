package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SecondaryAbility;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.skills.herbalism.HerbalismManager;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.PerksUtils;
import com.gmail.nossr50.util.skills.SkillUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class DropsProvider_mcMMO1 implements DropsProvider {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if (!ItemUtils.isCrops(block.getType()) || !shouldBonusDrops(player))
            return new ArrayList<>();

        return plugin.getNMSAdapter().getCropDrops(player, block).stream()
                .peek(itemStack -> itemStack.setAmount(itemStack.getAmount() * 2)).collect(Collectors.toList());
    }

    @Override
    public boolean isSpawnersOnly() {
        return false;
    }

    private boolean shouldBonusDrops(Player player) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        HerbalismManager herbalismManager = mcMMOPlayer.getHerbalismManager();
        int activationChance = PerksUtils.handleLuckyPerks(player, SkillType.HERBALISM);
        return SkillUtils.activationSuccessful(SecondaryAbility.HERBALISM_DOUBLE_DROPS, player, herbalismManager.getSkillLevel(), activationChance);
    }

}
