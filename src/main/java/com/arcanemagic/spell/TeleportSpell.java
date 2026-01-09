package com.arcanemagic.spell;

import com.arcanemagic.item.WandItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Teleport spell - teleports player forward
 */
public class TeleportSpell extends Spell {

    @Override
    public int getManaCost() {
        return 40;
    }

    @Override
    public int getCooldown() {
        return 80; // 4 seconds
    }

    @Override
    public WandItem.WandTier getMinTier() {
        return WandItem.WandTier.MASTER;
    }

    @Override
    public String getSpellId() {
        return "teleport";
    }

    @Override
    public int getSpellColor() {
        return 0x9400D3; // Purple
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        // Calculate teleport destination
        Vector3d start = player.getEyePosition(1.0f);
        Vector3d look = player.getLookAngle();
        double maxDistance = 20.0; // 20 blocks

        Vector3d end = start.add(look.scale(maxDistance));

        // Raycast to find safe landing spot
        BlockRayTraceResult result = world.clip(new RayTraceContext(
                start, end,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                player));

        Vector3d teleportPos;
        if (result.getType() != RayTraceResult.Type.MISS) {
            // Hit something, step back a bit
            teleportPos = result.getLocation().subtract(look.scale(0.5));
        } else {
            teleportPos = end;
        }

        // Find safe ground position
        BlockPos blockPos = new BlockPos(teleportPos);
        while (blockPos.getY() > 0 && world.isEmptyBlock(blockPos.below())) {
            blockPos = blockPos.below();
        }

        // Spawn departure particles
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    50, 0.5, 1.0, 0.5, 0.5);
        }

        // Play departure sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // Teleport player
        player.teleportTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);

        // Spawn arrival particles
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    50, 0.5, 1.0, 0.5, 0.5);

            ((ServerWorld) world).sendParticles(ParticleTypes.REVERSE_PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    30, 0.5, 1.0, 0.5, 0.1);
        }

        // Play arrival sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.2f);

        return true;
    }
}
