package com.arcanemagic.block;

import com.arcanemagic.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ToolType;

import java.util.Random;

/**
 * Magic Ore - world-generated ore that drops mana crystal shards
 */
public class MagicOreBlock extends OreBlock {

    public MagicOreBlock() {
        super(Properties.of(Material.STONE)
                .strength(3.0f, 3.0f)
                .harvestLevel(2)
                .harvestTool(ToolType.PICKAXE)
                .sound(SoundType.STONE)
                .lightLevel(state -> 5)
                .requiresCorrectToolForDrops());
    }

    @Override
    protected int xpOnDrop(Random rand) {
        return MathHelper.nextInt(rand, 3, 7);
    }
}
