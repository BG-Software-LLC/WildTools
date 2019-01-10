package xyz.wildseries.wildtools.objects.tools;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.objects.ToolMode;
import xyz.wildseries.wildtools.api.objects.tools.SortTool;
import xyz.wildseries.wildtools.utils.BukkitUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WSortTool extends WTool implements SortTool {

    public WSortTool(Material type, String name){
        super(type, name, ToolMode.SORT);
    }

    @Override
    public void useOnBlock(Player pl, Block block) {
        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> useOnBlock(pl, block));
            return;
        }

        if(block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST){
            Locale.INVALID_CONTAINER_SORT_WAND.send(pl);
            return;
        }

        if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(pl, block.getLocation()))
            return;

        WTool.toolBlockBreak.add(pl.getUniqueId());

        if(!BukkitUtil.canInteract(pl, block)) {
            WTool.toolBlockBreak.remove(pl.getUniqueId());
            return;
        }

        WTool.toolBlockBreak.remove(pl.getUniqueId());

        if(!canUse(pl.getUniqueId())){
            Locale.COOLDOWN_TIME.send(pl, getTime(getTimeLeft(pl.getUniqueId())));
            return;
        }

        setLastUse(pl.getUniqueId());

        List<InventoryItem> inventoryItems = new ArrayList<>();

        Inventory inventory = ((InventoryHolder) block.getState()).getInventory();

        Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .forEach(itemStack -> inventoryItems.add(new InventoryItem(itemStack)));

        inventory.clear();

        Collections.sort(inventoryItems);

        inventoryItems.forEach(inventoryItem -> inventory.addItem(inventoryItem.itemStack));

        if(inventoryItems.size() > 1 && pl.getGameMode() != GameMode.CREATIVE && !isUnbreakable())
            reduceDurablility(pl);

        Locale.SORTED_CHEST.send(pl);
    }

    private class InventoryItem implements Comparable<InventoryItem>{

        public ItemStack itemStack;

        InventoryItem(ItemStack itemStack){
            this.itemStack = itemStack;
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public int compareTo(InventoryItem o) {
            //Comparing itemstack types
            if(itemStack.getType().ordinal() > o.itemStack.getType().ordinal())
                return 1;
            else if(itemStack.getType().ordinal() < o.itemStack.getType().ordinal())
                return -1;

            //Comparing durabilities
            return Integer.compare(itemStack.getDurability(), o.itemStack.getDurability());
        }
    }

}
