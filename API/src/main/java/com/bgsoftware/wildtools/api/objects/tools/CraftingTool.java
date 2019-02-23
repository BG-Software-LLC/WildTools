package com.bgsoftware.wildtools.api.objects.tools;

import org.bukkit.inventory.Recipe;

import java.util.Iterator;

@SuppressWarnings("unused")
public interface CraftingTool extends Tool {

    Iterator<Recipe> getCraftings();

}
