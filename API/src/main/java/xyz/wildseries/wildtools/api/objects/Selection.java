package xyz.wildseries.wildtools.api.objects;

import org.bukkit.Location;
import org.bukkit.block.Dispenser;

import java.util.List;

public interface Selection {

    void setRightClick(Location rightClick);

    void setLeftClick(Location leftClick);

    boolean isReady();

    boolean isInside();

    List<Dispenser> getDispensers();

    void remove();

}
