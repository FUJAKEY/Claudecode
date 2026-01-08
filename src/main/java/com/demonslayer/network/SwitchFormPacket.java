package com.demonslayer.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for switching breathing forms (Client -> Server)
 */
public class SwitchFormPacket {

    private final boolean forward;

    public SwitchFormPacket(boolean forward) {
        this.forward = forward;
    }

    public static void encode(SwitchFormPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.forward);
    }

    public static SwitchFormPacket decode(PacketBuffer buffer) {
        return new SwitchFormPacket(buffer.readBoolean());
    }

    public static void handle(SwitchFormPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                // Get current form from player's held item NBT
                net.minecraft.item.ItemStack stack = player.getMainHandItem();
                if (stack.getItem().getDescriptionId().contains("nichirin")) {
                    net.minecraft.nbt.CompoundNBT nbt = stack.getOrCreateTag();
                    int currentForm = nbt.getInt("current_form");
                    int maxForms = 6; // Most styles have 6 forms

                    if (packet.forward) {
                        currentForm = (currentForm + 1) % maxForms;
                    } else {
                        currentForm = (currentForm - 1 + maxForms) % maxForms;
                    }

                    nbt.putInt("current_form", currentForm);

                    player.displayClientMessage(
                            new StringTextComponent("Form " + (currentForm + 1) + " selected")
                                    .withStyle(TextFormatting.AQUA),
                            true);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
