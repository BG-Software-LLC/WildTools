package com.bgsoftware.wildtools.api.objects;

import org.bukkit.inventory.ItemStack;
import com.bgsoftware.wildtools.api.objects.tools.Tool;

public interface ToolKind {
    String id();

    default int sortOrder() {
        return 0;
    }

    default boolean isSimilar(ItemStack stack, Tool tool) {
        return tool.isSimilar(stack);
    }
}
