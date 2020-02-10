package com.bgsoftware.wildtools.hooks;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class PaperHook {

    private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private static Field blockControllerField = null;
    private static Method leftClickBlockMethod = null;

    public static void init(){
        try{
            Class<?> worldClass = getNMSClass("World"),
                    interactManagerClass = getNMSClass("PlayerInteractManager"),
                    blockPositionClass = getNMSClass("BlockPosition"),
                    enumDirectionClass = getNMSClass("EnumDirection"),
                    antiXRayClass = Class.forName("com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray");

            blockControllerField = worldClass.getDeclaredField("chunkPacketBlockController");
            leftClickBlockMethod = antiXRayClass.getMethod("onPlayerLeftClickBlock", interactManagerClass, blockPositionClass, enumDirectionClass);
        }catch(Throwable ignored){}
    }

    public static void handleLeftClickBlockMethod(Object world, Object playerInteractManager, Object blockPosition, Object enumDirection){
        try {
            leftClickBlockMethod.invoke(blockControllerField.get(world), playerInteractManager, blockPosition, enumDirection);
        }catch(Throwable ignored){}
    }

    public static boolean isAntiXRayAvailable(){
        return blockControllerField != null && leftClickBlockMethod != null;
    }

    private static Class<?> getNMSClass(String clazz) throws ClassNotFoundException{
        return Class.forName("net.minecraft.server." + version + "." + clazz);
    }

}
