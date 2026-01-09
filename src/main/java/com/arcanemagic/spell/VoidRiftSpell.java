package com.arcanemagic.spell;

import com.arcanemagic.item.WandItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

/**
 * VOID RIFT - Opens a dimensional rift that sucks in enemies and tears them
 * apart
 * Creates a swirling vortex of destruction with End particles and portal
 * effects
 */
public class VoidRiftSpell extends Spell {

    private static final Random RANDOM = new Random();

    @Override
    public int getManaCost() {
        return 80;
    }

    @Override
    public int getCooldown() {
        return 400; // 20 seconds
    }

    @Override
    public WandItem.WandTier getMinTier() {
        return WandItem.WandTier.MASTER;
    }

    @Override
    public String getSpellId() {
        return "void_rift";
    }

    @Override
    public int getSpellColor() {
        return 0x4B0082; // Deep purple/indigo
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        ServerWorld serverWorld = (ServerWorld) world;

        // Find target position
        Vector3d start = player.getEyePosition(1.0f);
        Vector3d look = player.getLookAngle();
        Vector3d end = start.add(look.scale(25.0));

        BlockRayTraceResult result = world.clip(new RayTraceContext(
                start, end,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                player));

        Vector3d targetPos;
        if (result.getType() != RayTraceResult.Type.MISS) {
            targetPos = result.getLocation();
        } else {
            targetPos = end;
        }

        double tx = targetPos.x;
        double ty = targetPos.y + 1;
        double tz = targetPos.z;

        // Ominous sound
        world.playSound(null, tx, ty, tz,
                SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 2.0f, 0.3f);
        world.playSound(null, tx, ty, tz,
                SoundEvents.AMBIENT_CAVE, SoundCategory.PLAYERS, 2.0f, 0.5f);

        // Create swirling vortex effect
        int riftDuration = 60; // 3 seconds of terror
        double riftRadius = 8.0;

        for (int tick = 0; tick < riftDuration; tick += 2) {
            final int t = tick;

            // Spiral portal effect - multiple layers
            for (int layer = 0; layer < 3; layer++) {
                double angle = (t * 0.3) + (layer * 2.094); // 120 degrees apart
                double radius = riftRadius * (1.0 - (t / (double) riftDuration) * 0.3);

                for (int i = 0; i < 12; i++) {
                    double a = angle + (i * 0.524); // 30 degrees
                    double px = tx + Math.cos(a) * radius;
                    double pz = tz + Math.sin(a) * radius;
                    double py = ty + Math.sin(t * 0.2 + i) * 2;

                    // End rod particles for ethereal effect
                    serverWorld.sendParticles(ParticleTypes.END_ROD,
                            px, py, pz, 1, 0.1, 0.1, 0.1, 0.02);

                    // Portal particles
                    serverWorld.sendParticles(ParticleTypes.PORTAL,
                            px, py, pz, 3, 0.3, 0.5, 0.3, 0.5);
                }
            }

            // Center of the rift - darkness and void
            serverWorld.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    tx, ty, tz, 20, 1.5, 1.5, 1.5, 0.5);
            serverWorld.sendParticles(ParticleTypes.DRAGON_BREATH,
                    tx, ty + 0.5, tz, 10, 0.8, 0.8, 0.8, 0.1);

            // Outer ring particles
            for (int i = 0; i < 20; i++) {
                double angle = RANDOM.nextDouble() * Math.PI * 2;
                double r = riftRadius + RANDOM.nextDouble() * 2;
                serverWorld.sendParticles(ParticleTypes.WITCH,
                        tx + Math.cos(angle) * r,
                        ty + RANDOM.nextDouble() * 3,
                        tz + Math.sin(angle) * r,
                        1, 0, 0, 0, 0);
            }
        }

        // DAMAGE PHASE - Pull in and damage enemies
        AxisAlignedBB riftArea = new AxisAlignedBB(
                tx - riftRadius, ty - 3, tz - riftRadius,
                tx + riftRadius, ty + 5, tz + riftRadius);

        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, riftArea,
                e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            // Calculate direction towards rift center
            double dx = tx - entity.getX();
            double dy = ty - entity.getY();
            double dz = tz - entity.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz) + 0.1;

            // Pull towards center (stronger when closer)
            double pullStrength = 1.5 * (1.0 - dist / riftRadius);
            entity.setDeltaMovement(
                    entity.getDeltaMovement().add(dx / dist * pullStrength, 0.3, dz / dist * pullStrength));

            // Void damage - bypasses armor!
            float damage = 15.0f + (float) (10.0f * (1.0 - dist / riftRadius));
            entity.hurt(DamageSource.OUT_OF_WORLD, damage);

            // Particles on damaged entity
            serverWorld.sendParticles(ParticleTypes.PORTAL,
                    entity.getX(), entity.getY() + 1, entity.getZ(),
                    15, 0.5, 0.5, 0.5, 0.5);
            serverWorld.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    entity.getX(), entity.getY() + 0.5, entity.getZ(),
                    8, 0.3, 0.3, 0.3, 0.3);
        }

        // Final implosion effect
        world.playSound(null, tx, ty, tz,
                SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.5f, 1.5f);
        serverWorld.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                tx, ty, tz, 1, 0, 0, 0, 0);

        // Lingering void particles
        for (int i = 0; i < 50; i++) {
            serverWorld.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    tx + RANDOM.nextGaussian() * 3,
                    ty + RANDOM.nextDouble() * 4,
                    tz + RANDOM.nextGaussian() * 3,
                    1, 0, 0, 0, 0.1);
        }

        return true;
    }
}
