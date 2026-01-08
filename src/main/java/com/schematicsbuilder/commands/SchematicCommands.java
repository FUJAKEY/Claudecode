package com.schematicsbuilder.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.schematicsbuilder.network.LoadSchematicPacket;
import com.schematicsbuilder.schematic.AutoBuilder;
import com.schematicsbuilder.schematic.SchematicData;
import com.schematicsbuilder.schematic.SchematicManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.File;
import java.util.List;

/**
 * Commands for schematic operations
 */
public class SchematicCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("schem")
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            listSchematics(ctx.getSource());
                            return 1;
                        }))
                .then(Commands.literal("load")
                        .then(Commands.argument("filename", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String filename = StringArgumentType.getString(ctx, "filename");
                                    loadSchematic(ctx.getSource(), filename);
                                    return 1;
                                })))
                .then(Commands.literal("pos")
                        .executes(ctx -> {
                            setPosition(ctx.getSource());
                            return 1;
                        }))
                .then(Commands.literal("build")
                        .executes(ctx -> {
                            startBuild(ctx.getSource());
                            return 1;
                        }))
                .then(Commands.literal("stop")
                        .executes(ctx -> {
                            stopBuild(ctx.getSource());
                            return 1;
                        }))
                .then(Commands.literal("pause")
                        .executes(ctx -> {
                            pauseBuild(ctx.getSource());
                            return 1;
                        }))
                .then(Commands.literal("rotate")
                        .executes(ctx -> {
                            rotate(ctx.getSource());
                            return 1;
                        }))
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            showStatus(ctx.getSource());
                            return 1;
                        }))
                .then(Commands.literal("help")
                        .executes(ctx -> {
                            showHelp(ctx.getSource());
                            return 1;
                        })));
    }

    private static void listSchematics(CommandSource source) {
        List<File> files = SchematicManager.getAvailableSchematics();
        source.sendSuccess(new StringTextComponent("═══ Schematics (" + files.size() + ") ═══")
                .withStyle(TextFormatting.GOLD), false);

        if (files.isEmpty()) {
            source.sendSuccess(new StringTextComponent("No schematics found!")
                    .withStyle(TextFormatting.RED), false);
            source.sendSuccess(new StringTextComponent("Put files in: schematics/")
                    .withStyle(TextFormatting.GRAY), false);
        } else {
            for (File f : files) {
                source.sendSuccess(new StringTextComponent(" • " + f.getName())
                        .withStyle(TextFormatting.YELLOW), false);
            }
        }
    }

    private static void loadSchematic(CommandSource source, String filename) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        SchematicData data = SchematicManager.loadSchematic(player.getUUID(), filename);
        if (data != null) {
            SchematicManager.setOrigin(player.getUUID(), player.blockPosition());
            source.sendSuccess(new StringTextComponent("✓ Loaded: " + data.getName() +
                    " (" + data.getBlockCount() + " blocks)")
                    .withStyle(TextFormatting.GREEN), false);
        } else {
            source.sendFailure(new StringTextComponent("✗ Failed to load: " + filename));
        }
    }

    private static void setPosition(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        SchematicManager.setOrigin(player.getUUID(), player.blockPosition());
        source.sendSuccess(new StringTextComponent("📍 Position set to: " + player.blockPosition().toShortString())
                .withStyle(TextFormatting.GREEN), false);
    }

    private static void startBuild(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        if (SchematicManager.startBuild(player)) {
            // Started - messages handled by AutoBuilder
        } else {
            source.sendFailure(new StringTextComponent("No schematic loaded!"));
        }
    }

    private static void stopBuild(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        SchematicManager.stopBuild(player);
    }

    private static void pauseBuild(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        SchematicManager.togglePause(player);
    }

    private static void rotate(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        SchematicManager.rotate(player.getUUID());
        SchematicData data = SchematicManager.getSelected(player.getUUID());
        if (data != null) {
            source.sendSuccess(new StringTextComponent("⟳ Rotated: " + data.getRotation() + "°")
                    .withStyle(TextFormatting.AQUA), false);
        }
    }

    private static void showStatus(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        AutoBuilder builder = SchematicManager.getBuilder(player.getUUID());
        if (builder != null && builder.isRunning()) {
            source.sendSuccess(new StringTextComponent("Status: " + builder.getProgress() + "% | " +
                    builder.getBlocksPlaced() + "/" + builder.getTotalBlocks() + " blocks | Layer " +
                    builder.getCurrentLayer() + "/" + builder.getMaxLayer())
                    .withStyle(TextFormatting.GREEN), false);
        } else {
            SchematicData data = SchematicManager.getSelected(player.getUUID());
            if (data != null) {
                source.sendSuccess(new StringTextComponent("Selected: " + data.getName() +
                        " at " + data.getOrigin().toShortString() + " (" + data.getRotation() + "°)")
                        .withStyle(TextFormatting.YELLOW), false);
            } else {
                source.sendSuccess(new StringTextComponent("No schematic loaded")
                        .withStyle(TextFormatting.GRAY), false);
            }
        }
    }

    private static void showHelp(CommandSource source) {
        source.sendSuccess(new StringTextComponent("═══ Schematic Builder Commands ═══")
                .withStyle(TextFormatting.GOLD), false);
        source.sendSuccess(new StringTextComponent("/schem list - List available schematics")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem load <file> - Load a schematic")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem pos - Set position to current location")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem rotate - Rotate 90 degrees")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem build - Start auto-building")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem pause - Pause/resume building")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem stop - Stop building")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem status - Show current status")
                .withStyle(TextFormatting.YELLOW), false);
    }
}
