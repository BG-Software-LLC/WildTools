package xyz.wildseries.wildtools.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.wildseries.wildtools.objects.WMaterial;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class ItemBuilder {

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder(ItemStack itemStack){
        this(itemStack.getType(), itemStack.getDurability());
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(WMaterial type){
        this(type.parseMaterial(), 0);
    }

    public ItemBuilder(Material type){
        this(type, 0);
    }

    public ItemBuilder(Material type, int damage){
        itemStack = new ItemStack(type, 1, (short) damage);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder withName(String name){
        if(!name.isEmpty())
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    public ItemBuilder withLore(String firstLine, List<String> listLine){
        List<String> loreList = new ArrayList<>();

        firstLine = ChatColor.translateAlternateColorCodes('&', firstLine);
        loreList.add(firstLine);

        for(String line : listLine){
            loreList.add(ChatColor.getLastColors(firstLine) + ChatColor.translateAlternateColorCodes('&', line));
        }

        if(loreList.size() > 10){
            for(int i = 10; i < loreList.size(); i++){
                loreList.remove(loreList.get(i));
            }
            loreList.add(ChatColor.getLastColors(firstLine) + "...");
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder withLore(String... lore){
        List<String> loreList = new ArrayList<>();

        for(String line : lore){
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemStack build(){
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
