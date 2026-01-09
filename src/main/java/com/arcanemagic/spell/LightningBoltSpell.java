package com.arcanemagic.spell;

import com.arcanemagic.item.WandItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
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

/**
 * Lightning Bolt spell - instant chain lightning that hits enemies
 */
public class LightningBoltSpell extends Spell {

    @Override
    public int getManaCost() {
        return 25;
    }

    @Override
    public int getCooldown() {
        return 60; // 3 seconds
    }

    @Override
    public WandItem.WandTier getMinTier() {
        return WandItem.WandTier.ADEPT;
    }

    @Override
    public String getSpellId() {
        return "lightning_bolt";
    }

    @Override
    public int getSpellColor() {
        return 0xFFFF00; // Yellow
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        // Raycast to find target position
        Vector3d start = player.getEyePosition(1.0f);
        Vector3d look = player.getLookAngle();
        Vector3d end = start.add(look.scale(30.0)); // 30 block range

        BlockRayTraceResult result = world.clip(new RayTraceContext(
                start, end,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                player));

        Vector3d hitPos;
        if (result.getType() != RayTraceResult.Type.MISS) {
            hitPos = result.getLocation();
        } else {
            hitPos = end;
        }

        // Spawn lightning bolt
        LightningBoltEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.moveTo(hitPos.x, hitPos.y, hitPos.z);
            world.addFreshEntity(lightning);
        }

        // Chain to nearby enemies
        BlockPos pos = new BlockPos(hitPos);
        AxisAlignedBB area = new AxisAlignedBB(pos).inflate(5.0);
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity entity : nearbyEntities) {
            if (nearbyEntities.indexOf(entity) < 3) { // Chain to max 3 enemies
                entity.hurt(DamageSource.LIGHTNING_BOLT, 8.0f);

                // Visual effect
                if (world instanceof ServerWorld) {
                    ((ServerWorld) world).sendParticles(ParticleTypes.FLASH,
                            entity.getX(), entity.getY() + 1, entity.getZ(),
                            5, 0.5, 1.0, 0.5, 0.1);
                }
            }
        }

        // Play thunder sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.5f, 1.2f);

        return true;
    }
}
