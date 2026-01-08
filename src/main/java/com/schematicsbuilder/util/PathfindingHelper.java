package com.schematicsbuilder.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

/**
 * Simple pathfinding helper for player movement to chests
 */
public class PathfindingHelper {

    /**
     * Move player towards target position
     * Returns true when player is close enough to target
     */
    public static boolean moveTowards(ServerPlayerEntity player, BlockPos target, double speed) {
        Vector3d playerPos = player.position();
        Vector3d targetVec = new Vector3d(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);

        double dist = playerPos.distanceTo(targetVec);

        // Close enough
        if (dist < 2.0) {
            return true;
        }

        // Calculate direction
        Vector3d direction = targetVec.subtract(playerPos).normalize();

        // Set movement
        double moveSpeed = Math.min(speed, dist);

        // Simple movement - player walks towards target
        player.setDeltaMovement(
                direction.x * moveSpeed * 0.3,
                player.getDeltaMovement().y, // Keep vertical momentum
                direction.z * moveSpeed * 0.3);

        // Face target
        double dx = target.getX() + 0.5 - player.getX();
        double dz = target.getZ() + 0.5 - player.getZ();
        float yaw = (float) (Math.atan2(dz, dx) * (180 / Math.PI)) - 90;
        player.yRot = yaw;
        player.yHeadRot = yaw;

        // Jump if blocked
        if (player.horizontalCollision && player.isOnGround()) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.4, 0));
        }

        player.hurtMarked = true;

        return false;
    }

    /**
     * Calculate simple path distance (Manhattan)
     */
    public static int getPathDistance(BlockPos from, BlockPos to) {
        return Math.abs(from.getX() - to.getX()) +
                Math.abs(from.getY() - to.getY()) +
                Math.abs(from.getZ() - to.getZ());
    }

    /**
     * Check if path is clear (simple check)
     */
    public static boolean isPathClear(ServerWorld world, BlockPos from, BlockPos to) {
        // Simple line check - can improve with A* later
        int dx = Integer.signum(to.getX() - from.getX());
        int dz = Integer.signum(to.getZ() - from.getZ());

        BlockPos current = from;
        int steps = 0;
        int maxSteps = getPathDistance(from, to) + 10;

        while (!current.equals(to) && steps < maxSteps) {
            if (current.getX() != to.getX()) {
                current = current.offset(dx, 0, 0);
            }
            if (current.getZ() != to.getZ()) {
                current = current.offset(0, 0, dz);
            }

            // Check if blocked
            if (!world.getBlockState(current).isAir() &&
                    !world.getBlockState(current).getMaterial().isReplaceable()) {
                // Try going up
                if (world.getBlockState(current.above()).isAir()) {
                    current = current.above();
                } else {
                    return false;
                }
            }

            steps++;
        }

        return true;
    }

    /**
     * Find closest open spot near target
     */
    public static BlockPos findOpenSpotNear(ServerWorld world, BlockPos target) {
        // Check spots around target
        for (int range = 1; range <= 3; range++) {
            for (int dx = -range; dx <= range; dx++) {
                for (int dz = -range; dz <= range; dz++) {
                    if (Math.abs(dx) == range || Math.abs(dz) == range) {
                        BlockPos check = target.offset(dx, 0, dz);
                        if (world.getBlockState(check).isAir() &&
                                world.getBlockState(check.above()).isAir() &&
                                !world.getBlockState(check.below()).isAir()) {
                            return check;
                        }
                    }
                }
            }
        }
        return target;
    }
}
