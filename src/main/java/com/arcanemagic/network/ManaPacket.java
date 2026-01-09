package com.arcanemagic.network;

import com.arcanemagic.client.ClientManaData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for syncing mana data from server to client
 */
public class ManaPacket {

    private final int mana;
    private final int maxMana;

    public ManaPacket(int mana, int maxMana) {
        this.mana = mana;
        this.maxMana = maxMana;
    }

    public static void encode(ManaPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.mana);
        buffer.writeInt(packet.maxMana);
    }

    public static ManaPacket decode(PacketBuffer buffer) {
        return new ManaPacket(buffer.readInt(), buffer.readInt());
    }

    public static void handle(ManaPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Handle on client side
            ClientManaData.setMana(packet.mana);
            ClientManaData.setMaxMana(packet.maxMana);
        });
        ctx.get().setPacketHandled(true);
    }
}
