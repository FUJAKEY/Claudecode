package com.demonslayer;

import com.demonslayer.init.ModItems;
import com.demonslayer.init.ModEntities;
import com.demonslayer.init.ModEffects;
import com.demonslayer.init.ModParticles;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DemonSlayerMod.MOD_ID)
public class DemonSlayerMod {
    public static final String MOD_ID = "demonslayer";
    public static final Logger LOGGER = LogManager.getLogger();

    public DemonSlayerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register items, entities, effects, particles
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        ModParticles.register(modEventBus);
        
        // Setup events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("Demon Slayer Mod initialized! 鬼滅の刃");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModEntities.registerAttributes();
        });
        LOGGER.info("Total Concentration Breathing ready!");
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup complete!");
    }
}
