package com.arcanemagic.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.Random;

/**
 * Arcane Pedestal - used for crafting and upgrading magical items
 */
public class ArcanePedestalBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

    public ArcanePedestalBlock() {
        super(Properties.of(Material.STONE)
                .strength(4.0f, 5.0f)
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE)
                .sound(SoundType.STONE)
                .lightLevel(state -> 4)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
            BlockRayTraceResult hit) {
        // TODO: Implement crafting/upgrading logic
        // For now, just show particles to indicate it's interactive
        if (world.isClientSide) {
            for (int i = 0; i < 10; i++) {
                world.addParticle(ParticleTypes.ENCHANT,
                        pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                        pos.getY() + 1.0 + world.random.nextDouble() * 0.5,
                        pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                        0, 0.5, 0);
            }
        }

        return ActionResultType.sidedSuccess(world.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        // Ambient particles
        if (rand.nextInt(3) == 0) {
            world.addParticle(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5,
                    (rand.nextDouble() - 0.5) * 0.2,
                    0.2,
                    (rand.nextDouble() - 0.5) * 0.2);
        }
    }
}
