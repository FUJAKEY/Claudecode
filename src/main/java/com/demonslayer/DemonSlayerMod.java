package com.demonslayer;

import com.demonslayer.client.ModKeyBindings;
import com.demonslayer.config.ModConfig;
import com.demonslayer.init.ModItems;
import com.demonslayer.init.ModEntities;
import com.demonslayer.init.ModEffects;
import com.demonslayer.init.ModParticles;
import com.demonslayer.network.ModNetworking;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Demon Slayer Mod - Kimetsu no Yaiba
 * Final Release v4.0 - Multiplayer Ready
 */
@Mod(DemonSlayerMod.MOD_ID)
public class DemonSlayerMod {
    public static final String MOD_ID = "demonslayer";
    public static final Logger LOGGER = LogManager.getLogger();

    public DemonSlayerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register configuration
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC, "demonslayer-common.toml");

        // Register items, entities, effects, particles
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        ModParticles.register(modEventBus);

        // Setup events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("═══════════════════════════════════════");
        LOGGER.info("  鬼滅の刃 Demon Slayer Mod v4.0 FINAL");
        LOGGER.info("  Multiplayer Ready | Full Featured");
        LOGGER.info("═══════════════════════════════════════");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register entity attributes
            ModEntities.registerAttributes();

            // Register network packets
            ModNetworking.register();
        });

        LOGGER.info("Breathing techniques initialized!");
        LOGGER.info("Network packets registered!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Register keybindings
        ModKeyBindings.register();

        LOGGER.info("Client keybindings registered!");
        LOGGER.info("  V - Switch Form Forward");
        LOGGER.info("  B - Switch Form Backward");
        LOGGER.info("  M - Activate Slayer Mark");
        LOGGER.info("  J - Quest Menu");
        LOGGER.info("  K - Show Stats");
    }
}
