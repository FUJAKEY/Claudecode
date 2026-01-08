package com.schematicsbuilder.schematic;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

/**
 * Manages linked resource chests for auto-building
 * Player can link multiple chests as resource sources
 */
public class ResourceChestManager {

    // Player UUID -> List of chest positions
    private static final Map<UUID, List<BlockPos>> linkedChests = new HashMap<>();

    // Player UUID -> Current fetch state
    private static final Map<UUID, FetchState> fetchStates = new HashMap<>();

    /**
     * Link a chest at player's looked-at position
     */
    public static boolean linkChest(ServerPlayerEntity player, BlockPos pos) {
        ServerWorld world = (ServerWorld) player.level;
        BlockState state = world.getBlockState(pos);

        // Check if it's a chest
        if (!(state.getBlock() instanceof ChestBlock) && state.getBlock() != Blocks.BARREL) {
            player.displayClientMessage(
                    new StringTextComponent("Not a valid container! Use on a chest or barrel.")
                            .withStyle(TextFormatting.RED),
                    false);
            return false;
        }

        UUID playerId = player.getUUID();
        List<BlockPos> chests = linkedChests.computeIfAbsent(playerId, k -> new ArrayList<>());

        // Check if already linked
        if (chests.contains(pos)) {
            player.displayClientMessage(
                    new StringTextComponent("This chest is already linked!")
                            .withStyle(TextFormatting.YELLOW),
                    false);
            return false;
        }

        chests.add(pos.immutable());

        player.displayClientMessage(
                new StringTextComponent("═══════════════════════════════════")
                        .withStyle(TextFormatting.GREEN),
                false);
        player.displayClientMessage(
                new StringTextComponent("  ✓ Chest Linked!")
                        .withStyle(TextFormatting.GREEN).withStyle(TextFormatting.BOLD),
                false);
        player.displayClientMessage(
                new StringTextComponent("  Position: " + pos.toShortString())
                        .withStyle(TextFormatting.GRAY),
                false);
        player.displayClientMessage(
                new StringTextComponent("  Total linked chests: " + chests.size())
                        .withStyle(TextFormatting.GRAY),
                false);
        player.displayClientMessage(
                new StringTextComponent("═══════════════════════════════════")
                        .withStyle(TextFormatting.GREEN),
                false);

        return true;
    }

    /**
     * Unlink a chest
     */
    public static boolean unlinkChest(ServerPlayerEntity player, BlockPos pos) {
        UUID playerId = player.getUUID();
        List<BlockPos> chests = linkedChests.get(playerId);

        if (chests == null || !chests.remove(pos)) {
            player.displayClientMessage(
                    new StringTextComponent("Chest not found in linked list!")
                            .withStyle(TextFormatting.RED),
                    false);
            return false;
        }

        player.displayClientMessage(
                new StringTextComponent("✓ Chest unlinked: " + pos.toShortString())
                        .withStyle(TextFormatting.YELLOW),
                false);
        return true;
    }

    /**
     * Clear all linked chests for player
     */
    public static void clearAllChests(ServerPlayerEntity player) {
        linkedChests.remove(player.getUUID());
        player.displayClientMessage(
                new StringTextComponent("All chests unlinked!")
                        .withStyle(TextFormatting.YELLOW),
                false);
    }

    /**
     * List all linked chests
     */
    public static void listChests(ServerPlayerEntity player) {
        List<BlockPos> chests = linkedChests.get(player.getUUID());

        player.displayClientMessage(
                new StringTextComponent("═══ Linked Resource Chests ═══")
                        .withStyle(TextFormatting.GOLD),
                false);

        if (chests == null || chests.isEmpty()) {
            player.displayClientMessage(
                    new StringTextComponent("No chests linked!")
                            .withStyle(TextFormatting.RED),
                    false);
            player.displayClientMessage(
                    new StringTextComponent("Use /schem chest link while looking at a chest")
                            .withStyle(TextFormatting.GRAY),
                    false);
        } else {
            for (int i = 0; i < chests.size(); i++) {
                BlockPos pos = chests.get(i);
                double dist = player.blockPosition().distManhattan(pos);
                player.displayClientMessage(
                        new StringTextComponent(" " + (i + 1) + ". " + pos.toShortString() +
                                " (" + (int) dist + " blocks away)")
                                .withStyle(TextFormatting.YELLOW),
                        false);
            }
        }
    }

    /**
     * Get linked chests for player
     */
    public static List<BlockPos> getLinkedChests(UUID playerId) {
        return linkedChests.getOrDefault(playerId, Collections.emptyList());
    }

    /**
     * Check if we need to fetch resources
     */
    public static boolean needsFetch(ServerPlayerEntity player, BlockState neededBlock) {
        // Check if creative
        if (player.isCreative())
            return false;

        // Check inventory for the block
        return !hasBlockInInventory(player, neededBlock);
    }

    /**
     * Check if player has block in inventory
     */
    public static boolean hasBlockInInventory(ServerPlayerEntity player, BlockState state) {
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

    /**
     * Get count of blocks in player inventory
     */
    public static int getBlockCount(ServerPlayerEntity player, BlockState state) {
        if (player.isCreative())
            return 9999;

        int count = 0;
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == state.getBlock().asItem()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Find chest containing the needed block
     */
    public static BlockPos findChestWithBlock(ServerPlayerEntity player, BlockState neededBlock) {
        ServerWorld world = (ServerWorld) player.level;
        List<BlockPos> chests = linkedChests.get(player.getUUID());

        if (chests == null || chests.isEmpty())
            return null;

        // Find closest chest with the needed block
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (BlockPos pos : chests) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof IInventory) {
                IInventory inv = (IInventory) te;

                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (!stack.isEmpty() && stack.getItem() == neededBlock.getBlock().asItem()) {
                        double dist = player.blockPosition().distSqr(pos);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closest = pos;
                        }
                        break;
                    }
                }
            }
        }

        return closest;
    }

    /**
     * Take blocks from chest and put in player inventory
     */
    public static int takeBlocksFromChest(ServerPlayerEntity player, BlockPos chestPos,
            BlockState neededBlock, int maxAmount) {
        ServerWorld world = (ServerWorld) player.level;
        TileEntity te = world.getBlockEntity(chestPos);

        if (!(te instanceof IInventory))
            return 0;

        IInventory chest = (IInventory) te;
        int taken = 0;

        for (int i = 0; i < chest.getContainerSize() && taken < maxAmount; i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == neededBlock.getBlock().asItem()) {
                int toTake = Math.min(stack.getCount(), maxAmount - taken);

                // Create copy for player
                ItemStack toGive = stack.copy();
                toGive.setCount(toTake);

                // Add to player inventory
                if (player.inventory.add(toGive)) {
                    stack.shrink(toTake);
                    taken += toTake;
                } else {
                    // Inventory full
                    break;
                }
            }
        }

        if (taken > 0) {
            chest.setChanged();
        }

        return taken;
    }

    /**
     * Get or create fetch state for player
     */
    public static FetchState getFetchState(UUID playerId) {
        return fetchStates.get(playerId);
    }

    public static void setFetchState(UUID playerId, FetchState state) {
        if (state == null) {
            fetchStates.remove(playerId);
        } else {
            fetchStates.put(playerId, state);
        }
    }

    /**
     * Clean up on player disconnect
     */
    public static void onPlayerDisconnect(UUID playerId) {
        linkedChests.remove(playerId);
        fetchStates.remove(playerId);
    }

    /**
     * Fetch state - tracks when player is going to chest for resources
     */
    public static class FetchState {
        public final BlockPos targetChest;
        public final BlockState neededBlock;
        public final BlockPos returnPos;
        public final long startTime;
        public boolean atChest = false;
        public int ticksAtChest = 0;

        public FetchState(BlockPos targetChest, BlockState neededBlock, BlockPos returnPos) {
            this.targetChest = targetChest;
            this.neededBlock = neededBlock;
            this.returnPos = returnPos;
            this.startTime = System.currentTimeMillis();
        }
    }
}
