package com.infinitygauntlet;

import com.infinitygauntlet.init.ModBlocks;
import com.infinitygauntlet.init.ModItems;
import com.infinitygauntlet.init.ModWorldGen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(InfinityGauntletMod.MOD_ID)
public class InfinityGauntletMod {
    public static final String MOD_ID = "infinitygauntlet";
    public static final Logger LOGGER = LogManager.getLogger();

    public InfinityGauntletMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register items and blocks
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        
        // Setup event
        modEventBus.addListener(this::commonSetup);
        
        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("Infinity Gauntlet Mod v2.0 initialized!");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModWorldGen.registerConfiguredFeatures();
            LOGGER.info("Vibranium ore generation registered!");
        });
    }
}
