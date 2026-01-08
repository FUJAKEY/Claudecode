package com.demonslayer.init;

import com.demonslayer.DemonSlayerMod;
import com.demonslayer.entity.DemonEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = 
            DeferredRegister.create(ForgeRegistries.ENTITIES, DemonSlayerMod.MOD_ID);

    public static final RegistryObject<EntityType<DemonEntity>> DEMON = ENTITIES.register("demon",
            () -> EntityType.Builder.of(DemonEntity::new, EntityClassification.MONSTER)
                    .sized(0.8F, 2.0F)
                    .clientTrackingRange(48)
                    .build("demon"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
