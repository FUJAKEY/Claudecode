package com.schematicsbuilder.network;

import com.schematicsbuilder.schematic.SchematicData;
import com.schematicsbuilder.schematic.SchematicManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for setting schematic position
 */
public class SetPositionPacket {

    private final BlockPos pos;

    public SetPositionPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(SetPositionPacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static SetPositionPacket decode(PacketBuffer buffer) {
        return new SetPositionPacket(buffer.readBlockPos());
    }

    public static void handle(SetPositionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null)
                return;

            SchematicManager.setOrigin(player.getUUID(), packet.pos);

            SchematicData data = SchematicManager.getSelected(player.getUUID());
            if (data != null) {
                player.displayClientMessage(
                        new StringTextComponent("📍 Position set to: " + packet.pos.toShortString())
                                .withStyle(TextFormatting.GREEN),
                        true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
