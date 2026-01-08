package com.schematicsbuilder.events;

import com.schematicsbuilder.SchematicsBuilderMod;
import com.schematicsbuilder.commands.SchematicCommands;
import com.schematicsbuilder.schematic.SchematicManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for server-side events
 */
@Mod.EventBusSubscriber(modid = SchematicsBuilderMod.MOD_ID)
public class ModEventHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SchematicCommands.register(event.getDispatcher());
        SchematicsBuilderMod.LOGGER.info("Registered schematic commands!");
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        // Tick all active builders
        SchematicManager.tickBuilders();
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        SchematicManager.onPlayerDisconnect(event.getPlayer().getUUID());
    }
}
