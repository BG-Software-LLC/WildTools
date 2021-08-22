package com.bgsoftware.wildtools.api.objects.tools;

import com.bgsoftware.wildtools.api.objects.ToolMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface Tool {

    /**
     * Set the display name of the tool's item.
     * @param name The new display name.
     */
    void setDisplayName(String name);

    /**
     * Set the lore of the tool's item.
     * @param lore The new lore.
     */
    void setLore(List<String> lore);

    /**
     * Set whether or not the item should have spigot's unbreakable status.
     * @param spigotUnbreakable True if tool should have the status, otherwise false.
     */
    void setSpigotUnbreakable(boolean spigotUnbreakable);

    /**
     * Set whether or not the tool should only affect blocks of the same type.
     * @param onlySameType True if tool should only affect similar blocks, otherwise false.
     */
    void setOnlySameType(boolean onlySameType);

    /**
     * Set whether or not the tool should only affect blocks inside players' claims.
     * @param onlyInsideClaim True if tool should only affect inside claims, otherwise false.
     */
    void setOnlyInsideClaim(boolean onlyInsideClaim);

    /**
     * Set whether or not the drops from the tool should be added to the players' inventories.
     * @param autoCollect True if drops should be added, otherwise false.
     */
    void setAutoCollect(boolean autoCollect);

    /**
     * Set whether or not the blocks broken by the tool will be instant-broken.
     * @param instantBreak True if blocks should be instant-broken, otherwise false.
     */
    void setInstantBreak(boolean instantBreak);

    /**
     * Set whether or not the tool will act like it has a silk-touch enchantment.
     * @param silkTouch True if tool should act like it has a silk-touch, otherwise false.
     */
    void setSilkTouch(boolean silkTouch);

    /**
     * Set whether or not the tool should be unbreakable.
     * @param unbreakable True if tool should be unbreakable, otherwise false.
     */
    void setUnbreakable(boolean unbreakable);

    /**
     * Set whether or not the tool should be damaged by other vanilla actions.
     * This includes hitting mobs, stripping wood, etc.
     * @param vanillaDamage True if should take damage, otherwise false.
     */
    void setVanillaDamage(boolean vanillaDamage);

    /**
     * Set the amount of uses the tool should have.
     * This will override vanilla's durability system.
     * @param usesLeft The amount of uses of the tool.
     */
    void setUsesLeft(int usesLeft);

    /**
     * Set a cooldown for using the tool.
     * @param cooldown The cooldown (in milliseconds)
     */
    void setCooldown(long cooldown);

    /**
     * Set whether or not the tool should be kept in the inventories of players when they die.
     * @param keepInventory True if tool should be kept, otherwise false.
     */
    void setKeepInventory(boolean keepInventory);

    /**
     * Set whether or not the tool should switch types, depends on the broken block.
     * @param omni True if tool should be omni, otherwise false.
     */
    void setOmni(boolean omni);

    /**
     * Set whether or not the tool should be private.
     * @param privateTool True if tool should be private, otherwise false.
     */
    void setPrivate(boolean privateTool);

    /**
     * Set whether or not the durability of the tool should be decreased in relation to the uses.
     * @param usesProgress True if durability should decrease, otherwise false.
     */
    void setUsesProgress(boolean usesProgress);

    /**
     * Add an enchantment for the tool item.
     * @param ench The enchantment to add.
     * @param level The level of the enchantment.
     */
    void addEnchantment(Enchantment ench, int level);

    /**
     * Add a blacklisted material to the tool.
     * All materials beside the blacklisted ones will be affected by the tool.
     * Please note: this feature doesn't work with whitelisted materials.
     * @param type The material to be blacklisted.
     */
    void addBlacklistedMaterial(String type);

    /**
     * Add a whitelisted material to the tool.
     * Only the materials that are whitelisted will be affected by the tool.
     * Please note: this feature doesn't work with blacklisted materials.
     * @param type The material to be whitelisted.
     */
    void addWhitelistedMaterial(String type);

    /**
     * Add a blacklisted drop to the tool.
     * All drops beside the blacklisted ones will be dropped by the tool.
     * Please note: this feature doesn't work with whitelisted drops.
     * @param drop The drop to be blacklisted.
     */
    void addBlacklistedDrop(String drop);

    /**
     * Add a whitelisted drop to the tool.
     * Only the drops that are whitelisted will be dropped by the tool.
     * Please note: this feature doesn't work with blacklisted drops.
     * @param drop The drop to be whitelisted.
     */
    void addWhitelistedDrop(String drop);

    /**
     * Set a multiplier for prices of the tool.
     * Please note: this will work only for sell wands and harvester hoes.
     *              this feature also works with SuperMobCoins.
     * @param multiplier The multiplier to set.
     */
    void setMultiplier(double multiplier);

    /**
     * Set the amount of exp required to combine this tool with another one.
     * You can disable the ability to combine this tool in an anvil by setting the exp to be -1.
     * @param anvilCombineExp The amount of exp needed to combine two tools.
     */
    void setAnvilCombineExp(int anvilCombineExp);

    /**
     * Set the maximum uses a tool can have from combining tools.
     * You can make it unlimited by setting the limit to be -1.
     * @param anvilCombineMax The maximum uses a new tool can have.
     */
    void setAnvilCombineLimit(int anvilCombineMax);

    /**
     * Set the blacklisted worlds of the tool.
     * The tool will not work in these worlds.
     * @param worlds The blacklisted worlds.
     */
    void setBlacklistedWorlds(List<String> worlds);

    /**
     * Set the whitelisted worlds of the tool.
     * The tool will only work in these worlds.
     * @param worlds The whitelisted worlds.
     */
    void setWhitelistedWorlds(List<String> worlds);

    /**
     * Set whether broken blocks should be counted towards statistics of the player or not.
     */
    void setStatistics(boolean statistics);

    /**
     * Get the raw item stack of the tool.
     * This item will not have parsed placeholders, or injected data.
     */
    ItemStack getItemStack();

    /**
     * Get a formatted item with default amount of uses.
     * This item will have parsed placeholders and injected data.
     */
    ItemStack getFormattedItemStack();

    /**
     * Get a formatted item with specific amount of uses.
     * This item will have parsed placeholders and injected data.
     * @param uses The amount of uses the item should have.
     */
    ItemStack getFormattedItemStack(int uses);

    /**
     * Get the tool mode.
     */
    ToolMode getToolMode();

    /**
     * Get the name of the tool.
     */
    String getName();

    /**
     * Check whether or not the tool is unbreakable.
     */
    boolean isUnbreakable();

    /**
     * Check whether or not the tool can take damage by other vanilla actions.
     */
    boolean hasVanillaDamage();

    /**
     * Check whether or not drops from the tool are added to the players' inventories.
     */
    boolean isAutoCollect();

    /**
     * Check whether or not blocks broken by the tool are instant-broken.
     */
    boolean isInstantBreak();

    /**
     * Check whether or not the tool acts like it has silk-touch.
     */
    boolean hasSilkTouch();

    /**
     * Get the default amount of uses of the tool.
     */
    int getDefaultUses();

    /**
     * Check whether or not the tool is using vanilla durability.
     */
    boolean isUsingDurability();

    /**
     * Check whether or not the tool only affects blocks of the same type.
     */
    boolean isOnlySameType();

    /**
     * Check whether or not the tool only affects blocks inside players' claims.
     */
    boolean isOnlyInsideClaim();

    /**
     * Get the cooldown of the tool.
     */
    long getCooldown();

    /**
     * Check whether or not the tool is kept in the inventories of players when they die.
     */
    boolean hasKeepInventory();

    /**
     * Check whether or not the tool switches types, depends on the broken block.
     */
    boolean isOmni();

    /**
     * Check whether or not the tool is private.
     */
    boolean isPrivate();

    /**
     * Check whether or not the durability of the tool should be decreased in relation to the uses.
     */
    boolean isUsesProgress();

    /**
     * Check whether or not the tool can be combined with another tool in an anvil.
     */
    boolean isAnvilCombine();

    /**
     * Check whether or not there is a maximum uses that a new tool can have from combining two tools.
     */
    boolean hasAnvilCombineLimit();

    /**
     * Get all the blacklisted block materials.
     */
    Set<String> getBlacklistedMaterials();

    /**
     * Get all the whitelisted block materials.
     */
    Set<String> getWhitelistedMaterials();

    /**
     * Get all the blacklisted block drops.
     */
    Set<String> getBlacklistedDrops();

    /**
     * Get all the whitelisted block drops.
     */
    Set<String> getWhitelistedDrops();

    /**
     * Check whether or not the tool has blacklisted block materials.
     */
    boolean hasBlacklistedMaterials();

    /**
     * Check whether or not the tool has whitelisted block materials.
     */
    boolean hasWhitelistedMaterials();

    /**
     * Check whether or not the tool has blacklisted block drops.
     */
    boolean hasBlacklistedDrops();

    /**
     * Check whether or not the tool has whitelisted block drops.
     */
    boolean hasWhitelistedDrops();

    /**
     * Check whether or not a block is blacklisted.
     * @param type The block's material type.
     * @param data The block's data value.
     */
    boolean isBlacklistedMaterial(Material type, short data);

    /**
     * Check whether or not a block is whitelisted.
     * @param type The block's material type.
     * @param data The block's data value.
     */
    boolean isWhitelistedMaterial(Material type, short data);

    /**
     * Check whether or not a drop is blacklisted.
     * @param type The drop's material type.
     * @param data The drop's data value.
     */
    boolean isBlacklistedDrop(Material type, short data);

    /**
     * Check whether or not a drop is whitelisted.
     * @param type The drop's material type.
     * @param data The drop's data value.
     */
    boolean isWhitelistedDrop(Material type, short data);

    /**
     * Get the multiplier for prices of the tool.
     * Please note: this only works for sell wands and harvester hoes.
     *              this feature also works with SuperMobCoins.
     */
    double getMultiplier();

    /**
     * Get the amount of exp required to combine this tool with another one.
     */
    int getAnvilCombineExp();

    /**
     * Get the maximum uses a new tool can have from combining two tools in an anvil.
     */
    int getAnvilCombineLimit();

    /**
     * Check whether or not a world is blacklisted.
     * @param world The world to check.
     */
    boolean isBlacklistedWorld(String world);

    /**
     * Check whether or not a world is whitelisted.
     * @param world The world to check.
     */
    boolean isWhitelistedWorld(String world);

    /**
     * Check whether broken blocks should be counted towards statistics of the player or not.
     */
    boolean hasStatistics();

    /**
     * Reduce durability of an item of this tool.
     * @param pl The player which holds/used the tool.
     * @param amount The amount of durability to remove.
     * @param itemStack The item to reduce it's durability.
     */
    void reduceDurablility(Player pl, int amount, ItemStack itemStack);

    /**
     * Get the durability of an item of this tool.
     * @param player The player which holds/used the tool.
     * @param itemStack The item to check.
     */
    int getDurability(Player player, ItemStack itemStack);

    /**
     * Check whether or not the tool can break a block.
     * @param block The block to check.
     * @param firstType The first broken-block's material type.
     * @param data The first broken-block's data value.
     */
    boolean canBreakBlock(Block block, Material firstType, short data);

    /**
     * Filter all drops for this tool.
     * This will consider whitelisted/blacklisted drops, and remove null & air items.
     * @param drops The original drops.
     */
    List<ItemStack> filterDrops(List<ItemStack> drops);

    /**
     * Check whether or not an item is of this tool.
     * @param is The item to check.
     */
    boolean isSimilar(ItemStack is);

    /**
     * Update the last time a player used this tool.
     * This will only work if cooldown exists for the tool.
     * @param uuid The UUID of the player.
     */
    void setLastUse(UUID uuid);

    /**
     * Check whether or not a player can use this tool, and no cooldowns are active for him.
     * @param uuid The UUID of the player.
     */
    boolean canUse(UUID uuid);

    /**
     * Get the amount of time left of cooldown for a player to use this tool.
     * @param uuid The UUID of the player.
     */
    long getTimeLeft(UUID uuid);

    /**
     * A method that is called when players break blocks using this tool.
     * @param e The block event that was fired.
     * @return True if everything went as it should, otherwise false.
     */
    boolean onBlockBreak(BlockBreakEvent e);

    /**
     * A method that is called when players interact with blocks using this tool.
     * @param e The interact event that was fired.
     * @return True if everything went as it should, otherwise false.
     */
    boolean onBlockInteract(PlayerInteractEvent e);

    /**
     * A method that is called when players hit blocks using this tool.
     * @param e The block event that was fired.
     * @return True if everything went as it should, otherwise false.
     */
    boolean onBlockHit(PlayerInteractEvent e);

    /**
     * A method that is called when players hit air using this tool.
     * @param e The interact event that was fired.
     * @return True if everything went as it should, otherwise false.
     */
    boolean onAirInteract(PlayerInteractEvent e);

}
