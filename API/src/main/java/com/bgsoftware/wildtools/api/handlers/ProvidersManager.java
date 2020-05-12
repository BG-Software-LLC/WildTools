package com.bgsoftware.wildtools.api.handlers;

import com.bgsoftware.wildtools.api.hooks.ContainerProvider;

public interface ProvidersManager {

    /**
     * Add a new container handler for the core.
     * @param containerProvider The handler to add.
     */
    void addContainerProvider(ContainerProvider containerProvider);

}
