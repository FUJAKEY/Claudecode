package com.demonslayer.network;

import com.demonslayer.systems.SlayerMarkSystem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for manually activating slayer mark (Client -> Server)
 */
public class ActivateMarkPacket {

    public ActivateMarkPacket() {
    }

    public static void encode(ActivateMarkPacket packet, PacketBuffer buffer) {
        // No data needed
    }

    public static ActivateMarkPacket decode(PacketBuffer buffer) {
        return new ActivateMarkPacket();
    }

    public static void handle(ActivateMarkPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                // Check if player has enough kills to activate manually
                if (SlayerMarkSystem.getDemonKills(player) >= 100) {
                    if (!SlayerMarkSystem.isMarkActive(player)) {
                        SlayerMarkSystem.activateMark(player);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
