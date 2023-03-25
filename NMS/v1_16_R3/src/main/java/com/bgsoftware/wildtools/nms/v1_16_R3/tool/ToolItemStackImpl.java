package com.bgsoftware.wildtools.nms.v1_16_R3.tool;

import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.Item;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.StatisticList;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class ToolItemStackImpl extends ToolItemStack {

    private final ItemStack nmsItem;

    public ToolItemStackImpl(ItemStack nmsItem) {
        this.nmsItem = nmsItem;
        this.setItem(CraftItemStack.asCraftMirror(nmsItem));
    }

    @Override
    public ToolItemStack copy() {
        return new ToolItemStackImpl(nmsItem.cloneItemStack());
    }

    @Override
    public void setTag(String key, int value) {
        this.nmsItem.getOrCreateTag().setInt(key, value);
    }

    @Override
    public void setTag(String key, String value) {
        this.nmsItem.getOrCreateTag().setString(key, value);
    }

    @Override
    public int getTag(String key, int def) {
        NBTTagCompound tagCompound = this.nmsItem.getTag();
        return tagCompound == null || !tagCompound.hasKeyOfType(key, 3) ? def : tagCompound.getInt(key);
    }

    @Override
    public String getTag(String key, String def) {
        NBTTagCompound tagCompound = this.nmsItem.getTag();
        return tagCompound == null || !tagCompound.hasKeyOfType(key, 8) ? def : tagCompound.getString(key);
    }

    @Override
    public void breakTool(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        entityPlayer.broadcastItemBreak(EnumItemSlot.MAINHAND);
        Item item = this.nmsItem.getItem();

        if (this.nmsItem.getCount() == 1)
            CraftEventFactory.callPlayerItemBreakEvent(entityPlayer, this.nmsItem);

        this.nmsItem.subtract(1);

        entityPlayer.b(StatisticList.ITEM_BROKEN.b(item));

        this.nmsItem.setDamage(0);
    }

}
