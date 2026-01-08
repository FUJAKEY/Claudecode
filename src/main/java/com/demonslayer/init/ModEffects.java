package com.demonslayer.init;

import com.demonslayer.DemonSlayerMod;
import com.demonslayer.effects.TotalConcentrationEffect;
import com.demonslayer.effects.DemonPowerEffect;
import net.minecraft.potion.Effect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEffects {
    public static final DeferredRegister<Effect> EFFECTS = 
            DeferredRegister.create(ForgeRegistries.POTIONS, DemonSlayerMod.MOD_ID);

    public static final RegistryObject<Effect> TOTAL_CONCENTRATION = EFFECTS.register("total_concentration",
            TotalConcentrationEffect::new);

    public static final RegistryObject<Effect> DEMON_POWER = EFFECTS.register("demon_power",
            DemonPowerEffect::new);

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
