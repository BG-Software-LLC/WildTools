package com.bgsoftware.wildtools.api.handlers;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.bgsoftware.wildtools.api.hooks.ContainerProvider;
import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;

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

    /**
     * Set the prices provider for the core.
     * @param pricesProvider The provider to set.
     */
    void setPricesProvider(PricesProvider pricesProvider);

    /**
     * Add a new claims handler for the core.
     * @param claimsProvider The handler to add.
     */
    void addClaimsProvider(ClaimsProvider claimsProvider);

}
