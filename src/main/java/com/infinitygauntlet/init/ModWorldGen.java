package com.infinitygauntlet.init;

import com.infinitygauntlet.InfinityGauntletMod;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = InfinityGauntletMod.MOD_ID)
public class ModWorldGen {
    
    public static ConfiguredFeature<?, ?> VIBRANIUM_ORE_FEATURE;

    public static void registerConfiguredFeatures() {
        VIBRANIUM_ORE_FEATURE = Feature.ORE.configured(
                new OreFeatureConfig(
                        OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                        ModBlocks.VIBRANIUM_ORE.get().defaultBlockState(),
                        3 // Vein size - very small
                ))
                .range(16) // Max height Y=16 (deep underground)
                .squared()
                .count(1); // Very rare - 1 vein per chunk

        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE,
                new ResourceLocation(InfinityGauntletMod.MOD_ID, "vibranium_ore"),
                VIBRANIUM_ORE_FEATURE);
    }

    @SubscribeEvent
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (event.getCategory() != Biome.Category.NETHER && 
            event.getCategory() != Biome.Category.THEEND) {
            BiomeGenerationSettingsBuilder generation = event.getGeneration();
            generation.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, VIBRANIUM_ORE_FEATURE);
        }
    }
}
