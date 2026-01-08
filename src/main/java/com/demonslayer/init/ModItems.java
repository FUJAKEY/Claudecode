package com.demonslayer.init;

import com.demonslayer.DemonSlayerMod;
import com.demonslayer.items.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, DemonSlayerMod.MOD_ID);

    // ========== NICHIRIN SWORDS ==========
    public static final RegistryObject<Item> NICHIRIN_SWORD_BLACK = ITEMS.register("nichirin_sword_black",
            () -> new NichirinSwordItem(NichirinSwordItem.NichirinColor.BLACK, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> NICHIRIN_SWORD_BLUE = ITEMS.register("nichirin_sword_blue",
            () -> new NichirinSwordItem(NichirinSwordItem.NichirinColor.BLUE, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> NICHIRIN_SWORD_RED = ITEMS.register("nichirin_sword_red",
            () -> new NichirinSwordItem(NichirinSwordItem.NichirinColor.RED, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> NICHIRIN_SWORD_YELLOW = ITEMS.register("nichirin_sword_yellow",
            () -> new NichirinSwordItem(NichirinSwordItem.NichirinColor.YELLOW, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> NICHIRIN_SWORD_GREEN = ITEMS.register("nichirin_sword_green",
            () -> new NichirinSwordItem(NichirinSwordItem.NichirinColor.GREEN, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> NICHIRIN_SWORD_PINK = ITEMS.register("nichirin_sword_pink",
            () -> new NichirinSwordItem(NichirinSwordItem.NichirinColor.PINK, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> NICHIRIN_SWORD_WHITE = ITEMS.register("nichirin_sword_white",
            () -> new NichirinSwordItem(NichirinSwordItem.NichirinColor.WHITE, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    // ========== MATERIALS ==========
    public static final RegistryObject<Item> SCARLET_CRIMSON_ORE = ITEMS.register("scarlet_crimson_ore",
            () -> new Item(new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SCARLET_CRIMSON_INGOT = ITEMS.register("scarlet_crimson_ingot",
            () -> new Item(new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> WISTERIA_FLOWER = ITEMS.register("wisteria_flower",
            () -> new WisteriaItem(new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB)));

    public static final RegistryObject<Item> DEMON_BLOOD = ITEMS.register("demon_blood",
            () -> new Item(new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).rarity(Rarity.UNCOMMON)));

    // ========== SLAYER UNIFORM ==========
    public static final RegistryObject<Item> SLAYER_HAORI = ITEMS.register("slayer_haori",
            () -> new SlayerArmorItem(EquipmentSlotType.CHEST, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SLAYER_PANTS = ITEMS.register("slayer_pants",
            () -> new SlayerArmorItem(EquipmentSlotType.LEGS, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SLAYER_BOOTS = ITEMS.register("slayer_boots",
            () -> new SlayerArmorItem(EquipmentSlotType.FEET, 
                    new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).rarity(Rarity.RARE)));

    // ========== BREATHING SCROLL ==========
    public static final RegistryObject<Item> BREATHING_SCROLL = ITEMS.register("breathing_scroll",
            () -> new BreathingScrollItem(new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    // ========== V2.0 NEW ITEMS ==========
    public static final RegistryObject<Item> DUAL_NICHIRIN = ITEMS.register("dual_nichirin",
            () -> new DualNichirinItem(new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> ONIGIRI = ITEMS.register("onigiri",
            () -> new OnigiriItem(new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(16)));

    public static final RegistryObject<Item> DEMON_ART_BOOK = ITEMS.register("demon_art_book",
            () -> new DemonArtBookItem(new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> HASHIRA_BADGE = ITEMS.register("hashira_badge",
            () -> new Item(new Item.Properties().tab(ModItemGroup.DEMON_SLAYER_TAB).stacksTo(1).rarity(Rarity.EPIC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
