package com.bgsoftware.wildtools.api.objects;

import org.bukkit.Material;
import com.bgsoftware.wildtools.api.objects.tools.Tool;

@FunctionalInterface
public interface ToolKindFactory {
    Tool create(Material type, String name, ToolSectionView cfg);
}