package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

public enum Materials {

    SUGAR_CANE("SUGAR_CANE_BLOCK"),
    MELON("MELON_BLOCK"),
    FARMLAND("SOIL"),
    GRASS_BLOCK("GRASS"),
    MAP("EMPTY_MAP"),
    CLOCK("WATCH"),
    REDSTONE_TORCH("REDSTONE_TORCH_ON"),
    IRON_BARS("IRON_FENCE"),
    CRAFTING_TABLE("WORKBENCH"),
    CAULDRON("CAULDRON_ITEM"),
    EXPERIENCE_BOTTLE("EXP_BOTTLE"),
    COBWEB("WEB"),
    SPAWNER("MOB_SPAWNER"),
    BLACK_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 15),
    LIGHT_BLUE_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 3),
    ENCHANTED_GOLDEN_APPLE("GOLDEN_APPLE", 1);

    private static final Map<Material, EnumSet<Tag>> MATERIAL_TAGS = initializeMaterialTags();

    private static int farmlandId = -1;

    private final Material bukkitType;
    private final short bukkitData;

    Materials(String legacy) {
        this(legacy, -1);
    }

    Materials(String legacy, int bukkitData) {
        this.bukkitType = Material.matchMaterial(ServerVersion.isLegacy() ? legacy : name());
        this.bukkitData = (short) bukkitData;
    }

    public Material toBukkitType() {
        return this.bukkitType;
    }

    public ItemStack toItemStack(int amount) {
        return this.bukkitData < 0 ? new ItemStack(this.bukkitType, amount) : new ItemStack(this.bukkitType, amount, this.bukkitData);
    }

    public static boolean isCrop(Material material) {
        return hasMaterialTag(material, Tag.CROPS);
    }

    public static boolean isHarvestable(Material material) {
        return hasMaterialTag(material, Tag.HARVESTABLE);
    }

    public static boolean isFarmland(Material material) {
        return hasMaterialTag(material, Tag.FARMLAND);
    }

    public static boolean isChorus(Material material) {
        return hasMaterialTag(material, Tag.CHORUS);
    }

    public static boolean isFlower(Material material) {
        return hasMaterialTag(material, Tag.FLOWER);
    }

    public static boolean isEndBlock(Material material) {
        return hasMaterialTag(material, Tag.END_BLOCK);
    }

    public static boolean isSlab(Material material) {
        return hasMaterialTag(material, Tag.SLAB);
    }

    public static boolean isPlaceThroughBlock(Material material) {
        return hasMaterialTag(material, Tag.PLACE_THROUGH_BLOCK);
    }

    public static boolean isSpawner(Material material) {
        return hasMaterialTag(material, Tag.SPAWNER);
    }

    public static boolean isBottle(Material material) {
        return hasMaterialTag(material, Tag.BOTTLE);
    }

    public static boolean isBucket(Material material) {
        return hasMaterialTag(material, Tag.BUCKET);
    }

    public static boolean isBlacklisted(Material material) {
        return hasMaterialTag(material, Tag.BLACKLISTED_BLOCK);
    }

    public static boolean isForceUpdate(Material material) {
        return hasMaterialTag(material, Tag.FORCE_UPDATE);
    }

    private static boolean hasMaterialTag(Material material, Tag tag) {
        EnumSet<Tag> tags = MATERIAL_TAGS.get(material);
        return tags != null && tags.contains(tag);
    }

    public static int getFarmlandId() {
        if (farmlandId == -1)
            farmlandId = WildToolsPlugin.getPlugin().getNMSAdapter().getFarmlandId();

        return farmlandId;
    }

    public static Optional<Material> getSafeMaterial(String name) {
        try {
            return Optional.of(Material.valueOf(name));
        } catch (IllegalArgumentException error) {
            return Optional.empty();
        }
    }

    private static Map<Material, EnumSet<Tag>> initializeMaterialTags() {
        EnumMap<Material, EnumSet<Tag>> materialTags = new EnumMap<>(Material.class);

        for (Material material : Material.values()) {
            String materialName = material.name();
            EnumSet<Tag> tags = EnumSet.noneOf(Tag.class);

            if (materialName.equals("BEDROCK") || materialName.equals("COMMAND") || materialName.equals("REPEATING_COMMAND_BLOCK") ||
                    materialName.equals("CHAIN_COMMAND_BLOCK") || materialName.equals("COMMAND_BLOCK") || materialName.equals("WATER") ||
                    materialName.equals("STATIONARY_WATER") || materialName.equals("LAVA") || materialName.equals("STATIONARY_LAVA") ||
                    materialName.equals("END_PORTAL_FRAME") || materialName.equals("ENDER_PORTAL_FRAME") || materialName.equals("BARRIER") ||
                    materialName.equals("STRUCTURE_BLOCK") || materialName.equals("STRUCTURE_VOID") || materialName.equals("END_PORTAL") ||
                    materialName.equals("ENDER_PORTAL") || materialName.equals("NETHER_PORTAL") || materialName.equals("PORTAL") ||
                    materialName.equals("BUBBLE_COLUMN") || materialName.equals("CAVE_AIR") || materialName.equals("VOID_AIR") ||
                    materialName.equals("AIR")) {
                tags.add(Tag.BLACKLISTED_BLOCK);
            }
            if (materialName.equals("WATER") || materialName.equals("STATIONARY_WATER") || materialName.equals("LAVA") ||
                    materialName.equals("STATIONARY_LAVA") || materialName.equals("ENDER_PORTAL") || materialName.equals("NETHER_PORTAL") ||
                    materialName.equals("PORTAL") || materialName.equals("BUBBLE_COLUMN")) {
                tags.add(Tag.FORCE_UPDATE);
            }
            if (materialName.equals("CROPS") || materialName.equals("WHEAT") || materialName.equals("CARROT") ||
                    materialName.equals("CARROTS") || materialName.equals("POTATO") || materialName.equals("POTATOES") ||
                    materialName.equals("BEETROOT_BLOCK") || materialName.equals("BEETROOTS") || materialName.equals("NETHER_WARTS") ||
                    materialName.equals("NETHER_WART") || materialName.equals("CACTUS") || materialName.equals("BAMBOO") ||
                    materialName.equals("SUGAR_CANE_BLOCK") || materialName.equals("SUGAR_CANE") || materialName.equals("MELON_BLOCK") ||
                    materialName.equals("MELON") || materialName.equals("PUMPKIN") || materialName.equals("COCOA")) {
                tags.add(Tag.CROPS);
                tags.add(Tag.HARVESTABLE);
            }
            if (materialName.equals("DIRT") || material == GRASS_BLOCK.toBukkitType()) {
                tags.add(Tag.HARVESTABLE);
                tags.add(Tag.FARMLAND);
            }
            if (materialName.contains("CHORUS")) {
                tags.add(Tag.HARVESTABLE);
                tags.add(Tag.CHORUS);
            }
            if (materialName.contains("FLOWER")) {
                tags.add(Tag.FLOWER);
            }
            if (materialName.contains("END")) {
                tags.add(Tag.END_BLOCK);
            }
            if (materialName.contains("STEP") || materialName.contains("SLAB")) {
                tags.add(Tag.SLAB);
            }
            if (!material.isSolid() && material != Materials.COBWEB.toBukkitType() && !tags.contains(Tag.CROPS)) {
                tags.add(Tag.PLACE_THROUGH_BLOCK);
            }
            if (materialName.contains("SPAWNER")) {
                tags.add(Tag.SPAWNER);
            }
            if (materialName.contains("BOTTLE")) {
                tags.add(Tag.BOTTLE);
            }
            if (materialName.contains("BUCKET")) {
                tags.add(Tag.BUCKET);
            }

            if (!tags.isEmpty())
                materialTags.put(material, tags);
        }

        return materialTags.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(materialTags);
    }

    private enum Tag {

        CROPS,
        HARVESTABLE,
        CHORUS,
        FARMLAND,
        FLOWER,
        END_BLOCK,
        SLAB,
        PLACE_THROUGH_BLOCK,
        SPAWNER,
        BOTTLE,
        BUCKET,
        BLACKLISTED_BLOCK,
        FORCE_UPDATE

    }

}
