package com.arcanemagic.block;

import com.arcanemagic.capability.ManaCapability;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

import java.util.List;
import java.util.Random;

/**
 * Mana Altar - provides mana regeneration boost to nearby players
 */
public class ManaAltarBlock extends Block {

    private static final int EFFECT_RADIUS = 5;
    private static final int BONUS_REGEN = 2; // Extra mana per tick

    public ManaAltarBlock() {
        super(Properties.of(Material.STONE)
                .strength(5.0f, 6.0f)
                .harvestLevel(2)
                .harvestTool(ToolType.PICKAXE)
                .sound(SoundType.STONE)
                .lightLevel(state -> 8)
                .requiresCorrectToolForDrops());
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        // Spawn magical particles
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        // Purple enchantment particles rising
        for (int i = 0; i < 3; i++) {
            double offsetX = (rand.nextDouble() - 0.5) * 0.5;
            double offsetZ = (rand.nextDouble() - 0.5) * 0.5;
            world.addParticle(ParticleTypes.ENCHANT,
                    x + offsetX, y + rand.nextDouble() * 0.5, z + offsetZ,
                    0, 0.5, 0);
        }

        // Occasional portal particle
        if (rand.nextInt(5) == 0) {
            world.addParticle(ParticleTypes.PORTAL,
                    x + (rand.nextDouble() - 0.5) * 2,
                    y + rand.nextDouble(),
                    z + (rand.nextDouble() - 0.5) * 2,
                    0, 0.2, 0);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        // Find nearby players and boost their mana regen
        AxisAlignedBB area = new AxisAlignedBB(pos).inflate(EFFECT_RADIUS);
        List<PlayerEntity> players = world.getEntitiesOfClass(PlayerEntity.class, area);

        for (PlayerEntity player : players) {
            player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
                mana.regenerateMana(BONUS_REGEN);
            });
        }
    }
}
