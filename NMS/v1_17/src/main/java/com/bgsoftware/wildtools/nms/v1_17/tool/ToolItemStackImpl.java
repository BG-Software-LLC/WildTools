package com.bgsoftware.wildtools.nms.v1_17.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class ToolItemStackImpl extends com.bgsoftware.wildtools.nms.v1_17.tool.AbstractToolItemStack {

    public ToolItemStackImpl(ItemStack nmsItem) {
        super(nmsItem);
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
        return getTagInternal(compoundTag ->
                compoundTag.contains(key, 3) ? compoundTag.getInt(key) : def, def);
    }

    @Override
    public String getTag(String key, String def) {
        return getTagInternal(compoundTag ->
                compoundTag.contains(key, 8) ? compoundTag.getString(key) : def, def);
    }

    private <R> R getTagInternal(Function<CompoundTag, R> function, R def) {
        CompoundTag compoundTag = this.nmsItem.getTag();
        return compoundTag == null ? def : function.apply(compoundTag);
    }

}
