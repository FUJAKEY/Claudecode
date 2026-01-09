package com.arcanemagic.spell;

import com.arcanemagic.item.WandItem;
import net.minecraft.block.Blocks;
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
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

/**
 * EPIC Meteor Shower spell - Rains massive destruction from the heavens
 * This is a truly devastating spell worthy of an Archmage!
 */
public class MeteorShowerSpell extends Spell {

    private static final Random RANDOM = new Random();

    @Override
    public int getManaCost() {
        return 100; // Very expensive
    }

    @Override
    public int getCooldown() {
        return 600; // 30 seconds - apocalyptic power needs cooldown
    }

    @Override
    public WandItem.WandTier getMinTier() {
        return WandItem.WandTier.ARCHMAGE;
    }

    @Override
    public String getSpellId() {
        return "meteor_shower";
    }

    @Override
    public int getSpellColor() {
        return 0xFF4500; // Orange-red fire
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        ServerWorld serverWorld = (ServerWorld) world;

        // Find target position (where player is looking, max 50 blocks)
        Vector3d start = player.getEyePosition(1.0f);
        Vector3d look = player.getLookAngle();
        Vector3d end = start.add(look.scale(50.0));

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

        // Epic sound effect - thunderous beginning
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 2.0f, 0.5f);

        // Schedule meteors to rain down over time
        final double tx = targetPos.x;
        final double ty = targetPos.y;
        final double tz = targetPos.z;

        // PHASE 1: Warning particles in the sky (immediate)
        for (int i = 0; i < 100; i++) {
            double ox = tx + RANDOM.nextGaussian() * 15;
            double oz = tz + RANDOM.nextGaussian() * 15;
            serverWorld.sendParticles(ParticleTypes.FLAME,
                    ox, ty + 40, oz, 5, 0.5, 0.5, 0.5, 0.1);
            serverWorld.sendParticles(ParticleTypes.SMOKE,
                    ox, ty + 38, oz, 3, 1, 1, 1, 0.05);
        }

        // PHASE 2: Spawn multiple waves of meteors
        int totalMeteors = 25; // Massive barrage!
        int radius = 12; // Large impact area

        for (int wave = 0; wave < 5; wave++) {
            final int waveNum = wave;

            // Schedule each wave with delay
            serverWorld.getServer().execute(() -> {
                scheduleMeteorWave(serverWorld, tx, ty, tz, totalMeteors / 5, radius, waveNum);
            });
        }

        // Immediate first wave
        spawnMeteorWave(serverWorld, player, tx, ty, tz, 8, radius);

        return true;
    }

    private void scheduleMeteorWave(ServerWorld world, double tx, double ty, double tz,
            int count, int radius, int waveNum) {
        for (int i = 0; i < count; i++) {
            double offsetX = tx + RANDOM.nextGaussian() * radius;
            double offsetZ = tz + RANDOM.nextGaussian() * radius;

            spawnMeteor(world, offsetX, ty, offsetZ, waveNum);
        }
    }

    private void spawnMeteorWave(ServerWorld world, PlayerEntity player,
            double tx, double ty, double tz, int count, int radius) {
        for (int i = 0; i < count; i++) {
            double offsetX = tx + RANDOM.nextGaussian() * radius;
            double offsetZ = tz + RANDOM.nextGaussian() * radius;

            spawnMeteor(world, offsetX, ty, offsetZ, 0);
        }
    }

    private void spawnMeteor(ServerWorld world, double x, double y, double z, int delay) {
        // Calculate ground level
        BlockPos groundPos = new BlockPos(x, y, z);
        while (groundPos.getY() > 0 && world.isEmptyBlock(groundPos)) {
            groundPos = groundPos.below();
        }

        double groundY = groundPos.getY() + 1;

        // Meteor trail from sky to ground
        double startY = groundY + 30 + RANDOM.nextInt(20);

        // Create falling meteor effect with particles
        for (double dy = startY; dy > groundY; dy -= 2) {
            // Main meteor body (large fire trail)
            world.sendParticles(ParticleTypes.FLAME,
                    x, dy, z, 15, 0.8, 0.8, 0.8, 0.2);
            world.sendParticles(ParticleTypes.LAVA,
                    x, dy, z, 5, 0.5, 0.5, 0.5, 0.1);
            world.sendParticles(ParticleTypes.LARGE_SMOKE,
                    x, dy + 2, z, 8, 1.0, 1.0, 1.0, 0.05);
        }

        // IMPACT!
        // Massive explosion
        world.explode(null, x, groundY, z, 4.0f + RANDOM.nextFloat() * 2.0f,
                Explosion.Mode.BREAK);

        // Impact particles - spectacular!
        world.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                x, groundY, z, 1, 0, 0, 0, 0);
        world.sendParticles(ParticleTypes.FLAME,
                x, groundY, z, 100, 3, 2, 3, 0.3);
        world.sendParticles(ParticleTypes.LAVA,
                x, groundY + 1, z, 30, 2, 1, 2, 0.2);
        world.sendParticles(ParticleTypes.SMOKE,
                x, groundY + 2, z, 50, 4, 3, 4, 0.1);
        world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x, groundY, z, 20, 2, 2, 2, 0.05);

        // Set fire around impact
        for (int fx = -2; fx <= 2; fx++) {
            for (int fz = -2; fz <= 2; fz++) {
                if (RANDOM.nextFloat() > 0.3) {
                    BlockPos firePos = new BlockPos(x + fx, groundY, z + fz);
                    if (world.isEmptyBlock(firePos)
                            && world.getBlockState(firePos.below()).isSolidRender(world, firePos.below())) {
                        world.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
                    }
                }
            }
        }

        // Sound effects
        world.playSound(null, x, groundY, z,
                SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 3.0f, 0.7f + RANDOM.nextFloat() * 0.3f);

        // Damage all entities in impact radius
        AxisAlignedBB damageArea = new AxisAlignedBB(
                x - 6, groundY - 2, z - 6,
                x + 6, groundY + 6, z + 6);

        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, damageArea);
        for (LivingEntity entity : entities) {
            float damage = 20.0f + RANDOM.nextFloat() * 15.0f; // 20-35 damage!
            entity.hurt(DamageSource.IN_FIRE, damage);
            entity.setSecondsOnFire(8); // Burn for 8 seconds

            // Knockback
            double dx = entity.getX() - x;
            double dz = entity.getZ() - z;
            double dist = Math.sqrt(dx * dx + dz * dz) + 0.1;
            entity.push(dx / dist * 2.0, 0.8, dz / dist * 2.0);
        }
    }
}
