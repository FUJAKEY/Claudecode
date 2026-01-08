package com.demonslayer.init;

import com.demonslayer.DemonSlayerMod;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroup {
    public static final ItemGroup DEMON_SLAYER_TAB = new ItemGroup("demonslayer") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.NICHIRIN_SWORD_BLACK.get());
        }
    };
}
