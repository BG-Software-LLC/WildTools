package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.SellInfo;
import com.bgsoftware.wildtools.api.hooks.SoldItem;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.items.ItemsDropper;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContainerProvider_Default implements ExtendedContainerProvider {

    private final WildToolsPlugin plugin;

    public ContainerProvider_Default(WildToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isContainer(BlockState blockState) {
        Material type = blockState.getType();
        return type == Material.CHEST || type == Material.TRAPPED_CHEST ||
                type.name().equals("BARREL") || type.name().contains("SHULKER_BOX");
    }

    @Override
    public SellInfo sellContainer(BlockState blockState, Inventory inventory, Player player) {
        Map<Integer, SoldItem> toSell = new HashMap<>();
        double totalEarnings = 0;

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && plugin.getProviders().canSellItem(player, itemStack)) {
                SoldItem soldItem = new SoldItem(itemStack, plugin.getProviders().getPrice(player, itemStack));
                toSell.put(slot, soldItem);
                totalEarnings += soldItem.getPrice();
            }
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
        ItemsDropper itemsDropper = new ItemsDropper();
        itemStackList.forEach(itemStack -> ItemUtils.addItem(itemStack, chestInventory, blockState.getLocation(), itemsDropper));
        itemsDropper.dropItems();
    }

}
