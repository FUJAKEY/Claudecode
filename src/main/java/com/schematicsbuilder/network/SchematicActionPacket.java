package com.schematicsbuilder.network;

import com.schematicsbuilder.schematic.AutoBuilder;
import com.schematicsbuilder.schematic.SchematicData;
import com.schematicsbuilder.schematic.SchematicManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

/**
 * Packet for schematic actions (start/stop/pause/list/rotate)
 */
public class SchematicActionPacket {

    public enum Action {
        LIST_SCHEMATICS,
        ROTATE,
        START_BUILD,
        STOP_BUILD,
        TOGGLE_PAUSE,
        GET_STATUS
    }

    private final Action action;
    private final String data;

    public SchematicActionPacket(Action action, String data) {
        this.action = action;
        this.data = data;
    }

    public static void encode(SchematicActionPacket packet, PacketBuffer buffer) {
        buffer.writeEnum(packet.action);
        buffer.writeUtf(packet.data);
    }

    public static SchematicActionPacket decode(PacketBuffer buffer) {
        return new SchematicActionPacket(buffer.readEnum(Action.class), buffer.readUtf(256));
    }

    public static void handle(SchematicActionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null)
                return;

            switch (packet.action) {
                case LIST_SCHEMATICS:
                    listSchematics(player);
                    break;
                case ROTATE:
                    rotate(player);
                    break;
                case START_BUILD:
                    startBuild(player);
                    break;
                case STOP_BUILD:
                    stopBuild(player);
                    break;
                case TOGGLE_PAUSE:
                    togglePause(player);
                    break;
                case GET_STATUS:
                    getStatus(player);
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void listSchematics(ServerPlayerEntity player) {
        List<File> files = SchematicManager.getAvailableSchematics();

        player.displayClientMessage(
                new StringTextComponent("═══ Available Schematics ═══")
                        .withStyle(TextFormatting.GOLD),
                false);

        if (files.isEmpty()) {
            player.displayClientMessage(
                    new StringTextComponent("No schematics found!")
                            .withStyle(TextFormatting.RED),
                    false);
            player.displayClientMessage(
                    new StringTextComponent("Put .schematic or .litematic files in: schematics/")
                            .withStyle(TextFormatting.GRAY),
                    false);
        } else {
            for (int i = 0; i < files.size(); i++) {
                File f = files.get(i);
                String name = f.getName();
                long size = f.length() / 1024;
                player.displayClientMessage(
                        new StringTextComponent(" " + (i + 1) + ". " + name + " (" + size + " KB)")
                                .withStyle(TextFormatting.YELLOW),
                        false);
            }
            player.displayClientMessage(
                    new StringTextComponent("Use /schem load <filename> to load")
                            .withStyle(TextFormatting.GRAY),
                    false);
        }
    }

    private static void rotate(ServerPlayerEntity player) {
        SchematicManager.rotate(player.getUUID());
        SchematicData data = SchematicManager.getSelected(player.getUUID());
        if (data != null) {
            player.displayClientMessage(
                    new StringTextComponent("⟳ Rotated to " + data.getRotation() + "°")
                            .withStyle(TextFormatting.AQUA),
                    true);
        } else {
            player.displayClientMessage(
                    new StringTextComponent("No schematic selected!")
                            .withStyle(TextFormatting.RED),
                    true);
        }
    }

    private static void startBuild(ServerPlayerEntity player) {
        if (SchematicManager.startBuild(player)) {
            // Started successfully - message handled by AutoBuilder
        } else {
            player.displayClientMessage(
                    new StringTextComponent("No schematic loaded! Use /schem load <file>")
                            .withStyle(TextFormatting.RED),
                    false);
        }
    }

    private static void stopBuild(ServerPlayerEntity player) {
        SchematicManager.stopBuild(player);
    }

    private static void togglePause(ServerPlayerEntity player) {
        SchematicManager.togglePause(player);
    }

    private static void getStatus(ServerPlayerEntity player) {
        AutoBuilder builder = SchematicManager.getBuilder(player.getUUID());
        if (builder != null && builder.isRunning()) {
            player.displayClientMessage(
                    new StringTextComponent("Building: " + builder.getProgress() + "% | Layer " +
                            builder.getCurrentLayer() + "/" + builder.getMaxLayer())
                            .withStyle(TextFormatting.GREEN),
                    false);
        } else {
            player.displayClientMessage(
                    new StringTextComponent("No build in progress")
                            .withStyle(TextFormatting.GRAY),
                    false);
        }
    }
}
