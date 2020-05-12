package com.bgsoftware.wildtools.api;

import com.bgsoftware.wildtools.api.handlers.ProvidersManager;
import com.bgsoftware.wildtools.api.handlers.ToolsManager;

public interface WildTools {

    /**
     * Get the tools manager of the core.
     */
    ToolsManager getToolsManager();

    /**
     * Get the providers manager of the core.
     */
    ProvidersManager getProviders();

}
