package com.bgsoftware.wildtools.api.handlers;

import com.bgsoftware.wildtools.api.hooks.ContainerProvider;
import com.bgsoftware.wildtools.api.hooks.DropsProvider;

public interface ProvidersManager {

    /**
     * Add a new container handler for the core.
     * @param containerProvider The handler to add.
     */
    void addContainerProvider(ContainerProvider containerProvider);

    /**
     * Add a new drops handler for the core.
     * @param dropsProvider The handler to add.
     */
    void addDropsProvider(DropsProvider dropsProvider);

}
