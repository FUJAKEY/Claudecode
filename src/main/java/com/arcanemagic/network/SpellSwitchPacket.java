package com.arcanemagic.network;

import com.arcanemagic.item.WandItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for synchronizing spell switching between client and server
 */
public class SpellSwitchPacket {

    private final boolean forward;

    public SpellSwitchPacket(boolean forward) {
        this.forward = forward;
    }

    public static void encode(SpellSwitchPacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.forward);
    }

    public static SpellSwitchPacket decode(PacketBuffer buf) {
        return new SpellSwitchPacket(buf.readBoolean());
    }

    public static void handle(SpellSwitchPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                // Check main hand first, then offhand
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();

                if (mainHand.getItem() instanceof WandItem) {
                    ((WandItem) mainHand.getItem()).cycleSpell(mainHand, msg.forward);
                } else if (offHand.getItem() instanceof WandItem) {
                    ((WandItem) offHand.getItem()).cycleSpell(offHand, msg.forward);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
