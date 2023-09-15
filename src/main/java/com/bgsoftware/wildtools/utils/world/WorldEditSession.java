package com.bgsoftware.wildtools.utils.world;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.hooks.listener.IToolBlockListener;
import com.bgsoftware.wildtools.utils.items.ItemStackMap;
import com.bgsoftware.wildtools.utils.math.NumberUtils;
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

    private ItemStackMap itemsToDrop;
    private int expToDrop = 0;

    private Vector3 dropLocation;
    private boolean applied = false;
    private int minPriority = SetBlockPriority.values().length;


    public WorldEditSession(World world) {
        this.world = world;
    }

    public boolean setType(Location location, Block block) {
        ensureNotApplied();
        return setType(location, plugin.getNMSWorld().getCombinedId(block));
    }

    public boolean setAir(Location location) {
        ensureNotApplied();
        return this.setType(location, 0);
    }

    public boolean setType(Location bukkitLocation, int blockId) {
        ensureNotApplied();
        boolean isPlace = blockId != 0;
        boolean updateBlock = dropLocation == null;
        return this.setType(bukkitLocation, isPlace, vec -> plugin.getNMSWorld().setBlockFast(this.world, vec, blockId, updateBlock), SetBlockPriority.NORMAL);
    }

    public boolean setType(Location bukkitLocation, boolean isPlace, SetBlockFunction setBlockFunction, SetBlockPriority priority) {
        ensureNotApplied();

        Vector3 location = Vector3.of(bukkitLocation);
        boolean setTypeResult = this.setTypeInternal(location, isPlace, setBlockFunction, priority.ordinal());

        if (setTypeResult && dropLocation == null)
            dropLocation = location;

        return setTypeResult;
    }

    private boolean setTypeInternal(Vector3 location, boolean isPlace, SetBlockFunction setBlockFunction, int priority) {
        ensureNotApplied();

        if (isLocationValid(location)) {
            BlockData blockData = new BlockData(location, isPlace, setBlockFunction, priority);
            if (this.minPriority > priority)
                this.minPriority = priority;
            return setDirty(blockData);
        }

        return false;
    }

    public void addDrops(List<ItemStack> drops) {
        ensureNotApplied();

        if (this.itemsToDrop == null)
            this.itemsToDrop = new ItemStackMap();

        this.itemsToDrop.addItems(drops);
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
        int priority = this.minPriority;
        Map<Vector2, List<BlockData>> affectedBlocksByChunks = this.affectedBlocksByChunks;
        while (!affectedBlocksByChunks.isEmpty()) {
            affectedBlocksByChunks = applyBlocksByPriority(this.world, affectedBlocksByChunks, priority++);
        }

        this.affectedBlocksByChunks.forEach((chunkVector, affectedBlocks) ->
                plugin.getNMSWorld().refreshChunk(chunkVector.toChunk(world), affectedBlocks));

        Location bukkitDropLocation = this.dropLocation.toLocation(this.world);

        // Drop all the items
        if (this.itemsToDrop != null && !this.itemsToDrop.isEmpty()) {
            this.itemsToDrop.forEach((itemToDrop, count) -> plugin.getProviders()
                    .getStackedItemProvider().dropItem(bukkitDropLocation, itemToDrop, count.get()));
        }

        if (this.expToDrop > 0) {
            ExperienceOrb orb = this.world.spawn(bukkitDropLocation, ExperienceOrb.class);
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
        return NumberUtils.range(location.getY(), plugin.getNMSWorld().getMinHeight(this.world), this.world.getMaxHeight() - 1) &&
                NumberUtils.range(location.getX(), -MAX_BLOCK_LOCATION, MAX_BLOCK_LOCATION) &&
                NumberUtils.range(location.getZ(), -MAX_BLOCK_LOCATION, MAX_BLOCK_LOCATION);
    }

    private static Map<Vector2, List<BlockData>> applyBlocksByPriority(World world, Map<Vector2, List<BlockData>> affectedBlocksByChunks, int priority) {
        Map<Vector2, List<BlockData>> leftOvers = new LinkedHashMap<>();

        affectedBlocksByChunks.forEach((chunkVector, affectedBlocks) -> {
            List<BlockData> chunkLeftOvers = null;

            for (BlockData blockData : affectedBlocks) {
                if (blockData.priority > priority) {
                    if (chunkLeftOvers == null)
                        chunkLeftOvers = leftOvers.computeIfAbsent(chunkVector, v -> new LinkedList<>());
                    chunkLeftOvers.add(blockData);
                } else {
                    blockData.setBlockFunction.run(blockData.location);
                    plugin.getProviders().notifyToolBlockListeners(world, blockData.location, blockData.isPlace ?
                            IToolBlockListener.Action.BLOCK_PLACE : IToolBlockListener.Action.BLOCK_BREAK);
                }
            }
        });

        return leftOvers;
    }

    public static class BlockData {

        public final Vector3 location;
        public final boolean isPlace;
        public final SetBlockFunction setBlockFunction;
        public final int priority;

        public BlockData(Vector3 location, boolean isPlace, SetBlockFunction setBlockFunction, int priority) {
            this.location = location;
            this.isPlace = isPlace;
            this.setBlockFunction = setBlockFunction;
            this.priority = priority;
        }

    }

    public interface SetBlockFunction {

        void run(Vector3 location);

    }

    public enum SetBlockPriority {

        NORMAL,
        CROPS,
        UPDATES

    }

}
