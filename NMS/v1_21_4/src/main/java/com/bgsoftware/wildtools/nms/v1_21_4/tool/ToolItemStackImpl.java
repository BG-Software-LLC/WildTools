package com.bgsoftware.wildtools.nms.v1_21_4.tool;

import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
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
        CustomData customData = this.nmsItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        customData = customData.update(compoundTag ->
                compoundTag.putInt(key, value));

        this.nmsItem.set(DataComponents.CUSTOM_DATA, customData);
    }

    @Override
    public void setTag(String key, String value) {
        CustomData customData = this.nmsItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        customData = customData.update(compoundTag ->
                compoundTag.putString(key, value));

        this.nmsItem.set(DataComponents.CUSTOM_DATA, customData);
    }

    @Override
    public int getTag(String key, int def) {
        CustomData customData = this.nmsItem.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag compoundTag = customData.getUnsafe();
            if (compoundTag.contains(key, 3))
                return compoundTag.getInt(key);
        }
        return def;
    }

    @Override
    public String getTag(String key, String def) {
        CustomData customData = this.nmsItem.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag compoundTag = customData.getUnsafe();
            if (compoundTag.contains(key, 8))
                return compoundTag.getString(key);
        }
        return def;
    }

    @Override
    public void breakTool(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        Item item = this.nmsItem.getItem();

        if (this.nmsItem.getCount() == 1)
            CraftEventFactory.callPlayerItemBreakEvent(serverPlayer, this.nmsItem);

        this.nmsItem.shrink(1);

        serverPlayer.awardStat(Stats.ITEM_BROKEN.get(item));

        this.nmsItem.setDamageValue(0);
    }

}
