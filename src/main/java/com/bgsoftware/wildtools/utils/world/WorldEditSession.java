package com.bgsoftware.wildtools.utils.world;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.hooks.listener.IToolBlockListener;
import com.bgsoftware.wildtools.utils.NumberUtils;
import com.bgsoftware.wildtools.utils.math.Vector2;
import com.bgsoftware.wildtools.utils.math.Vector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldEditSession {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final int MAX_BLOCK_LOCATION = 29999984;

    private final Map<Vector2, List<BlockData>> affectedBlocksByChunks = new LinkedHashMap<>();
    private final Map<Vector3, BlockData> affectedBlocks = new LinkedHashMap<>();
    private final World world;

    private List<ItemStack> itemsToDrop;
    private int expToDrop = 0;

    private Vector3 dropLocation;
    private boolean applied = false;


    public WorldEditSession(World world) {
        this.world = world;
    }

    public void setType(Location location, Block block) {
        ensureNotApplied();
        setType(location, plugin.getNMSAdapter().getCombinedId(block));
    }

    public void setAir(Location location) {
        ensureNotApplied();
        this.setType(location, 0);
    }

    public void setType(Location bukkitLocation, int blockId) {
        ensureNotApplied();

        Vector3 location = Vector3.of(bukkitLocation);
        boolean isPlace = blockId != 0;

        boolean updateBlock = dropLocation == null;

        boolean res = this.setTypeInternal(location, isPlace,
                vec -> plugin.getNMSAdapter().setBlockFast(this.world, vec, blockId, updateBlock));

        if (res && updateBlock)
            dropLocation = location;
    }

    public boolean setType(Location bukkitLocation, boolean isPlace, SetBlockFunction setBlockFunction) {
        ensureNotApplied();
        return this.setTypeInternal(Vector3.of(bukkitLocation), isPlace, setBlockFunction);
    }

    private boolean setTypeInternal(Vector3 location, boolean isPlace, SetBlockFunction setBlockFunction) {
        ensureNotApplied();

        if (isLocationValid(location)) {
            BlockData blockData = new BlockData(location, isPlace, setBlockFunction);
            return setDirty(blockData);
        }

        return false;
    }

    public void addDrops(List<ItemStack> drops) {
        ensureNotApplied();
        if (this.itemsToDrop == null) this.itemsToDrop = new LinkedList<>();
        this.itemsToDrop.addAll(drops);
    }

    public void addExp(int exp) {
        ensureNotApplied();
        this.expToDrop += exp;
    }

    public List<Location> getAffectedBlocks() {
        return Collections.unmodifiableList(affectedBlocks.keySet().stream()
                .map(vec -> new Location(this.world, vec.getX(), vec.getY(), vec.getZ()))
                .collect(Collectors.toList()));
    }

    public void apply() {
        ensureNotApplied();

        if (dropLocation == null) {
            applied = true;
            return;
        }

        //Refreshing chunks
        affectedBlocksByChunks.forEach((chunkVector, affectedBlocks) -> {
            affectedBlocks.forEach(blockData -> {
                blockData.setBlockFunction.run(blockData.location);
                plugin.getProviders().notifyToolBlockListeners(this.world, blockData.location, blockData.isPlace ?
                        IToolBlockListener.Action.BLOCK_PLACE : IToolBlockListener.Action.BLOCK_BREAK);
            });

            plugin.getNMSAdapter().refreshChunk(chunkVector.toChunk(this.world), affectedBlocks);
        });

        // Drop all the items
        if (this.itemsToDrop != null && !this.itemsToDrop.isEmpty())
            plugin.getNMSAdapter().dropItems(this.world, this.dropLocation, this.itemsToDrop);

        if (this.expToDrop > 0) {
            Location bukkitLocation = this.dropLocation.toLocation(this.world);
            ExperienceOrb orb = this.world.spawn(bukkitLocation, ExperienceOrb.class);
            orb.setExperience(this.expToDrop);
        }

        applied = true;
    }

    private boolean setDirty(BlockData blockData) {
        if (this.affectedBlocks.containsKey(blockData.location))
            return false;

        this.affectedBlocks.put(blockData.location, blockData);

        Vector2 chunk = new Vector2(blockData.location.getX() >> 4, blockData.location.getZ() >> 4);
        affectedBlocksByChunks.computeIfAbsent(chunk, c -> new LinkedList<>()).add(blockData);

        return true;
    }

    private void ensureNotApplied() {
        if (applied)
            throw new IllegalStateException("Cannot use an already applied session");
    }

    private boolean isLocationValid(Vector3 location) {
        return NumberUtils.range(location.getY(), plugin.getNMSAdapter().getMinHeight(this.world), this.world.getMaxHeight()) &&
                NumberUtils.range(location.getX(), -MAX_BLOCK_LOCATION, MAX_BLOCK_LOCATION) &&
                NumberUtils.range(location.getZ(), -MAX_BLOCK_LOCATION, MAX_BLOCK_LOCATION);
    }

    public static class BlockData {

        public final Vector3 location;
        public final boolean isPlace;
        public final SetBlockFunction setBlockFunction;

        public BlockData(Vector3 location, boolean isPlace, SetBlockFunction setBlockFunction) {
            this.location = location;
            this.isPlace = isPlace;
            this.setBlockFunction = setBlockFunction;
        }

    }

    public interface SetBlockFunction {

        void run(Vector3 location);

    }

}
