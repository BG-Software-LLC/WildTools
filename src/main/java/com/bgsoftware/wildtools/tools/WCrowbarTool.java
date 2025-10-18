package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.api.events.CrowbarWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.CrowbarTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.Materials;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WCrowbarTool extends WTool implements CrowbarTool {

    private final List<String> commandsOnUse;

    public WCrowbarTool(Material type, String name, List<String> commandsOnUse) {
        super(type, name, ToolMode.CROWBAR);
        this.commandsOnUse = commandsOnUse;
    }

    @Override
    public List<String> getCommandsOnUse() {
        return commandsOnUse;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        if (!Materials.isSpawner(e.getClickedBlock().getType()))
            return false;

        if (!BukkitUtils.canBreakBlock(e.getPlayer(), e.getClickedBlock(), this) ||
                !BukkitUtils.hasBreakAccess(e.getClickedBlock(), e.getPlayer()))
            return true;

        CreatureSpawner creatureSpawner = (CreatureSpawner) e.getClickedBlock().getState();

        List<ItemStack> itemsToDrop = new ArrayList<>();

        plugin.getProviders().getBlockDrops(itemsToDrop, e.getPlayer(), e.getClickedBlock(), true);

        boolean addedSilktouch = false;

        /* Many plugins require the players to have silk touch.
           Therefore, I add silk touch manually to avoid issues. */
        if (e.getItem().getEnchantmentLevel(Enchantment.SILK_TOUCH) < 1) {
            addedSilktouch = true;
            e.getItem().addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
        }

        WorldEditSession editSession = new WorldEditSession(e.getClickedBlock().getWorld());

        try {
            if (!BukkitUtils.breakBlock(e.getPlayer(), e.getClickedBlock(), e.getItem(), this, editSession, itemStack -> null))
                return true;
        } finally {
            if (addedSilktouch)
                e.getItem().removeEnchantment(Enchantment.SILK_TOUCH);
        }

        CrowbarWandUseEvent crowbarWandUseEvent = new CrowbarWandUseEvent(e.getPlayer(), this, e.getClickedBlock());
        Bukkit.getPluginManager().callEvent(crowbarWandUseEvent);

        if (crowbarWandUseEvent.isCancelled())
            return true;

        if (commandsOnUse.isEmpty()) {
            if (!itemsToDrop.isEmpty()) {
                ItemStack dropItem = itemsToDrop.get(0);
                if (isAutoCollect()) {
                    ItemUtils.addItem(dropItem, e.getPlayer().getInventory(), e.getClickedBlock().getLocation(), null);
                } else {
                    plugin.getProviders().getStackedItemProvider().dropItem(e.getClickedBlock().getLocation(), dropItem);
                }
            }
        } else {
            commandsOnUse.forEach(commandOnUse -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandOnUse
                    .replace("%player%", e.getPlayer().getName())
                    .replace("%entity%", creatureSpawner.getSpawnedType().name())
            ));
        }

        editSession.apply();

        reduceDurablility(e.getPlayer(), 1, e.getItem());

        return true;
    }
}
