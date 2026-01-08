package com.schematicsbuilder.network;

import com.schematicsbuilder.SchematicsBuilderMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Network handler for client-server communication
 */
public class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SchematicsBuilderMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    public static void register() {
        // Client -> Server
        CHANNEL.registerMessage(packetId++, SchematicActionPacket.class,
                SchematicActionPacket::encode, SchematicActionPacket::decode,
                SchematicActionPacket::handle);

        CHANNEL.registerMessage(packetId++, LoadSchematicPacket.class,
                LoadSchematicPacket::encode, LoadSchematicPacket::decode,
                LoadSchematicPacket::handle);

        CHANNEL.registerMessage(packetId++, SetPositionPacket.class,
                SetPositionPacket::encode, SetPositionPacket::decode,
                SetPositionPacket::handle);

        SchematicsBuilderMod.LOGGER.info("Network packets registered!");
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayerEntity player) {
        CHANNEL.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
