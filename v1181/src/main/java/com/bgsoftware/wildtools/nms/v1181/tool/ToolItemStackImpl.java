package com.bgsoftware.wildtools.nms.v1181.tool;

import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class ToolItemStackImpl extends ToolItemStack {

    private final ItemStack nmsItem;

    public ToolItemStackImpl(ItemStack nmsItem) {
        this.nmsItem = nmsItem;
        this.setItem(CraftItemStack.asCraftMirror(nmsItem));
    }

    @Override
    public ToolItemStack copy() {
        return new ToolItemStackImpl(nmsItem.copy());
    }

    @Override
    public void setTag(String key, int value) {
        this.nmsItem.getOrCreateTag().putInt(key, value);
    }

    @Override
    public void setTag(String key, String value) {
        this.nmsItem.getOrCreateTag().putString(key, value);
    }

    @Override
    public int getTag(String key, int def) {
        CompoundTag compoundTag = this.nmsItem.getTag();
        return compoundTag == null || !compoundTag.contains(key, 3) ? def : compoundTag.getInt(key);
    }

    @Override
    public String getTag(String key, String def) {
        CompoundTag compoundTag = this.nmsItem.getTag();
        return compoundTag == null || !compoundTag.contains(key, 8) ? def : compoundTag.getString(key);
    }

    @Override
    public void breakTool(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        serverPlayer.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        Item item = this.nmsItem.getItem();

        if (this.nmsItem.getCount() == 1)
            CraftEventFactory.callPlayerItemBreakEvent(serverPlayer, this.nmsItem);

        this.nmsItem.shrink(1);

        serverPlayer.awardStat(Stats.ITEM_BROKEN.get(item));

        this.nmsItem.setDamageValue(0);
    }

}
