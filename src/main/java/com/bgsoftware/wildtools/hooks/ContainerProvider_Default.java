package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.SellInfo;
import com.bgsoftware.wildtools.api.hooks.SoldItem;
import com.bgsoftware.wildtools.utils.Materials;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerProvider_Default implements ExtendedContainerProvider {

    @Nullable
    private static final Material BARREL = Materials.getSafeMaterial("BARREL").orElse(null);
    @Nullable
    private static final Material SHULKER_BOX = Materials.getSafeMaterial("SHULKER_BOX").orElse(null);

    private final WildToolsPlugin plugin;

    public ContainerProvider_Default(WildToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isContainer(BlockState blockState) {
        Material type = blockState.getType();
        return type == Material.CHEST || type == Material.TRAPPED_CHEST || type == BARREL || type == SHULKER_BOX;
    }

    @Override
    public SellInfo sellContainer(BlockState blockState, Inventory inventory, Player player) {
        Map<Integer, SoldItem> toSell = new HashMap<>();
        double totalEarnings = 0;

        try {
            this.plugin.getProviders().startBulkSell();

            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack itemStack = inventory.getItem(slot);
                if (itemStack != null) {
                    SoldItem soldItem = new SoldItem(itemStack, plugin.getProviders().getPrice(player, itemStack));
                    if (soldItem.isSellable()) {
                        toSell.put(slot, soldItem);
                        totalEarnings += soldItem.getPrice();
                    }
                }

            }
        } finally {
            this.plugin.getProviders().stopBulkSell();
        }

        return new SellInfo(toSell, totalEarnings);
    }

    @Override
    public void removeContainer(BlockState blockState, Inventory inventory, SellInfo sellInfo) {
        sellInfo.getSoldItems().keySet().forEach(slot -> inventory.setItem(slot, new ItemStack(Material.AIR)));
    }

    @Override
    public List<Inventory> getAllInventories(BlockState blockState, Inventory chestInventory) {
        return Collections.singletonList(chestInventory);
    }

    @Override
    public void addItems(BlockState blockState, Inventory chestInventory, List<ItemStack> itemStackList) {
        itemStackList.forEach(itemStack -> ItemUtils.addItem(itemStack, chestInventory, blockState.getLocation(), null));
    }

}
