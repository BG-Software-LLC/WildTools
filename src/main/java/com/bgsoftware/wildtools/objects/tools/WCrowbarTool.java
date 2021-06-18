package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.CrowbarWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.CrowbarTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WCrowbarTool extends WTool implements CrowbarTool {

    private final List<String> commandsOnUse;

    public WCrowbarTool(Material type, String name, List<String> commandsOnUse){
        super(type, name, ToolMode.CROWBAR);
        this.commandsOnUse = commandsOnUse;
    }

    @Override
    public List<String> getCommandsOnUse() {
        return commandsOnUse;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        if(!e.getClickedBlock().getType().name().contains("SPAWNER"))
            return false;

        if(!BukkitUtils.canBreakBlock(e.getPlayer(), e.getClickedBlock(), this))
            return true;

        CreatureSpawner creatureSpawner = (CreatureSpawner) e.getClickedBlock().getState();

        List<ItemStack> itemsToDrop = plugin.getProviders().getBlockDrops(e.getPlayer(), e.getClickedBlock(), true);

        boolean addedSilktouch = false;

        /* Many plugins require the players to have silk touch.
           Therefore, I add silk touch manually to avoid issues. */
        if(e.getItem().getEnchantmentLevel(Enchantment.SILK_TOUCH) < 1){
            addedSilktouch = true;
            e.getItem().addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
        }

        try {
            if (!BukkitUtils.breakBlock(e.getPlayer(), null, null, e.getClickedBlock(), e.getItem(), this, itemStack -> null))
                return true;
        }finally {
            if(addedSilktouch)
                e.getItem().removeEnchantment(Enchantment.SILK_TOUCH);
        }

        if(commandsOnUse.isEmpty()) {
            if(!itemsToDrop.isEmpty()) {
                ItemStack dropItem = itemsToDrop.get(0);
                if (isAutoCollect())
                    ItemUtils.addItem(dropItem, e.getPlayer().getInventory(), e.getClickedBlock().getLocation(), null);
                else
                    e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation(), dropItem);
            }
        }
        else{
            commandsOnUse.forEach(commandOnUse -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandOnUse
                    .replace("%player%", e.getPlayer().getName())
                    .replace("%entity%", creatureSpawner.getSpawnedType().name())
            ));
        }

        CrowbarWandUseEvent crowbarWandUseEvent = new CrowbarWandUseEvent(e.getPlayer(), this, e.getClickedBlock());
        Bukkit.getPluginManager().callEvent(crowbarWandUseEvent);

        reduceDurablility(e.getPlayer(), 1, e.getItem());

        return true;
    }
}
