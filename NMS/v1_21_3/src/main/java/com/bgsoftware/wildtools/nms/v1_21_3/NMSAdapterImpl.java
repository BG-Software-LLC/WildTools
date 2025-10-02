package com.bgsoftware.wildtools.nms.v1_21_3;

import org.bukkit.inventory.meta.ItemMeta;

public class NMSAdapterImpl extends com.bgsoftware.wildtools.nms.v1_21_3.AbstractNMSAdapter {

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.setEnchantmentGlintOverride(true);
    }

}
