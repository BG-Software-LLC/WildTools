package com.bgsoftware.wildtools.api.objects;

import org.bukkit.inventory.ItemStack;
import com.bgsoftware.wildtools.api.objects.tools.Tool;

public interface ToolKind {
    String id();

    int sortOrder();

    boolean isSimilar(ItemStack stack, Tool tool);
}
