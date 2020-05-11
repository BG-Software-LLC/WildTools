package com.bgsoftware.wildtools.hooks;

import de.dustplanet.util.SilkUtil;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JavaReflectionMemberAccess")
public final class DropsProvider_SilkSpawners implements DropsProvider {

    private static Method getSpawnerEntityIDMethod = null, getCreatureNameMethod = null, newSpawnerItemMethod;

    static {
        try{
            getSpawnerEntityIDMethod = SilkUtil.class.getMethod("getSpawnerEntityID", Block.class);
            getCreatureNameMethod = SilkUtil.class.getMethod("getCreatureName", String.class);
            newSpawnerItemMethod = SilkUtil.class.getMethod("newSpawnerItem", String.class, String.class, int.class, boolean.class);
        }catch(Exception ignored){}
    }

    private SilkUtil silkUtil;

    public DropsProvider_SilkSpawners(){
        silkUtil = SilkUtil.hookIntoSilkSpanwers();
    }

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        List<ItemStack> drops = new ArrayList<>();

        if(!(block.getState() instanceof CreatureSpawner))
            return drops;

        Object entityId = getSpawnerEntityID((CreatureSpawner) block.getState());
        String mobName = getCreatureName(entityId).toLowerCase().replace(" ", "");

        drops.add(newSpawnerItem(entityId, silkUtil.getCustomSpawnerName(mobName)));

        return drops;
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }

    @Override
    public boolean callEvent() {
        return false;
    }

    private Object getSpawnerEntityID(CreatureSpawner spawner){
        Object entityId;
        try{
            entityId = getSpawnerEntityIDMethod.invoke(silkUtil, spawner.getBlock());
        }catch(Exception ex){
            throw new IllegalStateException("Couldn't process the getCreatureName of SilkSpawners.");
        }
        return entityId == null || entityId.equals(0) ? silkUtil.getDefaultEntityID() : entityId;
    }

    private String getCreatureName(Object entityId){
        try{
            return (String) getCreatureNameMethod.invoke(silkUtil, entityId);
        }catch(Exception ex){
            return silkUtil.getCreatureName((short) entityId);
        }
    }

    private ItemStack newSpawnerItem(Object entityId, String customName){
        try{
            return (ItemStack) newSpawnerItemMethod.invoke(silkUtil, entityId, customName, 1, false);
        }catch(Exception ex){
            return silkUtil.newSpawnerItem((short) entityId, customName, 1, false);
        }
    }

}
