package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipesHandler {

    public RecipesHandler(WildToolsPlugin plugin){
        File file = new File(plugin.getDataFolder(), "recipes.yml");

        if(!file.exists()){
            plugin.saveResource("recipes.yml", false);
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<String, ItemStack> recipeItems = new HashMap<>();

        if(cfg.contains("items")){
            for(String name : cfg.getConfigurationSection("items").getKeys(false)){
                recipeItems.put(name.toUpperCase(), new ItemBuilder(cfg.getConfigurationSection("items." + name)).build());
            }
        }

        if(cfg.contains("recipes")){
            outerLoop:
            for(String name : cfg.getConfigurationSection("recipes").getKeys(false)){
                Tool tool = plugin.getToolsManager().getTool(name);

                if(tool == null) {
                    WildToolsPlugin.log("Couldn't find the tool " + name + ", skipping recipe...");
                    continue;
                }

                Map<String, Character> typeToChar = new HashMap<>();
                Map<Character, ItemStack> charToItem = new HashMap<>();

                List<String> configRecipe = cfg.getStringList("recipes." + name);

                char[][] shape = new char[][] {
                        new char[] {' ', ' ', ' '},
                        new char[] {' ', ' ', ' '},
                        new char[] {' ', ' ', ' '}
                };

                for(int i = 0; i < 3; i++){
                    String[] types = configRecipe.get(i).split(", ");
                    for(int j = 0; j < 3; j++){
                        String type = types[j].toUpperCase();

                        Character ch = typeToChar.get(type);

                        if(ch != null) {
                            shape[i][j] = ch;
                            continue;
                        }

                        ch = generateChar(type, typeToChar);
                        shape[i][j] = ch;

                        ItemStack itemStack;

                        try {
                            if (recipeItems.containsKey(type)) {
                                itemStack = recipeItems.get(type);
                            } else {
                                if (type.contains(":")) {
                                    String[] typeSections = type.split(":");
                                    itemStack = new ItemStack(Material.valueOf(typeSections[0]), 1, Short.parseShort(typeSections[1]));
                                } else {
                                    itemStack = new ItemStack(Material.valueOf(type));
                                }
                            }
                        }catch(Exception ex){
                            WildToolsPlugin.log("Couldn't find valid recipe for " + name + ", skipping...");
                            continue outerLoop;
                        }

                        charToItem.put(ch, itemStack);
                    }
                }

                AdvancedShapedRecipe recipe =  plugin.getNMSAdapter().createRecipe(tool.getName(), tool.getFormattedItemStack());
                recipe.shape(new String(shape[0]), new String(shape[1]), new String(shape[2]));

                for(Map.Entry<Character, ItemStack> entry : charToItem.entrySet())
                    recipe.setIngredient(entry.getKey(), entry.getValue());

                // TODO: Fix recipe
//                try {
//                    Bukkit.addRecipe(recipe.toRecipe());
//                }catch (Exception ignored){}
            }
        }

    }

    private char[] charsToGenerate = new char[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H','I'
    };

    private char generateChar(String type, Map<String, Character> typeToChar){
        for(char ch : charsToGenerate){
            if(!typeToChar.containsValue(ch)){
                typeToChar.put(type, ch);
                return ch;
            }
        }

        throw new RuntimeException("Failed to find a character to generate!");
    }

}
