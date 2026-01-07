package com.infinitygauntlet.init;

import com.infinitygauntlet.InfinityGauntletMod;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroup extends ItemGroup {
    
    public static final ModItemGroup INFINITY_GAUNTLET_TAB = new ModItemGroup("infinitygauntlet");
    
    public ModItemGroup(String label) {
        super(label);
    }
    
    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ModItems.INFINITY_GAUNTLET.get());
    }
}
