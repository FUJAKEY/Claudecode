package com.arcanemagic.spell;

import com.arcanemagic.ArcaneMagicMod;
import com.arcanemagic.item.WandItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * THE WORLD - Time Stop Spell
 * Freezes all entities in a 30 block radius for 10 seconds
 * Creates a translucent barrier sphere at the boundary
 * "ZA WARUDO! TOKI WO TOMARE!"
 */
public class TheWorldSpell extends Spell {

    // Track frozen entities and their original positions/motions
    private static final Map<UUID, FrozenEntityData> FROZEN_ENTITIES = new HashMap<>();
    private static final Map<UUID, Long> ACTIVE_TIME_STOPS = new HashMap<>();
    private static final double RADIUS = 30.0;
    private static final int DURATION_TICKS = 200; // 10 seconds

    @Override
    public int getManaCost() {
        return 150; // Very expensive
    }

    @Override
    public int getCooldown() {
        return 1200; // 60 seconds - god-tier ability
    }

    @Override
    public WandItem.WandTier getMinTier() {
        return WandItem.WandTier.DIVINE;
    }

    @Override
    public String getSpellId() {
        return "the_world";
    }

    @Override
    public int getSpellColor() {
        return 0xFFD700; // Gold
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        ServerWorld serverWorld = (ServerWorld) world;
        Vector3d center = player.position();

        // Play THE WORLD sound effect!
        SoundEvent theWorldSound = ForgeRegistries.SOUND_EVENTS.getValue(
                new ResourceLocation(ArcaneMagicMod.MOD_ID, "the_world"));
        if (theWorldSound != null) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    theWorldSound, SoundCategory.PLAYERS, 3.0f, 1.0f);
        }

        // Initial flash effect
        for (int i = 0; i < 200; i++) {
            double angle = Math.random() * Math.PI * 2;
            double angle2 = Math.random() * Math.PI;
            double r = RADIUS;
            double x = center.x + Math.sin(angle2) * Math.cos(angle) * r;
            double y = center.y + Math.cos(angle2) * r;
            double z = center.z + Math.sin(angle2) * Math.sin(angle) * r;

            serverWorld.sendParticles(ParticleTypes.END_ROD,
                    x, y, z, 1, 0, 0, 0, 0);
        }

        // Create golden barrier sphere effect
        createBarrierSphere(serverWorld, center, RADIUS);

        // Find all entities in radius (except caster)
        AxisAlignedBB area = new AxisAlignedBB(
                center.x - RADIUS, center.y - RADIUS, center.z - RADIUS,
                center.x + RADIUS, center.y + RADIUS, center.z + RADIUS);

        List<Entity> entities = world.getEntities(player, area,
                e -> e.distanceToSqr(center) <= RADIUS * RADIUS && e instanceof LivingEntity);

        // Freeze all entities
        long endTime = world.getGameTime() + DURATION_TICKS;
        ACTIVE_TIME_STOPS.put(player.getUUID(), endTime);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity living = (LivingEntity) entity;

                // Store original data
                FrozenEntityData data = new FrozenEntityData(
                        living.position(),
                        living.getDeltaMovement(),
                        living.yRot,
                        living.xRot,
                        endTime);
                FROZEN_ENTITIES.put(living.getUUID(), data);

                // Stop movement
                living.setDeltaMovement(0, 0, 0);
                living.setNoGravity(true);

                // Visual effect on frozen entity
                serverWorld.sendParticles(ParticleTypes.END_ROD,
                        living.getX(), living.getY() + 1, living.getZ(),
                        20, 0.5, 0.5, 0.5, 0.02);
            }
        }

        // Schedule time resume (handled in tick event)
        ArcaneMagicMod.LOGGER.info("THE WORLD activated! Froze " + entities.size() + " entities for 10 seconds!");

        return true;
    }

    private void createBarrierSphere(ServerWorld world, Vector3d center, double radius) {
        // Create visible barrier at the edge of time stop zone
        int particleCount = 500;

        for (int i = 0; i < particleCount; i++) {
            // Spherical coordinates
            double phi = Math.acos(1 - 2 * (i / (double) particleCount));
            double theta = Math.PI * (1 + Math.sqrt(5)) * i;

            double x = center.x + radius * Math.sin(phi) * Math.cos(theta);
            double y = center.y + radius * Math.cos(phi);
            double z = center.z + radius * Math.sin(phi) * Math.sin(theta);

            // Golden barrier particles
            world.sendParticles(ParticleTypes.END_ROD,
                    x, y, z, 1, 0, 0, 0, 0);

            // Additional purple tint
            if (i % 3 == 0) {
                world.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        x, y, z, 1, 0, 0, 0, 0);
            }
        }

        // Ring at ground level for visibility
        for (int i = 0; i < 100; i++) {
            double angle = (i / 100.0) * Math.PI * 2;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            world.sendParticles(ParticleTypes.FLAME,
                    x, center.y, z, 3, 0.1, 0.1, 0.1, 0);
        }
    }

    /**
     * Called every tick to maintain time stop and check for expiration
     */
    public static void tickTimeStop(World world) {
        if (world.isClientSide)
            return;

        long currentTime = world.getGameTime();
        List<UUID> toRemove = new ArrayList<>();

        // Update frozen entities
        for (Map.Entry<UUID, FrozenEntityData> entry : FROZEN_ENTITIES.entrySet()) {
            FrozenEntityData data = entry.getValue();

            if (currentTime >= data.endTime) {
                toRemove.add(entry.getKey());
                continue;
            }

            // Find entity and keep it frozen
            Entity entity = ((ServerWorld) world).getEntity(entry.getKey());
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;

                // Keep at frozen position
                living.setPos(data.frozenPos.x, data.frozenPos.y, data.frozenPos.z);
                living.setDeltaMovement(0, 0, 0);
                living.yRot = data.frozenYaw;
                living.xRot = data.frozenPitch;

                // Frozen particle effect
                if (currentTime % 10 == 0) {
                    ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD,
                            living.getX(), living.getY() + 1, living.getZ(),
                            3, 0.3, 0.5, 0.3, 0.01);
                }
            }
        }

        // Unfreeze expired entities
        for (UUID id : toRemove) {
            Entity entity = ((ServerWorld) world).getEntity(id);
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                FrozenEntityData data = FROZEN_ENTITIES.get(id);

                // Restore movement
                living.setNoGravity(false);
                living.setDeltaMovement(data.frozenMotion);

                // Unfreeze particles
                ((ServerWorld) world).sendParticles(ParticleTypes.FLASH,
                        living.getX(), living.getY() + 1, living.getZ(),
                        5, 0.5, 0.5, 0.5, 0);
            }
            FROZEN_ENTITIES.remove(id);
        }

        // Clean up expired time stops
        ACTIVE_TIME_STOPS.entrySet().removeIf(e -> currentTime >= e.getValue());
    }

    /**
     * Check if a position is inside an active time stop zone (for barrier
     * collision)
     */
    public static boolean isInsideTimeStop(Vector3d pos, UUID casterId) {
        // For now, entities are frozen by position tracking
        // Barrier collision would need additional implementation
        return ACTIVE_TIME_STOPS.containsKey(casterId);
    }

    /**
     * Data class to store frozen entity state
     */
    private static class FrozenEntityData {
        final Vector3d frozenPos;
        final Vector3d frozenMotion;
        final float frozenYaw;
        final float frozenPitch;
        final long endTime;

        FrozenEntityData(Vector3d pos, Vector3d motion, float yaw, float pitch, long endTime) {
            this.frozenPos = pos;
            this.frozenMotion = motion;
            this.frozenYaw = yaw;
            this.frozenPitch = pitch;
            this.endTime = endTime;
        }
    }
}
