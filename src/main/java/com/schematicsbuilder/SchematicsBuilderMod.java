package com.schematicsbuilder;

import com.schematicsbuilder.client.ModKeyBindings;
import com.schematicsbuilder.network.ModNetworking;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Schematics Builder - Auto-build schematics with AI precision
 * Supports .schematic and .litematic formats
 */
@Mod(SchematicsBuilderMod.MOD_ID)
public class SchematicsBuilderMod {
    public static final String MOD_ID = "schematicsbuilder";
    public static final Logger LOGGER = LogManager.getLogger();

    public static File schematicsFolder;

    public SchematicsBuilderMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("╔════════════════════════════════════╗");
        LOGGER.info("║  Schematics Builder v1.0.0         ║");
        LOGGER.info("║  Auto-build with AI precision!     ║");
        LOGGER.info("╚════════════════════════════════════╝");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetworking.register();

            // Create schematics folder
            schematicsFolder = new File("schematics");
            if (!schematicsFolder.exists()) {
                schematicsFolder.mkdirs();
                LOGGER.info("Created schematics folder: " + schematicsFolder.getAbsolutePath());
            }
        });

        LOGGER.info("Schematics Builder initialized!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ModKeyBindings.register();
        LOGGER.info("Client keybindings registered!");
        LOGGER.info("  Press O to open Schematics Menu");
        LOGGER.info("  Press P to toggle preview");
        LOGGER.info("  Press [ ] to rotate schematic");
    }
}
