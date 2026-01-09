package com.arcanemagic.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ToolType;

import java.util.Random;

/**
 * Rune Stone - decorative/functional block with magical runes
 */
public class RuneStoneBlock extends Block {

    public RuneStoneBlock() {
        super(Properties.of(Material.STONE)
                .strength(3.5f, 4.0f)
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE)
                .sound(SoundType.STONE)
                .lightLevel(state -> 3)
                .requiresCorrectToolForDrops());
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        // Subtle rune glow particles
        if (rand.nextInt(8) == 0) {
            double x = pos.getX() + rand.nextDouble();
            double y = pos.getY() + rand.nextDouble();
            double z = pos.getZ() + rand.nextDouble();

            world.addParticle(ParticleTypes.ENCHANT,
                    x, y, z,
                    0, 0.1, 0);
        }
    }
}
