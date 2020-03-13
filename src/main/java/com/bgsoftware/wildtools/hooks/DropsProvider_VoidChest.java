package com.bgsoftware.wildtools.hooks;

import me.shin1gamix.voidchest.VoidChestPlugin;
import me.shin1gamix.voidchest.configuration.FileManager;
import me.shin1gamix.voidchest.datastorage.VoidStorage;
import me.shin1gamix.voidchest.utilities.nbtapi.NBTItem;
import me.shin1gamix.voidchest.voidmanager.VoidItemManager;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_VoidChest implements DropsProvider {

    private VoidChestPlugin plugin = VoidChestPlugin.getInstance();

    @Override
    public List<ItemStack> getBlockDrops(Block block) {
        List<ItemStack> drops = new ArrayList<>();
        VoidStorage voidStorage = plugin.getVoidManager().getVoidStorage(block);

        if(voidStorage == null)
            return drops;

        VoidItemManager.getInstance().getCachedItem(voidStorage.getName()).ifPresent(cachedItem -> {
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

        return drops;
    }
}
