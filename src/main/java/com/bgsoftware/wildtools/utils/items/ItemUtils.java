package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.scheduler.Scheduler;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ItemUtils {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void addItem(ItemStack itemStack, Inventory inventory, Location location, @Nullable WorldEditSession editSession) {
        HashMap<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);
        if (location != null && !additionalItems.isEmpty()) {
            if (editSession != null) {
                List<ItemStack> drops = new LinkedList<>();
                additionalItems.forEach((i, drop) -> drops.add(drop));
                editSession.addDrops(drops);
            } else {
                ItemStackMap itemsToDrop = new ItemStackMap();
                itemsToDrop.addItems(additionalItems.values());
                Scheduler.runTask(location, () -> itemsToDrop.forEach((itemToDrop, count) -> plugin.getProviders()
                        .getStackedItemProvider().dropItem(location, itemToDrop, count.get())));
            }
        }
    }

    public static void formatItemStack(ToolItemStack toolItemStack) {
        Tool tool = toolItemStack.getTool();

        if (tool == null)
            return;

        ItemMeta meta = toolItemStack.getItemMeta();
        int usesLeft = toolItemStack.getUses();
        String ownerName = "None", ownerUUID = toolItemStack.getOwner();
        String enabled = Locale.HARVESTER_SELL_ENABLED.getMessage(), disabled = Locale.HARVESTER_SELL_DISABLED.getMessage();

        if (enabled == null) enabled = "";
        if (disabled == null) disabled = "";

        try {
            ownerName = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName();
        } catch (Exception ignored) {
        }

        if (meta.hasDisplayName()) {
            meta.setDisplayName(tool.getItemStack().getItemMeta().getDisplayName()
                    .replace("{}", usesLeft + "")
                    .replace("{owner}", ownerName)
                    .replace("{sell-mode}", toolItemStack.hasSellMode() ? enabled : disabled));
        }

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();

            for (String line : tool.getItemStack().getItemMeta().getLore())
                lore.add(line
                        .replace("{}", usesLeft + "")
                        .replace("{owner}", ownerName)
                        .replace("{sell-mode}", toolItemStack.hasSellMode() ? enabled : disabled));

            meta.setLore(lore);
        }

        toolItemStack.setItemMeta(meta);
    }

    public static void reduceDurability(ToolItemStack toolItemStack, Player player, int amount) {
        Tool tool = toolItemStack.getTool();

        if (tool == null)
            return;

        if (tool.isUnbreakable() || player.getGameMode() == GameMode.CREATIVE)
            return;

        ItemStack clonedTools = null;

        if (toolItemStack.getAmount() > 1) {
            clonedTools = toolItemStack.getItem().clone();
            clonedTools.setAmount(clonedTools.getAmount() - 1);
            toolItemStack.setAmount(1);
        }

        if (tool.isUsingDurability()) {
            int unbLevel = toolItemStack.getEnchantmentLevel(Enchantment.DURABILITY);

            // Durability Reduce Chance: (100/(Level+1))%
            if (unbLevel != 0) {
                int chance = new Random().nextInt(100);
                if (chance > (100 / (unbLevel + 1)))
                    return;
            }

            toolItemStack.setDurability((short) (toolItemStack.getDurability() + amount));

            if (toolItemStack.getDurability() > toolItemStack.getMaxDurability())
                toolItemStack.breakTool(player);
        } else {
            int usesLeft = toolItemStack.getUses();
            toolItemStack.setUses((usesLeft -= amount));

            if (usesLeft <= 0)
                toolItemStack.breakTool(player);

                //Update name and lore
            else {
                // Update durability depends on the uses
                if (tool.isUsesProgress()) {
                    float usesPercentage = (float) usesLeft / tool.getDefaultUses();
                    toolItemStack.setDurability((short) Math.round((1 - usesPercentage) * toolItemStack.getMaxDurability()));
                }

                if (toolItemStack.hasItemMeta())
                    ItemUtils.formatItemStack(toolItemStack);
            }
        }

        if (clonedTools != null)
            ItemUtils.addItem(clonedTools, player.getInventory(), player.getLocation(), null);
    }

    public static int getDurability(Player player, ToolItemStack toolItemStack) {
        Tool tool = toolItemStack.getTool();

        boolean unbreakable = tool != null && tool.isUnbreakable();
        boolean usingDurability = tool == null || tool.isUsingDurability();

        if (unbreakable || player.getGameMode() == GameMode.CREATIVE)
            return Integer.MAX_VALUE;

        return usingDurability ? toolItemStack.getMaxDurability() - toolItemStack.getDurability() + 1 : toolItemStack.getUses();
    }

}
