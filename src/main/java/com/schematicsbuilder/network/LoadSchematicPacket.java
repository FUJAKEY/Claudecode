package com.schematicsbuilder.network;

import com.schematicsbuilder.schematic.SchematicData;
import com.schematicsbuilder.schematic.SchematicManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for loading a specific schematic file
 */
public class LoadSchematicPacket {

    private final String filename;

    public LoadSchematicPacket(String filename) {
        this.filename = filename;
    }

    public static void encode(LoadSchematicPacket packet, PacketBuffer buffer) {
        buffer.writeUtf(packet.filename);
    }

    public static LoadSchematicPacket decode(PacketBuffer buffer) {
        return new LoadSchematicPacket(buffer.readUtf(256));
    }

    public static void handle(LoadSchematicPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null)
                return;

            SchematicData data = SchematicManager.loadSchematic(player.getUUID(), packet.filename);

            if (data != null) {
                // Set origin to player position
                SchematicManager.setOrigin(player.getUUID(), player.blockPosition());

                player.displayClientMessage(
                        new StringTextComponent("═══════════════════════════════════")
                                .withStyle(TextFormatting.GREEN),
                        false);
                player.displayClientMessage(
                        new StringTextComponent("  ✓ Schematic Loaded!")
                                .withStyle(TextFormatting.GREEN).withStyle(TextFormatting.BOLD),
                        false);
                player.displayClientMessage(
                        new StringTextComponent("  Name: " + data.getName())
                                .withStyle(TextFormatting.YELLOW),
                        false);
                player.displayClientMessage(
                        new StringTextComponent(
                                "  Size: " + data.getWidth() + "x" + data.getHeight() + "x" + data.getLength())
                                .withStyle(TextFormatting.GRAY),
                        false);
                player.displayClientMessage(
                        new StringTextComponent("  Blocks: " + data.getBlockCount())
                                .withStyle(TextFormatting.GRAY),
                        false);
                player.displayClientMessage(
                        new StringTextComponent("  Origin: " + data.getOrigin().toShortString())
                                .withStyle(TextFormatting.GRAY),
                        false);
                player.displayClientMessage(
                        new StringTextComponent("═══════════════════════════════════")
                                .withStyle(TextFormatting.GREEN),
                        false);
                player.displayClientMessage(
                        new StringTextComponent("Press B to start building!")
                                .withStyle(TextFormatting.AQUA),
                        false);
            } else {
                player.displayClientMessage(
                        new StringTextComponent("✗ Failed to load schematic: " + packet.filename)
                                .withStyle(TextFormatting.RED),
                        false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
