package com.schematicsbuilder.schematic;

import com.schematicsbuilder.SchematicsBuilderMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages schematic loading, storage, and active builders
 */
public class SchematicManager {

    private static final Map<UUID, SchematicData> loadedSchematics = new ConcurrentHashMap<>();
    private static final Map<UUID, AutoBuilder> activeBuilders = new ConcurrentHashMap<>();
    private static final Map<UUID, SchematicData> selectedSchematics = new ConcurrentHashMap<>();

    /**
     * Get list of available schematic files
     */
    public static List<File> getAvailableSchematics() {
        List<File> files = new ArrayList<>();
        File folder = SchematicsBuilderMod.schematicsFolder;

        if (folder != null && folder.exists()) {
            File[] found = folder.listFiles((dir, name) -> name.endsWith(".schematic") || name.endsWith(".schem")
                    || name.endsWith(".litematic"));
            if (found != null) {
                files.addAll(Arrays.asList(found));
            }
        }

        return files;
    }

    /**
     * Load a schematic from file
     */
    public static SchematicData loadSchematic(UUID playerId, String filename) {
        try {
            File file = new File(SchematicsBuilderMod.schematicsFolder, filename);
            if (!file.exists()) {
                SchematicsBuilderMod.LOGGER.error("Schematic not found: " + filename);
                return null;
            }

            SchematicData data = SchematicData.load(file);
            loadedSchematics.put(playerId, data);
            selectedSchematics.put(playerId, data);

            SchematicsBuilderMod.LOGGER.info("Loaded schematic: " + data.getName() +
                    " (" + data.getWidth() + "x" + data.getHeight() + "x" + data.getLength() +
                    ", " + data.getBlockCount() + " blocks)");

            return data;
        } catch (Exception e) {
            SchematicsBuilderMod.LOGGER.error("Failed to load schematic: " + filename, e);
            return null;
        }
    }

    /**
     * Get currently selected schematic for player
     */
    public static SchematicData getSelected(UUID playerId) {
        return selectedSchematics.get(playerId);
    }

    /**
     * Set schematic origin position
     */
    public static void setOrigin(UUID playerId, BlockPos pos) {
        SchematicData data = selectedSchematics.get(playerId);
        if (data != null) {
            data.setOrigin(pos);
        }
    }

    /**
     * Rotate selected schematic
     */
    public static void rotate(UUID playerId) {
        SchematicData data = selectedSchematics.get(playerId);
        if (data != null) {
            data.rotate90();
        }
    }

    /**
     * Start auto-building
     */
    public static boolean startBuild(ServerPlayerEntity player) {
        SchematicData data = selectedSchematics.get(player.getUUID());
        if (data == null) {
            return false;
        }

        // Stop existing builder
        stopBuild(player);

        AutoBuilder builder = new AutoBuilder(player, data);
        activeBuilders.put(player.getUUID(), builder);
        builder.start();

        return true;
    }

    /**
     * Stop auto-building
     */
    public static void stopBuild(ServerPlayerEntity player) {
        AutoBuilder builder = activeBuilders.remove(player.getUUID());
        if (builder != null) {
            builder.stop();
        }
    }

    /**
     * Toggle pause on current build
     */
    public static void togglePause(ServerPlayerEntity player) {
        AutoBuilder builder = activeBuilders.get(player.getUUID());
        if (builder != null) {
            builder.togglePause();
        }
    }

    /**
     * Get active builder for player
     */
    public static AutoBuilder getBuilder(UUID playerId) {
        return activeBuilders.get(playerId);
    }

    /**
     * Tick all active builders
     */
    public static void tickBuilders() {
        Iterator<Map.Entry<UUID, AutoBuilder>> it = activeBuilders.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, AutoBuilder> entry = it.next();
            if (!entry.getValue().tick()) {
                it.remove();
            }
        }
    }

    /**
     * Cleanup on player disconnect
     */
    public static void onPlayerDisconnect(UUID playerId) {
        loadedSchematics.remove(playerId);
        selectedSchematics.remove(playerId);
        AutoBuilder builder = activeBuilders.remove(playerId);
        if (builder != null) {
            builder.stop();
        }
    }
}
