package com.arcanemagic.spell;

import com.arcanemagic.item.WandItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
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
 * Meteor Shower spell - rains fire from the sky (Archmage only)
 */
public class MeteorShowerSpell extends Spell {

    @Override
    public int getManaCost() {
        return 80;
    }

    @Override
    public int getCooldown() {
        return 200; // 10 seconds
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
        return 0xFF0000; // Red
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        // Target area where player is looking
        Vector3d look = player.getLookAngle();
        Vector3d targetCenter = player.position().add(look.scale(15.0));

        Random rand = world.random;
        int meteorCount = 10;

        // Play epic sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1.0f, 0.5f);

        // Spawn meteors
        for (int i = 0; i < meteorCount; i++) {
            double offsetX = (rand.nextDouble() - 0.5) * 10;
            double offsetZ = (rand.nextDouble() - 0.5) * 10;
            double spawnY = targetCenter.y + 15 + rand.nextDouble() * 5;

            double targetX = targetCenter.x + offsetX;
            double targetZ = targetCenter.z + offsetZ;

            // Create fireball falling from sky
            SmallFireballEntity meteor = new SmallFireballEntity(
                    world,
                    targetX,
                    spawnY,
                    targetZ,
                    0,
                    -1.0,
                    0);

            meteor.setOwner(player);

            // Delay spawn for dramatic effect
            int delay = i * 4;
            scheduleSpawn(world, meteor, delay);
        }

        // Spawn visual effects at target area
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.FLAME,
                    targetCenter.x, targetCenter.y + 10, targetCenter.z,
                    100, 5.0, 2.0, 5.0, 0.1);

            ((ServerWorld) world).sendParticles(ParticleTypes.LAVA,
                    targetCenter.x, targetCenter.y + 5, targetCenter.z,
                    50, 5.0, 1.0, 5.0, 0.1);
        }

        // Deal damage to enemies in area
        BlockPos centerPos = new BlockPos(targetCenter);
        AxisAlignedBB damageArea = new AxisAlignedBB(centerPos).inflate(8.0);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, damageArea,
                e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            entity.hurt(DamageSource.ON_FIRE, 10.0f);
            entity.setSecondsOnFire(5);
        }

        return true;
    }

    private void scheduleSpawn(World world, SmallFireballEntity meteor, int delay) {
        // In a real mod, you'd use a scheduled task
        // For simplicity, spawn immediately (meteors will fall at different X/Z)
        world.addFreshEntity(meteor);
    }
}
