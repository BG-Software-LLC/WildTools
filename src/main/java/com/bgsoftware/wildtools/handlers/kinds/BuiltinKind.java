package com.bgsoftware.wildtools.handlers.kinds;

import com.bgsoftware.wildtools.api.objects.ToolKind;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.inventory.ItemStack;

public final class BuiltinKind implements ToolKind {
    private final String id;
    private final int sort;

    public BuiltinKind(ToolMode mode, int sort) {
        this.id = mode.name();
        this.sort = sort;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public int sortOrder() {
        return sort;
    }

    @Override
    public boolean isSimilar(ItemStack stack, Tool tool) {
        return tool.isSimilar(stack);
    }
}
