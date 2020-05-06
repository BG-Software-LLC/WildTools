package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.objects.tools.WSellTool;
import com.bgsoftware.wildtools.utils.container.SellInfo;
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
        return collectorHandler.getCollectorAtLocation(blockState.getLocation()) != null;
    }

    @Override
    public SellInfo sellContainer(BlockState blockState, Player player) {
        ChunkCollector chunkCollector = ChunkCollectorPlugin.getInstance().getCollectorHandler().getCollectorAtLocation(blockState.getLocation());
        Map<Integer, WSellTool.SoldItem> toSell = new HashMap<>();
        double totalEarnings = 0;

        CollectorInventory collectorInventory = chunkCollector.getInventory();
        Inventory inventory = collectorInventory.get();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            double price = itemStack == null ? 0 : collectorInventory.getPrice(itemStack.getType()) * chunkCollector.getItemCollection().get(itemStack.getType());
            if(price > 0){
                WSellTool.SoldItem soldItem = new WSellTool.SoldItem(itemStack, price);
                toSell.put(slot, soldItem);
                totalEarnings += price;
            }
        }

        return new SellInfo(toSell, totalEarnings);
    }

    @Override
    public void removeContainer(BlockState blockState, SellInfo sellInfo) {
        ChunkCollector chunkCollector = ChunkCollectorPlugin.getInstance().getCollectorHandler().getCollectorAtLocation(blockState.getLocation());

        for(WSellTool.SoldItem soldItem : sellInfo.getSoldItems().values()){
            chunkCollector.getItemCollection().put(soldItem.getItem().getType(), 0);
        }

        chunkCollector.update();
    }
}
