package com.bgsoftware.wildtools.api.objects;

import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.Location;
import org.bukkit.block.Dispenser;

import java.util.List;

public interface Selection {

    /**
     * Set the right-click location of the selection.
     * @param rightClick The right-click location.
     */
    void setRightClick(Location rightClick);

    /**
     * Set the left-click location of the selection.
     * @param leftClick The right-click location.
     */
    void setLeftClick(Location leftClick);

    /**
     * Check whether or not both locations are set.
     */
    boolean isReady();

    /**
     * Check whether or not the player is between both locations.
     */
    boolean isInside();

    /**
     * Get all the dispensers between both locations.
     * @param tool The tool that was used.
     */
    List<Dispenser> getDispensers(Tool tool);

    /**
     * Remove the selection from cache.
     * Please note: selections are auto removed from cache after 10 minutes.
     */
    void remove();

}
