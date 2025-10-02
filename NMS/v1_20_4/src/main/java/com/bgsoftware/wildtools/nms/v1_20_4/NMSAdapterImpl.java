package com.bgsoftware.wildtools.nms.v1_20_4;

import org.bukkit.inventory.meta.ItemMeta;

public class NMSAdapterImpl extends com.bgsoftware.wildtools.nms.v1_20_4.AbstractNMSAdapter {

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.setEnchantmentGlintOverride(true);
    }

}
