package com.bgsoftware.wildtools.hooks;

import me.shin1gamix.voidchest.VoidChestPlugin;
import me.shin1gamix.voidchest.configuration.FileManager;
import me.shin1gamix.voidchest.data.PlayerData;
import me.shin1gamix.voidchest.data.PlayerDataManager;
import me.shin1gamix.voidchest.datastorage.VoidStorage;
import me.shin1gamix.voidchest.utilities.nbtapi.NBTItem;
import me.shin1gamix.voidchest.voidmanager.VoidItemManager;
import me.shin1gamix.voidchest.voidmanager.VoidStorageManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_VoidChest implements DropsProvider {

    private final VoidChestPlugin instance;

    public DropsProvider_VoidChest(){
        instance = VoidChestPlugin.getInstance();
    }

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        List<ItemStack> drops = new ArrayList<>();

        VoidStorageManager voidStorageManager = instance.getVoidChestPluginHandler().getVoidStorageManager();
        VoidItemManager voidItemManager = instance.getVoidChestPluginHandler().getVoidItemManager();
        VoidStorage voidStorage = voidStorageManager.getVoidStorage(block);

        if(voidStorage == null)
            return drops;

        voidItemManager.getCachedItem(voidStorage.getName()).ifPresent(cachedItem -> {
            ItemStack itemStack = cachedItem.getVoidChestItem();
            if(FileManager.getInstance().getOptions().getFileConfiguration().getBoolean("charge-break-persistent", false)) {
                Long chargeLeftSeconds = voidStorage.getVoidStorageCharge().getChargeLeftSeconds();
                if(chargeLeftSeconds != null && chargeLeftSeconds >= 1L) {
                    NBTItem nbtItem = NBTItem.of(itemStack);
                    nbtItem.setLong("voidCharge", voidStorage.getVoidStorageCharge().getChargeTime());
                    itemStack = nbtItem.getItem();
                }
            }

            drops.add(itemStack);
        });

        voidStorage.getInventoryHandler().closeInventories();
        voidStorage.getVoidStorageAbilities().setHologramActivated(false);
        voidStorage.getVoidStorageHologram().updateHologram();
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        playerData.removeVoidStorage(voidStorage);
        playerData.loadStatsToFile(false);

        return drops;
    }
}
