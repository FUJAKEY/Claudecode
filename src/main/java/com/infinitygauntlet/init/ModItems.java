package com.infinitygauntlet.init;

import com.infinitygauntlet.InfinityGauntletMod;
import com.infinitygauntlet.items.*;
import com.infinitygauntlet.items.armor.NanoArmorItem;
import com.infinitygauntlet.items.weapons.ThanosSwordItem;
import com.infinitygauntlet.items.weapons.VibraniumShieldItem;
import net.minecraft.inventory.EquipmentSlotType;
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

    // ========== WEAPONS ==========
    public static final RegistryObject<Item> THANOS_SWORD = ITEMS.register("thanos_sword",
            () -> new ThanosSwordItem(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant()));

    public static final RegistryObject<Item> VIBRANIUM_SHIELD = ITEMS.register("vibranium_shield",
            () -> new VibraniumShieldItem(new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .stacksTo(1)
                    .durability(1000)
                    .rarity(Rarity.EPIC)));

    // ========== NANO ARMOR ==========
    public static final RegistryObject<Item> NANO_HELMET = ITEMS.register("nano_helmet",
            () -> new NanoArmorItem(EquipmentSlotType.HEAD, new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .rarity(Rarity.EPIC)
                    .fireResistant()));

    public static final RegistryObject<Item> NANO_CHESTPLATE = ITEMS.register("nano_chestplate",
            () -> new NanoArmorItem(EquipmentSlotType.CHEST, new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .rarity(Rarity.EPIC)
                    .fireResistant()));

    public static final RegistryObject<Item> NANO_LEGGINGS = ITEMS.register("nano_leggings",
            () -> new NanoArmorItem(EquipmentSlotType.LEGS, new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .rarity(Rarity.EPIC)
                    .fireResistant()));

    public static final RegistryObject<Item> NANO_BOOTS = ITEMS.register("nano_boots",
            () -> new NanoArmorItem(EquipmentSlotType.FEET, new Item.Properties()
                    .tab(ModItemGroup.INFINITY_GAUNTLET_TAB)
                    .rarity(Rarity.EPIC)
                    .fireResistant()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
