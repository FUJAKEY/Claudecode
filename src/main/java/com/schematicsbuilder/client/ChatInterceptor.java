package com.schematicsbuilder.client;

import com.schematicsbuilder.SchematicsBuilderMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Intercepts chat messages to handle /schem commands client-side
 */
@Mod.EventBusSubscriber(modid = SchematicsBuilderMod.MOD_ID, value = Dist.CLIENT)
public class ChatInterceptor {

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();

        // Check if it's a /schem command
        if (message.startsWith("/schem")) {
            // Process client-side
            if (ClientCommands.processCommand(message)) {
                // Cancel sending to server - we handled it
                event.setCanceled(true);
            }
        }
    }
}
