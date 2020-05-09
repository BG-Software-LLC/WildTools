package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.utils.Pair;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SkillType;
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
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("JavaReflectionMemberAccess")
public final class BlockActionProvider_mcMMO implements BlockActionProvider {

    private static final Map<Location, Pair<Player, ItemStack>> blockBreakTracking = new HashMap<>();
    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private static Method checkAbilityActivationMethod = null;
    private static Method herbalismBlockCheckMethod = null;
    private static boolean isEnabled = false;

    static {
        try{
            checkAbilityActivationMethod = McMMOPlayer.class.getMethod("checkAbilityActivation", SkillType.class);
            herbalismBlockCheckMethod = HerbalismManager.class.getMethod("herbalismBlockCheck", BlockState.class);
        }catch(Throwable ignored){}
    }

    public BlockActionProvider_mcMMO(){
        isEnabled = true;
        Bukkit.getPluginManager().registerEvents(new McMMOListener(), plugin);
    }

    public static void markAsPlaced(Location loc){
        if(isEnabled)
            com.gmail.nossr50.mcMMO.getPlaceStore().setTrue(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
    }

    @Override
    public void onBlockBreak(Player player, Block block, ItemStack usedItem){
        com.gmail.nossr50.datatypes.player.McMMOPlayer mcMMOPlayer = com.gmail.nossr50.util.player.UserManager.getPlayer(player);
        BlockState blockState = block.getState();
        blockBreakTracking.put(block.getLocation(), new Pair<>(player, usedItem));
        try {
            if (BlockUtils.affectedByGreenTerra(block.getState())) {
                HerbalismManager herbalismManager = mcMMOPlayer.getHerbalismManager();
                if (herbalismManager.canActivateAbility()) {
                    checkAbilityActivation(mcMMOPlayer);
                }

                if (getPermissions("HERBALISM", player)) {
                    herbalismBlockCheck(herbalismManager, block, player);
                }

            } else {
                if (BlockUtils.affectedBySuperBreaker(blockState) && ItemUtils.isPickaxe(usedItem) &&
                        getPermissions("MINING", player) && !mcMMO.getPlaceStore().isTrue(blockState)) {
                    MiningManager miningManager = mcMMOPlayer.getMiningManager();
                    miningManager.miningBlockCheck(blockState);
                } else if (BlockUtils.isLog(blockState) && ItemUtils.isAxe(usedItem) && getPermissions("WOODCUTTING", player) &&
                        !mcMMO.getPlaceStore().isTrue(blockState)) {
                    WoodcuttingManager woodcuttingManager = mcMMOPlayer.getWoodcuttingManager();
                    if (woodcuttingManager.canUseTreeFeller(usedItem)) {
                        woodcuttingManager.processTreeFeller(blockState);
                    } else {
                        woodcuttingManager.woodcuttingBlockCheck(blockState);
                    }
                } else if (BlockUtils.affectedByGigaDrillBreaker(blockState) && ItemUtils.isShovel(usedItem) &&
                        getPermissions("EXCAVATION", player) && !mcMMO.getPlaceStore().isTrue(blockState)) {
                    ExcavationManager excavationManager = mcMMOPlayer.getExcavationManager();
                    excavationManager.excavationBlockCheck(blockState);
                    if (mcMMOPlayer.getAbilityMode(SuperAbilityType.GIGA_DRILL_BREAKER)) {
                        excavationManager.gigaDrillBreaker(blockState);
                    }
                }

                mcMMO.getPlaceStore().setFalse(blockState);
            }
        } finally {
            blockBreakTracking.remove(block.getLocation());
        }
    }

    private static boolean getPermissions(String type, Player player){
        try{
            return PrimarySkillType.valueOf(type).getPermissions(player);
        }catch(Throwable ex){
            return SkillType.valueOf(type).getPermissions(player);
        }
    }

    private static void checkAbilityActivation(McMMOPlayer mcMMOPlayer){
        try{
            checkAbilityActivationMethod.invoke(mcMMOPlayer, SkillType.valueOf("HERBALISM"));
        }catch(Throwable ex){
            mcMMOPlayer.checkAbilityActivation(PrimarySkillType.valueOf("HERBALISM"));
        }
    }

    private static void herbalismBlockCheck(HerbalismManager herbalismManager, Block block, Player player){
        try{
            herbalismBlockCheckMethod.invoke(herbalismManager, block.getState());
        }catch(Throwable ex){
            herbalismManager.processHerbalismBlockBreakEvent(new BlockBreakEvent(block, player));
        }
    }

    @SuppressWarnings("unused")
    private static final class McMMOListener implements Listener{

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onItemSpawn(McMMOItemSpawnEvent e){
            Pair<Player, ItemStack> pair = blockBreakTracking.remove(e.getLocation());
            if(pair != null){
                Tool usedTool = plugin.getToolsManager().getTool(pair.getY());
                if(usedTool != null && usedTool.isAutoCollect()){
                    e.setCancelled(true);
                    pair.getX().getInventory().addItem(e.getItemStack());
                }
            }
        }

    }

}
