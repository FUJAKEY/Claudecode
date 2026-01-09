package com.arcanemagic.spell;

import com.arcanemagic.item.WandItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Ice Shard spell - freezing projectile that slows enemies
 */
public class IceShardSpell extends Spell {

    @Override
    public int getManaCost() {
        return 12;
    }

    @Override
    public int getCooldown() {
        return 30; // 1.5 seconds
    }

    @Override
    public WandItem.WandTier getMinTier() {
        return WandItem.WandTier.APPRENTICE;
    }

    @Override
    public String getSpellId() {
        return "ice_shard";
    }

    @Override
    public int getSpellColor() {
        return 0x00BFFF; // Deep sky blue
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        Vector3d look = player.getLookAngle();

        // Create ice shard projectile (using snowball as base)
        IceShardProjectile shard = new IceShardProjectile(world, player);
        shard.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        shard.shoot(look.x, look.y, look.z, 2.0f, 0.1f);
        world.addFreshEntity(shard);

        // Play sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GLASS_BREAK, SoundCategory.PLAYERS, 0.8f, 1.5f);

        // Spawn particles
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.SNOWFLAKE,
                    player.getX() + look.x, player.getEyeY(), player.getZ() + look.z,
                    10, 0.2, 0.2, 0.2, 0.05);
        }

        return true;
    }

    /**
     * Custom ice shard projectile
     */
    public static class IceShardProjectile extends SnowballEntity {

        public IceShardProjectile(World world, LivingEntity thrower) {
            super(world, thrower);
        }

        @Override
        protected void onHit(RayTraceResult result) {
            super.onHit(result);

            if (result instanceof EntityRayTraceResult) {
                Entity target = ((EntityRayTraceResult) result).getEntity();
                if (target instanceof LivingEntity && target != getOwner()) {
                    LivingEntity living = (LivingEntity) target;

                    // Deal damage
                    living.hurt(DamageSource.MAGIC, 5.0f);

                    // Apply slowness
                    living.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, 2));

                    // Spawn ice particles
                    if (level instanceof ServerWorld) {
                        ((ServerWorld) level).sendParticles(ParticleTypes.SNOWFLAKE,
                                target.getX(), target.getY() + 1, target.getZ(),
                                20, 0.5, 0.5, 0.5, 0.1);
                    }
                }
            }
        }
    }
}
