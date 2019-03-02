package com.bgsoftware.wildtools.api.objects;

import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.Location;
import org.bukkit.block.Dispenser;

import java.util.List;

public interface Selection {

    void setRightClick(Location rightClick);

    void setLeftClick(Location leftClick);

    boolean isReady();

    boolean isInside();

    List<Dispenser> getDispensers(Tool tool);

    void remove();

}
