package com.arcanemagic.init;

import com.arcanemagic.ArcaneMagicMod;
import com.arcanemagic.block.ManaAltarBlock;
import com.arcanemagic.block.ArcanePedestalBlock;
import com.arcanemagic.block.MagicOreBlock;
import com.arcanemagic.block.RuneStoneBlock;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Registry for all mod blocks
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            ArcaneMagicMod.MOD_ID);

    // ========== MAGIC BLOCKS ==========
    public static final RegistryObject<Block> MANA_ALTAR = BLOCKS.register("mana_altar",
            ManaAltarBlock::new);

    public static final RegistryObject<Block> ARCANE_PEDESTAL = BLOCKS.register("arcane_pedestal",
            ArcanePedestalBlock::new);

    public static final RegistryObject<Block> MAGIC_ORE = BLOCKS.register("magic_ore",
            MagicOreBlock::new);

    public static final RegistryObject<Block> RUNE_STONE = BLOCKS.register("rune_stone",
            RuneStoneBlock::new);
}
