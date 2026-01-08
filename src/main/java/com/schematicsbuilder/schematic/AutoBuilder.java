package com.schematicsbuilder.schematic;

import com.schematicsbuilder.SchematicsBuilderMod;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

/**
 * Auto-Builder - Builds schematics layer by layer with AI optimization
 */
public class AutoBuilder {

    private final ServerPlayerEntity player;
    private final SchematicData schematic;
    private final ServerWorld world;

    private final Queue<BuildTask> buildQueue;
    private int currentLayer = 0;
    private int maxLayer;
    private int blocksPlaced = 0;
    private int totalBlocks;
    private boolean running = false;
    private boolean paused = false;

    // Build settings
    private int blocksPerTick = 1;
    private int tickDelay = 2;
    private int tickCounter = 0;

    public AutoBuilder(ServerPlayerEntity player, SchematicData schematic) {
        this.player = player;
        this.schematic = schematic;
        this.world = (ServerWorld) player.level;
        this.buildQueue = new LinkedList<>();
        this.maxLayer = schematic.getHeight();
        this.totalBlocks = schematic.getBlockCount();

        // Pre-calculate all tasks
        loadAllLayers();
    }

    private void loadAllLayers() {
        buildQueue.clear();
        for (int y = 0; y < maxLayer; y++) {
            List<Map.Entry<BlockPos, BlockState>> layerBlocks = schematic.getBlocksAtLayer(y);
            for (Map.Entry<BlockPos, BlockState> entry : layerBlocks) {
                BlockPos worldPos = schematic.toWorldPos(entry.getKey());
                buildQueue.add(new BuildTask(worldPos, entry.getValue(), y));
            }
        }
        totalBlocks = buildQueue.size();
        SchematicsBuilderMod.LOGGER.info("Loaded " + totalBlocks + " blocks to build across " + maxLayer + " layers");
    }

    /**
     * Start the auto-build process
     */
    public void start() {
        if (running)
            return;
        running = true;
        paused = false;

        player.displayClientMessage(
                new StringTextComponent("═══════════════════════════════════")
                        .withStyle(TextFormatting.GREEN),
                false);
        player.displayClientMessage(
                new StringTextComponent("  ▶ Auto-Build Started!")
                        .withStyle(TextFormatting.GREEN).withStyle(TextFormatting.BOLD),
                false);
        player.displayClientMessage(
                new StringTextComponent("  Schematic: " + schematic.getName())
                        .withStyle(TextFormatting.YELLOW),
                false);
        player.displayClientMessage(
                new StringTextComponent("  Blocks: " + totalBlocks + " | Layers: " + maxLayer)
                        .withStyle(TextFormatting.GRAY),
                false);
        player.displayClientMessage(
                new StringTextComponent("═══════════════════════════════════")
                        .withStyle(TextFormatting.GREEN),
                false);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0F, 2.0F);
    }

    /**
     * Pause/resume building
     */
    public void togglePause() {
        paused = !paused;
        player.displayClientMessage(
                new StringTextComponent(paused ? "⏸ Build Paused" : "▶ Build Resumed")
                        .withStyle(paused ? TextFormatting.YELLOW : TextFormatting.GREEN),
                true);
    }

    /**
     * Stop building completely
     */
    public void stop() {
        running = false;
        paused = false;

        player.displayClientMessage(
                new StringTextComponent("⏹ Build Stopped! Placed " + blocksPlaced + "/" + totalBlocks + " blocks")
                        .withStyle(TextFormatting.RED),
                false);
    }

    /**
     * Called every server tick to process building
     */
    public boolean tick() {
        if (!running || paused)
            return running;

        tickCounter++;
        if (tickCounter < tickDelay)
            return running;
        tickCounter = 0;

        // Check if player has required blocks
        for (int i = 0; i < blocksPerTick; i++) {
            if (buildQueue.isEmpty()) {
                complete();
                return false;
            }

            BuildTask task = buildQueue.peek();
            if (task == null)
                continue;

            // Check if we can place this block
            if (canPlaceBlock(task)) {
                placeBlock(task);
                buildQueue.poll();
                blocksPlaced++;

                // Update layer
                if (task.layer > currentLayer) {
                    currentLayer = task.layer;
                    player.displayClientMessage(
                            new StringTextComponent("📦 Layer " + (currentLayer + 1) + "/" + maxLayer + " | " +
                                    getProgress() + "% complete")
                                    .withStyle(TextFormatting.AQUA),
                            true);
                }
            } else {
                // Can't place - check inventory
                if (!hasBlockInInventory(task.state)) {
                    player.displayClientMessage(
                            new StringTextComponent("⚠ Missing: " + task.state.getBlock().getName().getString())
                                    .withStyle(TextFormatting.RED),
                            true);
                    paused = true;
                    return running;
                }
                // Position blocked, skip for now
                buildQueue.poll();
                buildQueue.add(task);
            }
        }

        return running;
    }

    private boolean canPlaceBlock(BuildTask task) {
        // Check if position is air or replaceable
        BlockState current = world.getBlockState(task.pos);
        return current.isAir() || current.getMaterial().isReplaceable();
    }

    private boolean hasBlockInInventory(BlockState state) {
        if (player.isCreative())
            return true;

        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == state.getBlock().asItem()) {
                return true;
            }
        }
        return false;
    }

    private void placeBlock(BuildTask task) {
        // Consume item from inventory (if not creative)
        if (!player.isCreative()) {
            for (int i = 0; i < player.inventory.getContainerSize(); i++) {
                ItemStack stack = player.inventory.getItem(i);
                if (!stack.isEmpty() && stack.getItem() == task.state.getBlock().asItem()) {
                    stack.shrink(1);
                    break;
                }
            }
        }

        // Place block
        world.setBlock(task.pos, task.state, 3);

        // Play sound occasionally
        if (blocksPlaced % 10 == 0) {
            world.playSound(null, task.pos, SoundEvents.STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);
        }
    }

    private void complete() {
        running = false;

        player.displayClientMessage(
                new StringTextComponent("═══════════════════════════════════")
                        .withStyle(TextFormatting.GREEN),
                false);
        player.displayClientMessage(
                new StringTextComponent("  ✓ BUILD COMPLETE!")
                        .withStyle(TextFormatting.GREEN).withStyle(TextFormatting.BOLD),
                false);
        player.displayClientMessage(
                new StringTextComponent("  Placed " + blocksPlaced + " blocks")
                        .withStyle(TextFormatting.YELLOW),
                false);
        player.displayClientMessage(
                new StringTextComponent("═══════════════════════════════════")
                        .withStyle(TextFormatting.GREEN),
                false);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    public int getProgress() {
        if (totalBlocks == 0)
            return 100;
        return (blocksPlaced * 100) / totalBlocks;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getBlocksPlaced() {
        return blocksPlaced;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public int getCurrentLayer() {
        return currentLayer;
    }

    public int getMaxLayer() {
        return maxLayer;
    }

    public void setSpeed(int blocksPerTick, int tickDelay) {
        this.blocksPerTick = Math.max(1, blocksPerTick);
        this.tickDelay = Math.max(1, tickDelay);
    }

    /**
     * Individual build task
     */
    private static class BuildTask {
        final BlockPos pos;
        final BlockState state;
        final int layer;

        BuildTask(BlockPos pos, BlockState state, int layer) {
            this.pos = pos;
            this.state = state;
            this.layer = layer;
        }
    }
}
