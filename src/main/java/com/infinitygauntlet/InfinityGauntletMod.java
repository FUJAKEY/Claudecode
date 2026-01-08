package com.infinitygauntlet;

import com.infinitygauntlet.config.ModConfiguration;
import com.infinitygauntlet.entity.ThanosEntity;
import com.infinitygauntlet.init.ModBlocks;
import com.infinitygauntlet.init.ModEntities;
import com.infinitygauntlet.init.ModItems;
import com.infinitygauntlet.init.ModWorldGen;
import com.infinitygauntlet.network.ModPackets;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
        
        // Register config
        ModConfiguration.register();
        
        // Register items, blocks, entities
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        
        // Setup events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        
        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("Infinity Gauntlet Mod v3.0 initialized!");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register world gen
            ModWorldGen.registerConfiguredFeatures();
            
            // Register entity attributes
            GlobalEntityTypeAttributes.put(ModEntities.THANOS.get(), ThanosEntity.createAttributes().build());
            
            // Register network packets
            ModPackets.register();
            
            LOGGER.info("Thanos entity and world generation registered!");
        });
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register keybindings
            com.infinitygauntlet.client.ModKeyBindings.register();
            LOGGER.info("Client setup complete - keybindings registered!");
        });
    }
}
