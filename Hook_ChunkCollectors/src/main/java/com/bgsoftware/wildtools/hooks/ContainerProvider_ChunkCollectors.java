package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ContainerProvider;
import com.bgsoftware.wildtools.api.hooks.SellInfo;
import com.bgsoftware.wildtools.api.hooks.SoldItem;
import com.cloth.ChunkCollectorPlugin;
import com.cloth.collectors.ChunkCollector;
import com.cloth.collectors.CollectorHandler;
import com.cloth.objects.CollectorInventory;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class ContainerProvider_ChunkCollectors implements ContainerProvider {

    @Override
    public boolean isContainer(BlockState blockState) {
        CollectorHandler collectorHandler = ChunkCollectorPlugin.getInstance().getCollectorHandler();
        ChunkCollector chunkCollector = collectorHandler.getCollectorAtLocation(blockState.getLocation());
        return chunkCollector != null && chunkCollector.getLocation().equals(blockState.getLocation());
    }

    @Override
    public SellInfo sellContainer(BlockState blockState, Inventory inventory, Player player) {
        ChunkCollector chunkCollector = ChunkCollectorPlugin.getInstance().getCollectorHandler().getCollectorAtLocation(blockState.getLocation());
        Map<Integer, SoldItem> toSell = new HashMap<>();
        double totalEarnings = 0;

        CollectorInventory collectorInventory = chunkCollector.getInventory();
        inventory = collectorInventory.get();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            double price = itemStack == null || !collectorInventory.isCollecting(itemStack.getType()) ? 0 :
                    collectorInventory.getPrice(itemStack.getType()) * chunkCollector.getItemCollection().get(itemStack.getType());
            if (price > 0) {
                SoldItem soldItem = new SoldItem(itemStack, price);
                toSell.put(slot, soldItem);
                totalEarnings += price;
            }
        }

        return new SellInfo(toSell, totalEarnings);
    }

    @Override
    public void removeContainer(BlockState blockState, Inventory inventory, SellInfo sellInfo) {
        ChunkCollector chunkCollector = ChunkCollectorPlugin.getInstance().getCollectorHandler().getCollectorAtLocation(blockState.getLocation());

        for (SoldItem soldItem : sellInfo.getSoldItems().values()) {
            chunkCollector.getItemCollection().put(soldItem.getItem().getType(), 0);
        }

        chunkCollector.update(true);
    }

}
