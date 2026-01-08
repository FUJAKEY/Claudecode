package com.schematicsbuilder.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.schematicsbuilder.schematic.AutoBuilder;
import com.schematicsbuilder.schematic.ResourceChestManager;
import com.schematicsbuilder.schematic.SchematicData;
import com.schematicsbuilder.schematic.SchematicManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.File;
import java.util.List;

/**
 * Commands for schematic operations with chest support
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
                // CHEST COMMANDS
                .then(Commands.literal("chest")
                        .then(Commands.literal("link")
                                .executes(ctx -> {
                                    linkChest(ctx.getSource());
                                    return 1;
                                }))
                        .then(Commands.literal("unlink")
                                .executes(ctx -> {
                                    unlinkChest(ctx.getSource());
                                    return 1;
                                }))
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                    listChests(ctx.getSource());
                                    return 1;
                                }))
                        .then(Commands.literal("clear")
                                .executes(ctx -> {
                                    clearChests(ctx.getSource());
                                    return 1;
                                })))
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
            String status = builder.isFetching() ? " (fetching)" : "";
            source.sendSuccess(new StringTextComponent("Status: " + builder.getProgress() + "%" + status + " | " +
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

        // Show chest info
        List<BlockPos> chests = ResourceChestManager.getLinkedChests(player.getUUID());
        if (!chests.isEmpty()) {
            source.sendSuccess(new StringTextComponent("📦 Resource chests: " + chests.size() + " linked")
                    .withStyle(TextFormatting.AQUA), false);
        }
    }

    // ========== CHEST COMMANDS ==========

    private static void linkChest(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        // Raycast to find looked-at block
        BlockPos target = getLookedAtBlock(player, 5.0);

        if (target == null) {
            source.sendFailure(new StringTextComponent("Look at a chest to link it!"));
            return;
        }

        ResourceChestManager.linkChest(player, target);
    }

    private static void unlinkChest(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        BlockPos target = getLookedAtBlock(player, 5.0);

        if (target == null) {
            source.sendFailure(new StringTextComponent("Look at a chest to unlink it!"));
            return;
        }

        ResourceChestManager.unlinkChest(player, target);
    }

    private static void listChests(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        ResourceChestManager.listChests(player);
    }

    private static void clearChests(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity))
            return;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        ResourceChestManager.clearAllChests(player);
    }

    /**
     * Get block player is looking at
     */
    private static BlockPos getLookedAtBlock(ServerPlayerEntity player, double maxDistance) {
        Vector3d eyePos = player.getEyePosition(1.0F);
        Vector3d lookVec = player.getLookAngle();
        Vector3d endPos = eyePos.add(lookVec.scale(maxDistance));

        BlockRayTraceResult result = player.level.clip(new RayTraceContext(
                eyePos, endPos,
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                player));

        if (result.getType() == RayTraceResult.Type.BLOCK) {
            return result.getBlockPos();
        }
        return null;
    }

    private static void showHelp(CommandSource source) {
        source.sendSuccess(new StringTextComponent("═══ Schematic Builder Commands ═══")
                .withStyle(TextFormatting.GOLD), false);
        source.sendSuccess(new StringTextComponent("/schem list - List schematics")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem load <file> - Load schematic")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem pos - Set build position")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem rotate - Rotate 90°")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem build - Start building")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem pause - Pause/resume")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem stop - Stop building")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("")
                .withStyle(TextFormatting.GRAY), false);
        source.sendSuccess(new StringTextComponent("═══ Resource Chest Commands ═══")
                .withStyle(TextFormatting.AQUA), false);
        source.sendSuccess(new StringTextComponent("/schem chest link - Link looked-at chest")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem chest unlink - Unlink chest")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem chest list - List all chests")
                .withStyle(TextFormatting.YELLOW), false);
        source.sendSuccess(new StringTextComponent("/schem chest clear - Unlink all")
                .withStyle(TextFormatting.YELLOW), false);
    }
}
