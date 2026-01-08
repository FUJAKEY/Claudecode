package com.demonslayer.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for syncing player data to client (Server -> Client)
 */
public class SyncPlayerDataPacket {

    private final int xp;
    private final int rank;
    private final boolean markActive;
    private final int[] breathingLevels;

    public SyncPlayerDataPacket(int xp, int rank, boolean markActive, int[] breathingLevels) {
        this.xp = xp;
        this.rank = rank;
        this.markActive = markActive;
        this.breathingLevels = breathingLevels;
    }

    public static void encode(SyncPlayerDataPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.xp);
        buffer.writeInt(packet.rank);
        buffer.writeBoolean(packet.markActive);
        buffer.writeInt(packet.breathingLevels.length);
        for (int level : packet.breathingLevels) {
            buffer.writeInt(level);
        }
    }

    public static SyncPlayerDataPacket decode(PacketBuffer buffer) {
        int xp = buffer.readInt();
        int rank = buffer.readInt();
        boolean markActive = buffer.readBoolean();
        int len = buffer.readInt();
        int[] levels = new int[len];
        for (int i = 0; i < len; i++) {
            levels[i] = buffer.readInt();
        }
        return new SyncPlayerDataPacket(xp, rank, markActive, levels);
    }

    public static void handle(SyncPlayerDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side handling
            PlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                CompoundNBT data = player.getPersistentData();
                data.putInt("demonslayer_xp", packet.xp);
                data.putInt("demonslayer_rank", packet.rank);
                data.putBoolean("slayer_mark_active", packet.markActive);

                String[] keys = { "water_breathing_lvl", "flame_breathing_lvl", "thunder_breathing_lvl",
                        "wind_breathing_lvl", "mist_breathing_lvl", "love_breathing_lvl", "sun_breathing_lvl" };
                for (int i = 0; i < Math.min(packet.breathingLevels.length, keys.length); i++) {
                    data.putInt(keys[i], packet.breathingLevels[i]);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
