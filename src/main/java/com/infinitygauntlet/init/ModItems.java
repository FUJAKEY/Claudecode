package com.infinitygauntlet.init;

import com.infinitygauntlet.InfinityGauntletMod;
import com.infinitygauntlet.items.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, InfinityGauntletMod.MOD_ID);

    // ========== INFINITY GAUNTLET ==========
    public static final RegistryObject<Item> INFINITY_GAUNTLET = ITEMS.register("infinity_gauntlet",
            () -> new InfinityGauntletItem(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant()));

    // ========== INFINITY STONES ==========
    public static final RegistryObject<Item> SPACE_STONE = ITEMS.register("space_stone",
            () -> new SpaceStoneItem(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> TIME_STONE = ITEMS.register("time_stone",
            () -> new TimeStoneItem(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> REALITY_STONE = ITEMS.register("reality_stone",
            () -> new RealityStoneItem(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> POWER_STONE = ITEMS.register("power_stone",
            () -> new PowerStoneItem(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> MIND_STONE = ITEMS.register("mind_stone",
            () -> new MindStoneItem(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> SOUL_STONE = ITEMS.register("soul_stone",
            () -> new SoulStoneItem(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    // ========== VIBRANIUM ==========
    public static final RegistryObject<Item> VIBRANIUM_INGOT = ITEMS.register("vibranium_ingot",
            () -> new Item(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .rarity(Rarity.RARE)
                    .fireResistant()));

    public static final RegistryObject<Item> VIBRANIUM_NUGGET = ITEMS.register("vibranium_nugget",
            () -> new Item(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .rarity(Rarity.UNCOMMON)));

    // Block Items
    public static final RegistryObject<Item> VIBRANIUM_ORE_ITEM = ITEMS.register("vibranium_ore",
            () -> new BlockItem(ModBlocks.VIBRANIUM_ORE.get(), new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> VIBRANIUM_BLOCK_ITEM = ITEMS.register("vibranium_block",
            () -> new BlockItem(ModBlocks.VIBRANIUM_BLOCK.get(), new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .rarity(Rarity.RARE)));

    // ========== GAUNTLET CORE ==========
    public static final RegistryObject<Item> GAUNTLET_CORE = ITEMS.register("gauntlet_core",
            () -> new Item(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
