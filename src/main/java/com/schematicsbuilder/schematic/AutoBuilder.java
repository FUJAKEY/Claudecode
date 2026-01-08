package com.schematicsbuilder.schematic;

import com.schematicsbuilder.SchematicsBuilderMod;
import com.schematicsbuilder.util.PathfindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

/**
 * Auto-Builder - Builds schematics layer by layer with AI optimization
 * Now with resource chest support!
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

    // Resource fetching
    private boolean fetchingResources = false;
    private BlockPos returnPosition = null;
    private BlockState neededBlock = null;
    private BlockPos targetChest = null;
    private int fetchTickCounter = 0;
    private boolean atChest = false;
    private boolean autoFetchEnabled = true;

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

        List<BlockPos> chests = ResourceChestManager.getLinkedChests(player.getUUID());

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
        if (!chests.isEmpty()) {
            player.displayClientMessage(
                    new StringTextComponent("  📦 Resource Chests: " + chests.size() + " linked")
                            .withStyle(TextFormatting.AQUA),
                    false);
        } else {
            player.displayClientMessage(
                    new StringTextComponent("  ⚠ No resource chests! Use /schem chest link")
                            .withStyle(TextFormatting.YELLOW),
                    false);
        }
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
        fetchingResources = false;

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

        // If fetching resources, handle that first
        if (fetchingResources) {
            tickFetchResources();
            return running;
        }

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
                if (hasBlockInInventory(task.state)) {
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
                    // Try to fetch from chest
                    if (autoFetchEnabled && tryStartFetch(task.state)) {
                        return running;
                    } else {
                        // No chests or can't fetch - pause
                        player.displayClientMessage(
                                new StringTextComponent("⚠ Missing: " + task.state.getBlock().getName().getString())
                                        .withStyle(TextFormatting.RED),
                                true);
                        paused = true;
                        return running;
                    }
                }
            } else {
                // Position blocked, skip for now
                buildQueue.poll();
                buildQueue.add(task);
            }
        }

        return running;
    }

    /**
     * Try to start fetching resources from chest
     */
    private boolean tryStartFetch(BlockState needed) {
        BlockPos chestPos = ResourceChestManager.findChestWithBlock(player, needed);

        if (chestPos == null) {
            return false; // No chest with this item
        }

        // Start fetching
        fetchingResources = true;
        neededBlock = needed;
        targetChest = chestPos;
        returnPosition = player.blockPosition();
        atChest = false;
        fetchTickCounter = 0;

        player.displayClientMessage(
                new StringTextComponent("🏃 Going to chest for: " + needed.getBlock().getName().getString())
                        .withStyle(TextFormatting.AQUA),
                true);

        return true;
    }

    /**
     * Handle resource fetching tick
     */
    private void tickFetchResources() {
        fetchTickCounter++;

        // Timeout after 30 seconds
        if (fetchTickCounter > 600) {
            player.displayClientMessage(
                    new StringTextComponent("❌ Failed to reach chest! Pausing...")
                            .withStyle(TextFormatting.RED),
                    true);
            fetchingResources = false;
            paused = true;
            return;
        }

        if (!atChest) {
            // Move towards chest
            BlockPos standPos = PathfindingHelper.findOpenSpotNear(world, targetChest);
            boolean arrived = PathfindingHelper.moveTowards(player, standPos, 0.4);

            if (arrived) {
                atChest = true;
                fetchTickCounter = 0;

                player.displayClientMessage(
                        new StringTextComponent("📦 At chest, grabbing items...")
                                .withStyle(TextFormatting.GREEN),
                        true);
            }
        } else {
            // At chest - take items
            // Wait a moment then take
            if (fetchTickCounter > 20) {
                int taken = ResourceChestManager.takeBlocksFromChest(
                        player, targetChest, neededBlock, 64);

                if (taken > 0) {
                    player.displayClientMessage(
                            new StringTextComponent("✓ Took " + taken + "x " +
                                    neededBlock.getBlock().getName().getString())
                                    .withStyle(TextFormatting.GREEN),
                            true);

                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }

                // Now return to build position
                player.displayClientMessage(
                        new StringTextComponent("🏃 Returning to build site...")
                                .withStyle(TextFormatting.AQUA),
                        true);

                // Simple teleport back (could make walking version later)
                player.teleportTo(returnPosition.getX() + 0.5,
                        returnPosition.getY(),
                        returnPosition.getZ() + 0.5);

                fetchingResources = false;
                atChest = false;
                neededBlock = null;
                targetChest = null;
            }
        }
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

    public boolean isFetching() {
        return fetchingResources;
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

    public void setAutoFetch(boolean enabled) {
        this.autoFetchEnabled = enabled;
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
