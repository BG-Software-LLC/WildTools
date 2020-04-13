package com.bgsoftware.wildtools.hooks;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SuperAbilityType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.excavation.ExcavationManager;
import com.gmail.nossr50.skills.herbalism.HerbalismManager;
import com.gmail.nossr50.skills.mining.MiningManager;
import com.gmail.nossr50.skills.woodcutting.WoodcuttingManager;
import com.gmail.nossr50.util.BlockUtils;
import com.gmail.nossr50.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class McMMOHook {

    private static final boolean isEnabled = isSupportedBuild();

    public static void markAsPlaced(Location loc){
        if(isEnabled)
            com.gmail.nossr50.mcMMO.getPlaceStore().setTrue(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
    }

    public static void handleBlockBreak(Player player, ItemStack inHand, Block block){
        if(isEnabled){
            com.gmail.nossr50.datatypes.player.McMMOPlayer mcMMOPlayer = com.gmail.nossr50.util.player.UserManager.getPlayer(player);
            BlockState blockState = block.getState();
            if (BlockUtils.affectedByGreenTerra(block.getState())) {
                HerbalismManager herbalismManager = mcMMOPlayer.getHerbalismManager();
                if (herbalismManager.canActivateAbility()) {
                    mcMMOPlayer.checkAbilityActivation(PrimarySkillType.HERBALISM);
                }

                if (PrimarySkillType.HERBALISM.getPermissions(player)) {
                    herbalismManager.processHerbalismBlockBreakEvent(new BlockBreakEvent(block, player));
                }

            } else {
                if (BlockUtils.affectedBySuperBreaker(blockState) && ItemUtils.isPickaxe(inHand) &&
                        PrimarySkillType.MINING.getPermissions(player) && !mcMMO.getPlaceStore().isTrue(blockState)) {
                    MiningManager miningManager = mcMMOPlayer.getMiningManager();
                    miningManager.miningBlockCheck(blockState);
                } else if (BlockUtils.isLog(blockState) && ItemUtils.isAxe(inHand) && PrimarySkillType.WOODCUTTING.getPermissions(player) && !mcMMO.getPlaceStore().isTrue(blockState)) {
                    WoodcuttingManager woodcuttingManager = mcMMOPlayer.getWoodcuttingManager();
                    if (woodcuttingManager.canUseTreeFeller(inHand)) {
                        woodcuttingManager.processTreeFeller(blockState);
                    } else {
                        woodcuttingManager.woodcuttingBlockCheck(blockState);
                    }
                } else if (BlockUtils.affectedByGigaDrillBreaker(blockState) && ItemUtils.isShovel(inHand) &&
                        PrimarySkillType.EXCAVATION.getPermissions(player) && !mcMMO.getPlaceStore().isTrue(blockState)) {
                    ExcavationManager excavationManager = mcMMOPlayer.getExcavationManager();
                    excavationManager.excavationBlockCheck(blockState);
                    if (mcMMOPlayer.getAbilityMode(SuperAbilityType.GIGA_DRILL_BREAKER)) {
                        excavationManager.gigaDrillBreaker(blockState);
                    }
                }

                mcMMO.getPlaceStore().setFalse(blockState);
            }
        }
    }

    private static boolean isSupportedBuild(){
        try{
            Class.forName("com.gmail.nossr50.datatypes.skills.PrimarySkillType");
            return true;
        }catch(ClassNotFoundException ex){
            return false;
        }
    }

}
