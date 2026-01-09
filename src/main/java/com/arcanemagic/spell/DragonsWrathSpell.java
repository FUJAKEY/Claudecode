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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

/**
 * DRAGON'S WRATH - Summons a spectral dragon that breathes devastating fire
 * Creates a massive dragon-shaped flame breath attack with lingering fire
 */
public class DragonsWrathSpell extends Spell {

    private static final Random RANDOM = new Random();

    @Override
    public int getManaCost() {
        return 90;
    }

    @Override
    public int getCooldown() {
        return 500; // 25 seconds
    }

    @Override
    public WandItem.WandTier getMinTier() {
        return WandItem.WandTier.ARCHMAGE;
    }

    @Override
    public String getSpellId() {
        return "dragons_wrath";
    }

    @Override
    public int getSpellColor() {
        return 0xFF6600; // Dragon orange
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        ServerWorld serverWorld = (ServerWorld) world;

        // Direction player is looking
        Vector3d look = player.getLookAngle();
        Vector3d start = player.getEyePosition(1.0f);

        // Epic dragon roar!
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 3.0f, 0.8f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundCategory.PLAYERS, 2.0f, 0.6f);

        // PHASE 1: Dragon head manifestation above player
        double headX = player.getX();
        double headY = player.getY() + 5;
        double headZ = player.getZ();

        // Dragon head particles - swirling fire
        for (int i = 0; i < 30; i++) {
            double angle = i * 0.21;
            double r = 2.0 + Math.sin(i * 0.5) * 0.5;
            serverWorld.sendParticles(ParticleTypes.FLAME,
                    headX + Math.cos(angle) * r,
                    headY + Math.sin(angle) * 0.5,
                    headZ + Math.sin(angle) * r,
                    5, 0.2, 0.2, 0.2, 0.05);
            serverWorld.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    headX + Math.cos(angle) * r * 0.5,
                    headY + 0.5,
                    headZ + Math.sin(angle) * r * 0.5,
                    3, 0.1, 0.1, 0.1, 0.02);
        }

        // Dragon eyes (two bright points)
        serverWorld.sendParticles(ParticleTypes.END_ROD,
                headX - look.z * 0.8, headY + 0.5, headZ + look.x * 0.8,
                10, 0.1, 0.1, 0.1, 0.02);
        serverWorld.sendParticles(ParticleTypes.END_ROD,
                headX + look.z * 0.8, headY + 0.5, headZ - look.x * 0.8,
                10, 0.1, 0.1, 0.1, 0.02);

        // PHASE 2: Breath attack - massive cone of fire
        double breathLength = 30.0;
        double coneAngle = 0.6; // Radians - wide cone

        for (double dist = 2; dist < breathLength; dist += 0.5) {
            double coneWidth = dist * Math.tan(coneAngle);

            // Calculate base position along look direction
            double bx = start.x + look.x * dist;
            double by = start.y + look.y * dist;
            double bz = start.z + look.z * dist;

            // Multiple streams within the cone
            int particlesAtDist = (int) (5 + dist * 1.5);
            for (int i = 0; i < particlesAtDist; i++) {
                // Random offset within cone
                double offsetX = (RANDOM.nextDouble() - 0.5) * coneWidth * 2;
                double offsetY = (RANDOM.nextDouble() - 0.5) * coneWidth;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * coneWidth * 2;

                // Calculate perpendicular vectors for proper cone
                Vector3d perp1 = look.cross(new Vector3d(0, 1, 0)).normalize();
                Vector3d perp2 = look.cross(perp1).normalize();

                double px = bx + perp1.x * offsetX + perp2.x * offsetY;
                double py = by + perp1.y * offsetX + perp2.y * offsetY + offsetY;
                double pz = bz + perp1.z * offsetX + perp2.z * offsetZ;

                // Primary fire
                serverWorld.sendParticles(ParticleTypes.FLAME,
                        px, py, pz, 2, 0.3, 0.3, 0.3, 0.1);

                // Intense inner flame
                if (RANDOM.nextFloat() > 0.5) {
                    serverWorld.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            px, py, pz, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Smoke trail
                if (dist > 10 && RANDOM.nextFloat() > 0.7) {
                    serverWorld.sendParticles(ParticleTypes.LARGE_SMOKE,
                            px, py + 0.5, pz, 1, 0.5, 0.5, 0.5, 0.02);
                }
            }

            // Lava drips for extra effect
            if (RANDOM.nextFloat() > 0.8) {
                serverWorld.sendParticles(ParticleTypes.LAVA,
                        bx + (RANDOM.nextDouble() - 0.5) * coneWidth,
                        by,
                        bz + (RANDOM.nextDouble() - 0.5) * coneWidth,
                        1, 0, 0, 0, 0);
            }
        }

        // PHASE 3: Damage and fire setting
        // Create hitbox along breath path
        for (double dist = 2; dist < breathLength; dist += 3) {
            double coneWidth = dist * Math.tan(coneAngle);

            Vector3d checkPos = start.add(look.scale(dist));

            AxisAlignedBB damageBox = new AxisAlignedBB(
                    checkPos.x - coneWidth, checkPos.y - coneWidth / 2, checkPos.z - coneWidth,
                    checkPos.x + coneWidth, checkPos.y + coneWidth / 2, checkPos.z + coneWidth);

            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, damageBox,
                    e -> e != player && e.isAlive());

            for (LivingEntity entity : entities) {
                // Devastating fire damage
                float damage = 25.0f - (float) (dist / breathLength * 10.0f); // More damage up close
                entity.hurt(DamageSource.IN_FIRE, Math.max(damage, 10.0f));
                entity.setSecondsOnFire(10); // Burn!

                // Knockback away from player
                Vector3d knockback = entity.position().subtract(player.position()).normalize();
                entity.push(knockback.x * 1.5, 0.5, knockback.z * 1.5);

                // Hit particles
                serverWorld.sendParticles(ParticleTypes.FLAME,
                        entity.getX(), entity.getY() + 1, entity.getZ(),
                        30, 0.5, 0.5, 0.5, 0.2);
            }

            // Set ground on fire
            if (RANDOM.nextFloat() > 0.6) {
                BlockPos groundPos = new BlockPos(checkPos.x, checkPos.y, checkPos.z);
                // Find ground level
                for (int dy = 0; dy > -5; dy--) {
                    BlockPos checkBlock = groundPos.offset(0, dy, 0);
                    if (!world.isEmptyBlock(checkBlock)) {
                        BlockPos firePos = checkBlock.above();
                        if (world.isEmptyBlock(firePos)) {
                            world.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
                        }
                        break;
                    }
                }
            }
        }

        // Final burst of particles at player
        serverWorld.sendParticles(ParticleTypes.FLAME,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 1.5, 1.0, 1.5, 0.2);

        // Dragon roar ending
        world.playSound(null, player.getX() + look.x * 15, player.getY(), player.getZ() + look.z * 15,
                SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 2.0f, 0.8f);

        return true;
    }
}
