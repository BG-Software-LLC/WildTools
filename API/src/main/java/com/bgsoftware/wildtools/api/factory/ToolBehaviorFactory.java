package com.bgsoftware.wildtools.api.factory;

import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.api.objects.tools.ToolBehavior;
import org.bukkit.configuration.ConfigurationSection;

@FunctionalInterface
public interface ToolBehaviorFactory {

    ToolBehavior create(Tool tool, ConfigurationSection config);

}
