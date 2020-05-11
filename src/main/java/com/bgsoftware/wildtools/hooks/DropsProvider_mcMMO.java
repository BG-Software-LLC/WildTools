package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SecondaryAbility;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.skills.herbalism.HerbalismManager;
import com.gmail.nossr50.util.BlockUtils;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.PerksUtils;
import com.gmail.nossr50.util.skills.SkillUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("JavaReflectionMemberAccess")
public final class DropsProvider_mcMMO implements DropsProvider {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private static Method handleLuckyPerksMethod,
            activationSuccessfulMethod;

    static {
        try{
            handleLuckyPerksMethod = PerksUtils.class.getMethod("handleLuckyPerks", Player.class, SkillType.class);
            activationSuccessfulMethod = SkillUtils.class.getMethod("activationSuccessful", SecondaryAbility.class, Player.class, int.class, int.class);
        }catch(Throwable ignored){}
    }

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if(!ItemUtils.isCrops(block.getType()) || !shouldBonusDrops(player, block))
            return new ArrayList<>();

        BlockActionProvider_mcMMO.doubleDropLocations.add(block.getLocation());

        return plugin.getNMSAdapter().getCropDrops(player, block).stream()
                .peek(itemStack -> itemStack.setAmount(itemStack.getAmount() * 2)).collect(Collectors.toList());
    }

    private boolean shouldBonusDrops(Player player, Block block){
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        HerbalismManager herbalismManager = mcMMOPlayer.getHerbalismManager();
        try{
            int activationChance = (int) handleLuckyPerksMethod.invoke(null, player, SkillType.HERBALISM);
            return (boolean) activationSuccessfulMethod.invoke(null, SecondaryAbility.HERBALISM_DOUBLE_DROPS, player, herbalismManager.getSkillLevel(), activationChance);
        }catch(Throwable ex){
            return BlockUtils.checkDoubleDrops(player, block.getState(), PrimarySkillType.HERBALISM, SubSkillType.HERBALISM_DOUBLE_DROPS);
        }
    }

}
