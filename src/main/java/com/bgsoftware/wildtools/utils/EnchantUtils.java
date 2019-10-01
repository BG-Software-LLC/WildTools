package com.bgsoftware.wildtools.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
public final class EnchantUtils {

    public static Map<String, Enchantment> getByName(){
        try{
            Field byNameField = Enchantment.class.getDeclaredField("byName");
            byNameField.setAccessible(true);
            return (Map<String, Enchantment>) byNameField.get(null);
        }catch(Exception ex){
            return new HashMap<>();
        }
    }

    public static Map<NamespacedKey, Enchantment> getByKey(){
        try{
            Field byKeyField = Enchantment.class.getDeclaredField("byKey");
            byKeyField.setAccessible(true);
            return (Map<NamespacedKey, Enchantment>) byKeyField.get(null);
        }catch(Exception ex){
            return new HashMap<>();
        }
    }

    public static Map<Integer, Enchantment> getById(){
        try{
            Field byIdField = Enchantment.class.getDeclaredField("byId");
            byIdField.setAccessible(true);
            return (Map<Integer, Enchantment>) byIdField.get(null);
        }catch(Exception ex){
            return new HashMap<>();
        }
    }

}
