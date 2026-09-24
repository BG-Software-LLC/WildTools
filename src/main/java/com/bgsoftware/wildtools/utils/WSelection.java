package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.api.objects.Selection;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.scheduler.ScheduledTask;
import com.bgsoftware.wildtools.scheduler.Scheduler;
import com.bgsoftware.wildtools.tools.WCannonTool;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WSelection implements Selection {

    private final UUID uuid;
    private final World world;

    private Location rightClick, leftClick;
    private ScheduledTask task;

    public WSelection(UUID uuid, World world, Location rightClick, Location leftClick){
        this.uuid = uuid;
        this.world = world;
        this.rightClick = rightClick;
        this.leftClick = leftClick;
        restartTask();
    }

    @Override
    public void setRightClick(Location rightClick) {
        this.rightClick = rightClick;
        restartTask();
    }

    @Override
    public void setLeftClick(Location leftClick) {
        this.leftClick = leftClick;
        restartTask();
    }

    @Override
    public boolean isReady(){
        return this.rightClick != null && this.leftClick != null;
    }

    @Override
    public boolean isInside(){
        Location loc = Bukkit.getPlayer(this.uuid).getLocation();

        Location min = getMinLocation();
        Location max = getMaxLocation();

        return min.getBlockX() <= loc.getBlockX() && max.getBlockX() >= loc.getBlockX() &&
                min.getBlockY() <= loc.getBlockY() && max.getBlockY() >= loc.getBlockY() &&
                min.getBlockZ() <= loc.getBlockZ() && max.getBlockZ() >= loc.getBlockZ();
    }

    @Override
    public List<Dispenser> getDispensers(Tool tool){
        List<Dispenser> dispensers = new ArrayList<>();

        Location min = getMinLocation();
        Location max = getMaxLocation();

        Player player = Bukkit.getPlayer(this.uuid);

        for (int y = max.getBlockY(); y >= min.getBlockY(); y--) {
            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.DISPENSER && BukkitUtils.canInteractBlock(player, block, player.getItemInHand())) {
                        dispensers.add((Dispenser) block.getState());
                    }
                }
            }
        }

        return dispensers;
    }

    @Override
    public void remove(){
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }

        WCannonTool.removeSelection(Bukkit.getPlayer(uuid));
    }

    private void restartTask(){
        if (this.task != null) {
            this.task.cancel();
        }

        this.task = Scheduler.runTaskAsync(this::remove, 20 * 60 * 10);
    }

    private Location getMinLocation() {
        return new Location(this.world, Math.min(this.rightClick.getBlockX(), this.leftClick.getBlockX()),
                Math.min(this.rightClick.getBlockY(), this.leftClick.getBlockY()),
                Math.min(this.rightClick.getBlockZ(), this.leftClick.getBlockZ()));
    }

    private Location getMaxLocation() {
        return new Location(this.world, Math.max(this.rightClick.getBlockX(), this.leftClick.getBlockX()),
                Math.max(this.rightClick.getBlockY(), this.leftClick.getBlockY()),
                Math.max(this.rightClick.getBlockZ(), this.leftClick.getBlockZ()));
    }

}
