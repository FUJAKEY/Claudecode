package com.arcanemagic.network;

import com.arcanemagic.ArcaneMagicMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Network handler for client-server synchronization
 */
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    public static SimpleChannel getChannel() {
        return INSTANCE;
    }

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(ArcaneMagicMod.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(
                packetId++,
                ManaPacket.class,
                ManaPacket::encode,
                ManaPacket::decode,
                ManaPacket::handle);

        ArcaneMagicMod.LOGGER.info("ArcaneMagic network registered!");
    }
}
