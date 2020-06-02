package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SuperAbilityType;
import com.gmail.nossr50.events.items.McMMOItemSpawnEvent;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.excavation.ExcavationManager;
import com.gmail.nossr50.skills.herbalism.HerbalismManager;
import com.gmail.nossr50.skills.mining.MiningManager;
import com.gmail.nossr50.skills.woodcutting.WoodcuttingManager;
import com.gmail.nossr50.util.BlockUtils;
import com.gmail.nossr50.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class BlockActionProvider_mcMMO implements BlockActionProvider {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public BlockActionProvider_mcMMO(){
        Bukkit.getPluginManager().registerEvents(new McMMOListener(), plugin);
    }

    @Override
    public void onBlockBreak(Player player, Block block, ItemStack usedItem){
        com.gmail.nossr50.datatypes.player.McMMOPlayer mcMMOPlayer = com.gmail.nossr50.util.player.UserManager.getPlayer(player);
        BlockState blockState = block.getState();
        try {
            if (BlockUtils.affectedByGreenTerra(block.getState())) {
                HerbalismManager herbalismManager = mcMMOPlayer.getHerbalismManager();
                if (herbalismManager.canActivateAbility()) {
                    mcMMOPlayer.checkAbilityActivation(PrimarySkillType.HERBALISM);
                }

                if (PrimarySkillType.HERBALISM.getPermissions(player)) {
                    herbalismManager.processHerbalismBlockBreakEvent(new BlockBreakEvent(block, player));
                }

            } else {
                if (BlockUtils.affectedBySuperBreaker(blockState) && ItemUtils.isPickaxe(usedItem) &&
                        PrimarySkillType.MINING.getPermissions(player) && !mcMMO.getPlaceStore().isTrue(blockState)) {
                    MiningManager miningManager = mcMMOPlayer.getMiningManager();
                    miningManager.miningBlockCheck(blockState);
                } else if (BlockUtils.isLog(blockState) && ItemUtils.isAxe(usedItem) &&
                        PrimarySkillType.WOODCUTTING.getPermissions(player) && !mcMMO.getPlaceStore().isTrue(blockState)) {
                    WoodcuttingManager woodcuttingManager = mcMMOPlayer.getWoodcuttingManager();
                    if (woodcuttingManager.canUseTreeFeller(usedItem)) {
                        woodcuttingManager.processTreeFeller(blockState);
                    } else {
                        woodcuttingManager.woodcuttingBlockCheck(blockState);
                    }
                } else if (BlockUtils.affectedByGigaDrillBreaker(blockState) && ItemUtils.isShovel(usedItem) &&
                        PrimarySkillType.EXCAVATION.getPermissions(player) && !mcMMO.getPlaceStore().isTrue(blockState)) {
                    ExcavationManager excavationManager = mcMMOPlayer.getExcavationManager();
                    excavationManager.excavationBlockCheck(blockState);
                    if (mcMMOPlayer.getAbilityMode(SuperAbilityType.GIGA_DRILL_BREAKER)) {
                        excavationManager.gigaDrillBreaker(blockState);
                    }
                }

                mcMMO.getPlaceStore().setFalse(blockState);
            }
        } finally {
            McMMOHook.doubleDropLocations.remove(block.getLocation());
        }
    }

    @SuppressWarnings("unused")
    private static final class McMMOListener implements Listener{

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onItemSpawn(McMMOItemSpawnEvent e){
            if(McMMOHook.doubleDropLocations.remove(e.getLocation()))
                e.setCancelled(true);
        }

    }

}