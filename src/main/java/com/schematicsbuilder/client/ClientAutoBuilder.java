package com.schematicsbuilder.client;

import com.schematicsbuilder.SchematicsBuilderMod;
import com.schematicsbuilder.schematic.SchematicData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

/**
 * CLIENT-SIDE Auto Builder
 * Works on ANY server - simulates real player actions
 */
public class ClientAutoBuilder {

    private static ClientAutoBuilder instance;

    private final Minecraft mc = Minecraft.getInstance();
    private SchematicData schematic;
    private Queue<BuildTask> buildQueue = new LinkedList<>();

    private boolean running = false;
    private boolean paused = false;

    private int currentLayer = 0;
    private int maxLayer = 0;
    private int blocksPlaced = 0;
    private int totalBlocks = 0;

    // Timing
    private int tickCounter = 0;
    private int placeDelay = 2; // ticks between placements (adjustable)

    // Movement
    private boolean movingToChest = false;
    private BlockPos targetChest = null;
    private BlockPos returnPos = null;
    private BlockState neededBlock = null;

    // Linked chests (client-side storage)
    private List<BlockPos> linkedChests = new ArrayList<>();

    public static ClientAutoBuilder getInstance() {
        if (instance == null) {
            instance = new ClientAutoBuilder();
        }
        return instance;
    }

    /**
     * Load schematic for building
     */
    public void loadSchematic(SchematicData data) {
        this.schematic = data;
        this.buildQueue.clear();

        // Pre-calculate all build tasks
        for (int y = 0; y < data.getHeight(); y++) {
            List<Map.Entry<BlockPos, BlockState>> layerBlocks = data.getBlocksAtLayer(y);
            for (Map.Entry<BlockPos, BlockState> entry : layerBlocks) {
                BlockPos worldPos = data.toWorldPos(entry.getKey());
                buildQueue.add(new BuildTask(worldPos, entry.getValue(), y));
            }
        }

        this.totalBlocks = buildQueue.size();
        this.maxLayer = data.getHeight();
        this.blocksPlaced = 0;
        this.currentLayer = 0;

        sendMessage("§a✓ Loaded: §e" + data.getName() + " §7(" + totalBlocks + " blocks, " + maxLayer + " layers)");
    }

    /**
     * Start building
     */
    public void start() {
        if (schematic == null) {
            sendMessage("§cNo schematic loaded! Use /schem load <file>");
            return;
        }

        if (running) {
            sendMessage("§eAlready building!");
            return;
        }

        running = true;
        paused = false;

        sendMessage("§a═══════════════════════════════════");
        sendMessage("§a§l  ▶ Auto-Build Started!");
        sendMessage("§e  Schematic: " + schematic.getName());
        sendMessage("§7  Blocks: " + totalBlocks + " | Layers: " + maxLayer);
        if (!linkedChests.isEmpty()) {
            sendMessage("§b  📦 Resource Chests: " + linkedChests.size() + " linked");
        }
        sendMessage("§a═══════════════════════════════════");
    }

    /**
     * Stop building
     */
    public void stop() {
        running = false;
        paused = false;
        movingToChest = false;
        sendMessage("§c⏹ Build Stopped! Placed " + blocksPlaced + "/" + totalBlocks);
    }

    /**
     * Pause/Resume
     */
    public void togglePause() {
        paused = !paused;
        sendMessage(paused ? "§e⏸ Build Paused" : "§a▶ Build Resumed");
    }

    /**
     * Called every client tick
     */
    public void onClientTick() {
        if (!running || paused)
            return;
        if (mc.player == null || mc.level == null)
            return;

        // Handle movement to chest
        if (movingToChest) {
            tickMovingToChest();
            return;
        }

        // Delay between placements
        tickCounter++;
        if (tickCounter < placeDelay)
            return;
        tickCounter = 0;

        // Process build queue
        if (buildQueue.isEmpty()) {
            complete();
            return;
        }

        BuildTask task = buildQueue.peek();
        if (task == null)
            return;

        // Check if position already has correct block
        BlockState current = mc.level.getBlockState(task.pos);
        if (current.getBlock() == task.state.getBlock()) {
            // Already placed, skip
            buildQueue.poll();
            blocksPlaced++;
            return;
        }

        // Check if position is placeable
        if (!canPlaceAt(task.pos)) {
            // Skip this block for now, try later
            buildQueue.poll();
            buildQueue.add(task);
            return;
        }

        // Find block in hotbar
        int slot = findBlockInHotbar(task.state);

        if (slot == -1) {
            // Not in hotbar - try to fetch from chest
            if (!linkedChests.isEmpty()) {
                BlockPos chest = findChestWithBlock(task.state);
                if (chest != null) {
                    startMovingToChest(chest, task.state);
                    return;
                }
            }

            // Can't find block anywhere
            sendMessage("§c⚠ Missing: " + task.state.getBlock().getName().getString() + " - pausing");
            paused = true;
            return;
        }

        // Switch to correct slot
        mc.player.inventory.selected = slot;

        // Place block!
        if (placeBlock(task.pos, task.state)) {
            buildQueue.poll();
            blocksPlaced++;

            // Update layer notification
            if (task.layer > currentLayer) {
                currentLayer = task.layer;
                sendActionBar(
                        "§b📦 Layer " + (currentLayer + 1) + "/" + maxLayer + " | " + getProgress() + "% complete");
            }
        }
    }

    /**
     * Check if we can place at position
     */
    private boolean canPlaceAt(BlockPos pos) {
        BlockState current = mc.level.getBlockState(pos);
        if (!current.isAir() && !current.getMaterial().isReplaceable()) {
            return false;
        }

        // Check if there's a supporting block nearby
        for (Direction dir : Direction.values()) {
            BlockState neighbor = mc.level.getBlockState(pos.relative(dir));
            if (!neighbor.isAir()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find block in hotbar
     */
    private int findBlockInHotbar(BlockState state) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                BlockItem blockItem = (BlockItem) stack.getItem();
                if (blockItem.getBlock() == state.getBlock()) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Place block using client actions
     */
    private boolean placeBlock(BlockPos pos, BlockState targetState) {
        // Find a face to place against
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = mc.level.getBlockState(neighbor);

            if (!neighborState.isAir()) {
                // Create ray trace result
                Vector3d hitVec = new Vector3d(
                        pos.getX() + 0.5 + dir.getStepX() * 0.5,
                        pos.getY() + 0.5 + dir.getStepY() * 0.5,
                        pos.getZ() + 0.5 + dir.getStepZ() * 0.5);

                BlockRayTraceResult rayTrace = new BlockRayTraceResult(
                        hitVec, dir.getOpposite(), neighbor, false);

                // Use item on block (sends packet to server!)
                mc.gameMode.useItemOn(mc.player, mc.level, Hand.MAIN_HAND, rayTrace);

                // Swing arm for visual feedback
                mc.player.swing(Hand.MAIN_HAND);

                return true;
            }
        }
        return false;
    }

    // ========== CHEST OPERATIONS ==========

    public void linkChest(BlockPos pos) {
        if (linkedChests.contains(pos)) {
            sendMessage("§eChest already linked!");
            return;
        }
        linkedChests.add(pos.immutable());
        sendMessage("§a✓ Chest linked at " + pos.toShortString() + " (Total: " + linkedChests.size() + ")");
    }

    public void unlinkChest(BlockPos pos) {
        if (linkedChests.remove(pos)) {
            sendMessage("§e✓ Chest unlinked");
        } else {
            sendMessage("§cChest not in list");
        }
    }

    public void listChests() {
        sendMessage("§6═══ Linked Chests (" + linkedChests.size() + ") ═══");
        if (linkedChests.isEmpty()) {
            sendMessage("§cNo chests linked! Look at chest and use /schem chest link");
        } else {
            for (int i = 0; i < linkedChests.size(); i++) {
                BlockPos pos = linkedChests.get(i);
                double dist = mc.player.blockPosition().distManhattan(pos);
                sendMessage("§e " + (i + 1) + ". " + pos.toShortString() + " §7(" + (int) dist + " blocks)");
            }
        }
    }

    public void clearChests() {
        linkedChests.clear();
        sendMessage("§eAll chests unlinked");
    }

    private BlockPos findChestWithBlock(BlockState needed) {
        // For client-side, we just return closest chest
        // Player will need to manually check contents
        if (linkedChests.isEmpty())
            return null;

        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (BlockPos pos : linkedChests) {
            double dist = mc.player.blockPosition().distSqr(pos);
            if (dist < closestDist) {
                closestDist = dist;
                closest = pos;
            }
        }

        return closest;
    }

    private void startMovingToChest(BlockPos chest, BlockState needed) {
        movingToChest = true;
        targetChest = chest;
        neededBlock = needed;
        returnPos = mc.player.blockPosition();

        sendActionBar("§b🏃 Going to chest for: " + needed.getBlock().getName().getString());
    }

    private void tickMovingToChest() {
        if (targetChest == null) {
            movingToChest = false;
            return;
        }

        ClientPlayerEntity player = mc.player;
        double dist = player.blockPosition().distSqr(targetChest);

        // Close enough to chest
        if (dist < 9) { // 3 blocks
            sendMessage("§a📦 At chest! Get the items and press B to resume");
            movingToChest = false;
            paused = true;
            return;
        }

        // Move towards chest
        moveTowards(targetChest);
    }

    /**
     * Simulate movement towards target
     */
    private void moveTowards(BlockPos target) {
        ClientPlayerEntity player = mc.player;

        // Calculate direction
        double dx = target.getX() + 0.5 - player.getX();
        double dz = target.getZ() + 0.5 - player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 0.1) {
            // Normalize and apply movement
            dx /= dist;
            dz /= dist;

            // Set player input (simulates pressing W)
            player.input.forwardImpulse = 1.0f;
            player.input.leftImpulse = 0.0f;

            // Face target
            float yaw = (float) (Math.atan2(dz, dx) * (180 / Math.PI)) - 90;
            player.yRot = yaw;
            player.yHeadRot = yaw;

            // Jump if stuck
            if (player.horizontalCollision) {
                player.input.jumping = true;
            } else {
                player.input.jumping = false;
            }
        }
    }

    private void complete() {
        running = false;
        sendMessage("§a═══════════════════════════════════");
        sendMessage("§a§l  ✓ BUILD COMPLETE!");
        sendMessage("§e  Placed " + blocksPlaced + " blocks");
        sendMessage("§a═══════════════════════════════════");
    }

    // ========== UTILS ==========

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

    public SchematicData getSchematic() {
        return schematic;
    }

    public void setSpeed(int delay) {
        this.placeDelay = Math.max(1, delay);
    }

    private void sendMessage(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(new StringTextComponent(msg), false);
        }
    }

    private void sendActionBar(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(new StringTextComponent(msg), true);
        }
    }

    /**
     * Build task
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
