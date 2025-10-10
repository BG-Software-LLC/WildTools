package com.bgsoftware.wildtools.nms.v1_21_9.tool;

import com.bgsoftware.common.reflection.ReflectField;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Function;

public class ToolItemStackImpl extends com.bgsoftware.wildtools.nms.v1_21_9.tool.AbstractToolItemStack {

    private static final ReflectField<CompoundTag> CUSTOM_DATA_TAG = new ReflectField<>(CustomData.class,
            CompoundTag.class, Modifier.PRIVATE | Modifier.FINAL, 1);

    public ToolItemStackImpl(ItemStack nmsItem) {
        super(nmsItem);
    }

    @Override
    public void setTag(String key, int value) {
        setTagInternal(compoundTag -> compoundTag.putInt(key, value));
    }

    @Override
    public void setTag(String key, String value) {
        setTagInternal(compoundTag -> compoundTag.putString(key, value));
    }

    @Override
    public int getTag(String key, int def) {
        return getTagInternal(compoundTag -> compoundTag.getIntOr(key, def), def);
    }

    @Override
    public String getTag(String key, String def) {
        return getTagInternal(compoundTag -> compoundTag.getStringOr(key, def), def);
    }

    private void setTagInternal(Consumer<CompoundTag> consumer) {
        CustomData customData = this.nmsItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData = customData.update(consumer);
        this.nmsItem.set(DataComponents.CUSTOM_DATA, customData);
    }

    private <R> R getTagInternal(Function<CompoundTag, R> function, R def) {
        CustomData customData = this.nmsItem.get(DataComponents.CUSTOM_DATA);
        if(customData != null) {
            CompoundTag compoundTag = getCustomDataTag(customData);
            return function.apply(compoundTag);
        }
        return def;
    }

    private static CompoundTag getCustomDataTag(CustomData customData) {
        try {
            return customData.getUnsafe();
        } catch (Throwable error) {
            return CUSTOM_DATA_TAG.get(customData);
        }
    }

}
