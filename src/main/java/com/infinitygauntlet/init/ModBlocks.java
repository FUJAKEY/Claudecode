package com.infinitygauntlet.init;

import com.infinitygauntlet.InfinityGauntletMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.OreBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = 
            DeferredRegister.create(ForgeRegistries.BLOCKS, InfinityGauntletMod.MOD_ID);

    // Vibranium Ore - very rare, generates deep underground
    public static final RegistryObject<Block> VIBRANIUM_ORE = BLOCKS.register("vibranium_ore",
            () -> new OreBlock(AbstractBlock.Properties.of(Material.STONE)
                    .requiresCorrectToolForDrops()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(3)
                    .strength(50.0F, 1200.0F)
                    .sound(SoundType.ANCIENT_DEBRIS)));

    // Vibranium Block - storage block
    public static final RegistryObject<Block> VIBRANIUM_BLOCK = BLOCKS.register("vibranium_block",
            () -> new Block(AbstractBlock.Properties.of(Material.METAL)
                    .requiresCorrectToolForDrops()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(3)
                    .strength(100.0F, 2400.0F)
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
