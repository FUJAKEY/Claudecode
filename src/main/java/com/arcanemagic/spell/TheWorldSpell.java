package com.arcanemagic.spell;

import com.arcanemagic.ArcaneMagicMod;
import com.arcanemagic.init.ModSounds;
import com.arcanemagic.item.WandItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

/**
 * THE WORLD - Time Stop Spell
 * "ZA WARUDO! TOKI WO TOMARE!"
 * 
 * Freezes all entities in a 30 block radius for 10 seconds.
 * Creates a visible honeycomb-pattern barrier sphere.
 * Blocks entity passage through the barrier.
 * Only the caster can move and use spells.
 */
public class TheWorldSpell extends Spell {

    // Active time stops: casterUUID -> TimeStopData
    private static final Map<UUID, TimeStopData> ACTIVE_TIME_STOPS = new HashMap<>();

    // Frozen entities: entityUUID -> FrozenEntityData
    private static final Map<UUID, FrozenEntityData> FROZEN_ENTITIES = new HashMap<>();

    public static final double RADIUS = 30.0;
    public static final int DURATION_TICKS = 200; // 10 seconds

    @Override
    public int getManaCost() {
        return 150;
    }

    @Override
    public int getCooldown() {
        return 1200; // 60 seconds
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

        // Play ZA WARUDO sound effect
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.THE_WORLD.get(), SoundCategory.PLAYERS, 2.0f, 1.0f);

        // Create time stop data
        long endTime = world.getGameTime() + DURATION_TICKS;
        TimeStopData data = new TimeStopData(center, endTime, player.getUUID());
        ACTIVE_TIME_STOPS.put(player.getUUID(), data);

        // Initial flash effect
        createBarrierParticles(serverWorld, center, BARRIER_RADIUS, true);

        // Find and freeze all entities in radius (except caster)
        // Use FREEZE_RADIUS for initial freeze to be safe
        AxisAlignedBB area = new AxisAlignedBB(
                center.x - FREEZE_RADIUS, center.y - FREEZE_RADIUS, center.z - FREEZE_RADIUS,
                center.x + FREEZE_RADIUS, center.y + FREEZE_RADIUS, center.z + FREEZE_RADIUS);

        List<Entity> entities = world.getEntities(player, area,
                e -> e.distanceToSqr(center) <= FREEZE_RADIUS * FREEZE_RADIUS && e instanceof LivingEntity);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && entity != player) {
                freezeEntity((LivingEntity) entity, endTime, player.getUUID());
            }
        }

        ArcaneMagicMod.LOGGER.info("THE WORLD! Froze " + entities.size() + " entities for 10 seconds!");
        return true;
    }

    /**
     * Create barrier particles in honeycomb pattern
     */
    private static void createBarrierParticles(ServerWorld world, Vector3d center, double radius, boolean isInitial) {
        int particleCount = isInitial ? 800 : 200;

        // Honeycomb pattern using fibonacci sphere distribution
        double goldenRatio = (1 + Math.sqrt(5)) / 2;

        for (int i = 0; i < particleCount; i++) {
            double theta = 2 * Math.PI * i / goldenRatio;
            double phi = Math.acos(1 - 2 * (i + 0.5) / particleCount);

            double x = center.x + radius * Math.sin(phi) * Math.cos(theta);
            double y = center.y + radius * Math.cos(phi);
            double z = center.z + radius * Math.sin(phi) * Math.sin(theta);

            // Golden/yellow barrier particles
            if (i % 3 == 0) {
                world.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0);
            }
            if (i % 5 == 0) {
                world.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0, 0, 0);
            }
            // Honeycomb effect - hexagonal pattern hints
            if (i % 7 == 0) {
                world.sendParticles(ParticleTypes.ENCHANT, x, y, z, 2, 0.1, 0.1, 0.1, 0.02);
            }
        }

        // Ring at ground level
        for (int i = 0; i < 60; i++) {
            double angle = (i / 60.0) * Math.PI * 2;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            world.sendParticles(ParticleTypes.FLAME, x, center.y, z, 2, 0.05, 0.5, 0.05, 0);
        }
    }

    /**
     * Called every world tick to maintain time stop effects
     */
    // Updated constants
    public static final double BARRIER_RADIUS = 30.0;
    public static final double FREEZE_RADIUS = 28.5; // Slightly smaller to prevent glitching at edge

    // ... (rest of class structure remains, logic changes below)

    /**
     * Called every world tick to maintain time stop effects
     */
    public static void tickTimeStop(World world) {
        if (world.isClientSide)
            return;

        ServerWorld serverWorld = (ServerWorld) world;
        long currentTime = world.getGameTime();

        // Process active time stops
        List<UUID> expiredStops = new ArrayList<>();

        for (Map.Entry<UUID, TimeStopData> entry : ACTIVE_TIME_STOPS.entrySet()) {
            TimeStopData data = entry.getValue();

            if (currentTime >= data.endTime) {
                expiredStops.add(entry.getKey());
                continue;
            }

            // Render barrier particles every 5 ticks
            if (currentTime % 5 == 0) {
                createBarrierParticles(serverWorld, data.center, BARRIER_RADIUS, false);
            }

            // Manage Area Effect (Freeze + Barrier)
            // Scan area slightly larger than barrier
            AxisAlignedBB area = new AxisAlignedBB(
                    data.center.x - BARRIER_RADIUS - 3, data.center.y - BARRIER_RADIUS - 3,
                    data.center.z - BARRIER_RADIUS - 3,
                    data.center.x + BARRIER_RADIUS + 3, data.center.y + BARRIER_RADIUS + 3,
                    data.center.z + BARRIER_RADIUS + 3);

            List<Entity> nearbyEntities = serverWorld.getEntities((Entity) null, area,
                    e -> e instanceof LivingEntity && !e.getUUID().equals(data.casterUUID));

            for (Entity entity : nearbyEntities) {
                double dist = entity.position().distanceTo(data.center);
                LivingEntity living = (LivingEntity) entity;

                if (dist <= FREEZE_RADIUS) {
                    // INNER ZONE: FREEZE
                    // Start freezing if not already frozen
                    if (!FROZEN_ENTITIES.containsKey(living.getUUID())) {
                        freezeEntity(living, data.endTime, data.casterUUID);
                    }
                } else if (dist < BARRIER_RADIUS + 2.0) {
                    // BARRIER ZONE (28.5 to ~32.0): PUSH

                    // If they were frozen but are now in barrier zone (e.g. pushed), unfreeze so
                    // physics works
                    if (FROZEN_ENTITIES.containsKey(living.getUUID())) {
                        unfreezeEntity(serverWorld, living.getUUID());
                    }

                    // Calculate push direction
                    Vector3d toCenter = data.center.subtract(entity.position()).normalize();
                    boolean isInside = dist < BARRIER_RADIUS;

                    // Logic: If inside barrier radius, push IN (to freeze). If outside, push OUT.
                    Vector3d pushDir = isInside ? toCenter : toCenter.scale(-1);

                    // Stronger push
                    double pushStrength = 0.6;
                    entity.setDeltaMovement(pushDir.scale(pushStrength));
                    entity.hurtMarked = true;

                    // Particles
                    if (currentTime % 5 == 0) {
                        serverWorld.sendParticles(ParticleTypes.ENCHANT,
                                entity.getX(), entity.getY() + 1, entity.getZ(),
                                5, 0.2, 0.2, 0.2, 0.05);
                    }
                }
            }
        }

        // Clean up expired time stops
        for (UUID casterId : expiredStops) {
            ACTIVE_TIME_STOPS.remove(casterId);
        }

        // Process frozen entities
        List<UUID> toUnfreeze = new ArrayList<>();

        for (Map.Entry<UUID, FrozenEntityData> entry : FROZEN_ENTITIES.entrySet()) {
            FrozenEntityData data = entry.getValue();

            if (currentTime >= data.endTime) {
                toUnfreeze.add(entry.getKey());
                continue;
            }

            // Find and keep entity frozen
            Entity entity = serverWorld.getEntity(entry.getKey());
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;

                // Force lock position
                living.setPos(data.frozenPos.x, data.frozenPos.y, data.frozenPos.z);
                living.setDeltaMovement(0, 0, 0);
                living.yRot = data.frozenYaw;
                living.xRot = data.frozenPitch;
                living.yRotO = data.frozenYaw;
                living.xRotO = data.frozenPitch;
                living.yBodyRot = data.frozenYaw;
                living.yHeadRot = data.frozenYaw;

                // Prevent jumping/sneaking
                living.setJumping(false);
                living.setShiftKeyDown(false);

                // Frozen particle effect
                if (currentTime % 10 == 0) {
                    serverWorld.sendParticles(ParticleTypes.END_ROD,
                            living.getX(), living.getY() + 1, living.getZ(),
                            1, 0.2, 0.5, 0.2, 0.01);
                }
            } else {
                // Entity maybe despawned or dead
                toUnfreeze.add(entry.getKey());
            }
        }

        // Unfreeze expired entities
        for (UUID id : toUnfreeze) {
            unfreezeEntity(serverWorld, id);
        }
    }

    private static void freezeEntity(LivingEntity living, long endTime, UUID casterId) {
        FrozenEntityData frozenData = new FrozenEntityData(
                living.position(),
                living.getDeltaMovement(),
                living.yRot,
                living.xRot,
                endTime,
                casterId);
        FROZEN_ENTITIES.put(living.getUUID(), frozenData);

        // Stop movement immediately
        living.setDeltaMovement(0, 0, 0);
        living.setNoGravity(true);
    }

    private static void unfreezeEntity(ServerWorld world, UUID entityId) {
        if (!FROZEN_ENTITIES.containsKey(entityId))
            return;

        FrozenEntityData data = FROZEN_ENTITIES.get(entityId);
        FROZEN_ENTITIES.remove(entityId);

        Entity entity = world.getEntity(entityId);
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            living.setNoGravity(false);
            living.setDeltaMovement(data.frozenMotion);

            // Unfreeze flash
            world.sendParticles(ParticleTypes.FLASH,
                    living.getX(), living.getY() + 1, living.getZ(),
                    3, 0.5, 0.5, 0.5, 0);
        }
    }

    /**
     * Check if an entity is frozen by The World
     */
    public static boolean isEntityFrozen(UUID entityUUID) {
        return FROZEN_ENTITIES.containsKey(entityUUID);
    }

    /**
     * Check if an entity can cast spells (not frozen, or is the time stop caster)
     */
    public static boolean canEntityCastSpells(UUID entityUUID) {
        // If not frozen, can cast
        if (!FROZEN_ENTITIES.containsKey(entityUUID)) {
            // But check if they're inside someone else's time stop zone
            // For now, allowing if not explicitly frozen
            return true;
        }
        return false;
    }

    /**
     * Check if this entity is the caster of an active time stop
     */
    public static boolean isTimeStopCaster(UUID entityUUID) {
        return ACTIVE_TIME_STOPS.containsKey(entityUUID);
    }

    /**
     * Data class for active time stops
     */
    private static class TimeStopData {
        final Vector3d center;
        final long endTime;
        final UUID casterUUID;

        TimeStopData(Vector3d center, long endTime, UUID casterUUID) {
            this.center = center;
            this.endTime = endTime;
            this.casterUUID = casterUUID;
        }
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
        final UUID casterUUID;

        FrozenEntityData(Vector3d pos, Vector3d motion, float yaw, float pitch, long endTime, UUID casterUUID) {
            this.frozenPos = pos;
            this.frozenMotion = motion;
            this.frozenYaw = yaw;
            this.frozenPitch = pitch;
            this.endTime = endTime;
            this.casterUUID = casterUUID;
        }
    }
}
