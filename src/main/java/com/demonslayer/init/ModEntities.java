package com.demonslayer.init;

import com.demonslayer.DemonSlayerMod;
import com.demonslayer.entity.DemonEntity;
import com.demonslayer.entity.MuzanEntity;
import com.demonslayer.entity.AkazaEntity;
import com.demonslayer.entity.KokushiboEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
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

    public static final RegistryObject<EntityType<MuzanEntity>> MUZAN = ENTITIES.register("muzan",
            () -> EntityType.Builder.of(MuzanEntity::new, EntityClassification.MONSTER)
                    .sized(1.0F, 2.5F)
                    .clientTrackingRange(64)
                    .fireImmune()
                    .build("muzan"));

    public static final RegistryObject<EntityType<AkazaEntity>> AKAZA = ENTITIES.register("akaza",
            () -> EntityType.Builder.of(AkazaEntity::new, EntityClassification.MONSTER)
                    .sized(1.0F, 2.3F)
                    .clientTrackingRange(64)
                    .fireImmune()
                    .build("akaza"));

    public static final RegistryObject<EntityType<KokushiboEntity>> KOKUSHIBO = ENTITIES.register("kokushibo",
            () -> EntityType.Builder.of(KokushiboEntity::new, EntityClassification.MONSTER)
                    .sized(1.0F, 2.5F)
                    .clientTrackingRange(64)
                    .fireImmune()
                    .build("kokushibo"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
    
    public static void registerAttributes() {
        GlobalEntityTypeAttributes.put(DEMON.get(), DemonEntity.createAttributes().build());
        GlobalEntityTypeAttributes.put(MUZAN.get(), MuzanEntity.createAttributes().build());
        GlobalEntityTypeAttributes.put(AKAZA.get(), AkazaEntity.createAttributes().build());
        GlobalEntityTypeAttributes.put(KOKUSHIBO.get(), KokushiboEntity.createAttributes().build());
    }
}
