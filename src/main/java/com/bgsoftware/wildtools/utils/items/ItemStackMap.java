package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.utils.Counter;
import com.google.common.collect.ForwardingMap;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ItemStackMap extends ForwardingMap<ItemStack, Counter> {

    private final Map<ItemStack, Counter> handle = new HashMap<>();

    @Override
    protected Map<ItemStack, Counter> delegate() {
        return this.handle;
    }

    @Override
    public Counter remove(Object object) {
        if (!(object instanceof ItemStack)) {
            return super.remove(object);
        }

        return super.remove(getItemStackAsKey((ItemStack) object));
    }

    @Override
    public Counter put(ItemStack key, Counter value) {
        return super.put(getItemStackAsKey(key), value);
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof ItemStack)) {
            return super.containsKey(key);
        }

        return super.containsKey(getItemStackAsKey((ItemStack) key));
    }

    @Override
    public Counter get(Object key) {
        if (!(key instanceof ItemStack)) {
            return super.get(key);
        }

        return super.get(getItemStackAsKey((ItemStack) key));
    }

    public void addItems(Collection<ItemStack> items) {
        items.forEach(item -> computeIfAbsent(item, i -> new Counter()).increase(item.getAmount()));
    }

    private static ItemStack getItemStackAsKey(ItemStack itemStack) {
        if (itemStack.getAmount() == 1)
            return itemStack;

        ItemStack itemKey = itemStack.clone();
        itemKey.setAmount(1);
        return itemKey;
    }

}
