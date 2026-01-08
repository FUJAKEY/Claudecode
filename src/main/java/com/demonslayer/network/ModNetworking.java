package com.demonslayer.network;

import com.demonslayer.DemonSlayerMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Network handler for multiplayer synchronization
 */
public class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DemonSlayerMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    public static void register() {
        // Client -> Server packets
        CHANNEL.registerMessage(packetId++, BreathingFormPacket.class,
                BreathingFormPacket::encode, BreathingFormPacket::decode,
                BreathingFormPacket::handle);

        CHANNEL.registerMessage(packetId++, SwitchFormPacket.class,
                SwitchFormPacket::encode, SwitchFormPacket::decode,
                SwitchFormPacket::handle);

        CHANNEL.registerMessage(packetId++, ActivateMarkPacket.class,
                ActivateMarkPacket::encode, ActivateMarkPacket::decode,
                ActivateMarkPacket::handle);

        CHANNEL.registerMessage(packetId++, StartQuestPacket.class,
                StartQuestPacket::encode, StartQuestPacket::decode,
                StartQuestPacket::handle);

        // Server -> Client packets
        CHANNEL.registerMessage(packetId++, SyncPlayerDataPacket.class,
                SyncPlayerDataPacket::encode, SyncPlayerDataPacket::decode,
                SyncPlayerDataPacket::handle);

        CHANNEL.registerMessage(packetId++, ParticleEffectPacket.class,
                ParticleEffectPacket::encode, ParticleEffectPacket::decode,
                ParticleEffectPacket::handle);

        DemonSlayerMod.LOGGER.info("Network packets registered for multiplayer!");
    }

    /**
     * Send packet to server
     */
    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    /**
     * Send packet to specific player
     */
    public static <MSG> void sendToPlayer(MSG message, ServerPlayerEntity player) {
        CHANNEL.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    /**
     * Send packet to all players in range
     */
    public static <MSG> void sendToAllNear(MSG message, ServerPlayerEntity source, double range) {
        for (ServerPlayerEntity player : source.getServer().getPlayerList().getPlayers()) {
            if (player.distanceTo(source) <= range) {
                sendToPlayer(message, player);
            }
        }
    }
}
