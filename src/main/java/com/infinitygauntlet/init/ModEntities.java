package com.infinitygauntlet.init;

import com.infinitygauntlet.InfinityGauntletMod;
import com.infinitygauntlet.entity.ThanosEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
    
    public static final DeferredRegister<EntityType<?>> ENTITIES = 
            DeferredRegister.create(ForgeRegistries.ENTITIES, InfinityGauntletMod.MOD_ID);
    
    public static final RegistryObject<EntityType<ThanosEntity>> THANOS = ENTITIES.register("thanos",
            () -> EntityType.Builder.of(ThanosEntity::new, EntityClassification.MONSTER)
                    .sized(1.2F, 2.8F) // Slightly larger than player
                    .fireImmune()
                    .clientTrackingRange(64)
                    .build("thanos"));
    
    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
