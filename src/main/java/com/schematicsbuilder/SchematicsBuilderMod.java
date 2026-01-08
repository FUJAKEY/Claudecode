package com.schematicsbuilder;

import com.schematicsbuilder.client.ModKeyBindings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Schematics Builder - CLIENT-SIDE auto-builder
 * Works on ANY server without server-side mod!
 */
@Mod(SchematicsBuilderMod.MOD_ID)
public class SchematicsBuilderMod {
    public static final String MOD_ID = "schematicsbuilder";
    public static final Logger LOGGER = LogManager.getLogger();

    public static File schematicsFolder;

    public SchematicsBuilderMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("╔════════════════════════════════════╗");
        LOGGER.info("║  Schematics Builder v2.0.0         ║");
        LOGGER.info("║  CLIENT-SIDE Auto-Builder          ║");
        LOGGER.info("║  Works on ANY server!              ║");
        LOGGER.info("╚════════════════════════════════════╝");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Create schematics folder
        schematicsFolder = new File("schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
            LOGGER.info("Created schematics folder: " + schematicsFolder.getAbsolutePath());
        }

        LOGGER.info("Schematics Builder initialized!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ModKeyBindings.register();
        LOGGER.info("Client keybindings registered!");
        LOGGER.info("  Press O to list schematics");
        LOGGER.info("  Press B to start building");
        LOGGER.info("  Press N to stop building");
        LOGGER.info("  Press , to pause/resume");
    }
}
