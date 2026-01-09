package com.arcanemagic.init;

import com.arcanemagic.ArcaneMagicMod;
import com.arcanemagic.item.ManaCrystalItem;
import com.arcanemagic.item.SpellTomeItem;
import com.arcanemagic.item.WandItem;
import com.arcanemagic.spell.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Registry for all mod items
 */
public class ModItems {

        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
                        ArcaneMagicMod.MOD_ID);

        // Default item properties
        private static Item.Properties defaultProps() {
                return new Item.Properties().tab(ArcaneMagicMod.ARCANE_TAB);
        }

        // ========== MANA CRYSTALS ==========
        public static final RegistryObject<Item> MANA_CRYSTAL = ITEMS.register("mana_crystal",
                        () -> new ManaCrystalItem(defaultProps().stacksTo(64)));

        public static final RegistryObject<Item> MANA_CRYSTAL_SHARD = ITEMS.register("mana_crystal_shard",
                        () -> new Item(defaultProps().stacksTo(64)));

        // ========== WAND CORES ==========
        public static final RegistryObject<Item> APPRENTICE_CORE = ITEMS.register("apprentice_core",
                        () -> new Item(defaultProps().stacksTo(16)));

        public static final RegistryObject<Item> ADEPT_CORE = ITEMS.register("adept_core",
                        () -> new Item(defaultProps().stacksTo(16)));

        public static final RegistryObject<Item> MASTER_CORE = ITEMS.register("master_core",
                        () -> new Item(defaultProps().stacksTo(16)));

        public static final RegistryObject<Item> ARCHMAGE_CORE = ITEMS.register("archmage_core",
                        () -> new Item(defaultProps().stacksTo(16).rarity(net.minecraft.item.Rarity.EPIC)));

        // ========== WANDS ==========
        public static final RegistryObject<Item> APPRENTICE_WAND = ITEMS.register("apprentice_wand",
                        () -> new WandItem(WandItem.WandTier.APPRENTICE, defaultProps().stacksTo(1).durability(100)));

        public static final RegistryObject<Item> ADEPT_WAND = ITEMS.register("adept_wand",
                        () -> new WandItem(WandItem.WandTier.ADEPT, defaultProps().stacksTo(1).durability(250)));

        public static final RegistryObject<Item> MASTER_WAND = ITEMS.register("master_wand",
                        () -> new WandItem(WandItem.WandTier.MASTER, defaultProps().stacksTo(1).durability(500)
                                        .rarity(net.minecraft.item.Rarity.RARE)));

        public static final RegistryObject<Item> ARCHMAGE_WAND = ITEMS.register("archmage_wand",
                        () -> new WandItem(WandItem.WandTier.ARCHMAGE, defaultProps().stacksTo(1)
                                        .rarity(net.minecraft.item.Rarity.EPIC)));

        // ========== SPELL TOMES ==========
        public static final RegistryObject<Item> TOME_FIREBALL = ITEMS.register("tome_fireball",
                        () -> new SpellTomeItem(new FireballSpell(), defaultProps().stacksTo(1)));

        public static final RegistryObject<Item> TOME_ICE_SHARD = ITEMS.register("tome_ice_shard",
                        () -> new SpellTomeItem(new IceShardSpell(), defaultProps().stacksTo(1)));

        public static final RegistryObject<Item> TOME_LIGHTNING = ITEMS.register("tome_lightning",
                        () -> new SpellTomeItem(new LightningBoltSpell(), defaultProps().stacksTo(1)));

        public static final RegistryObject<Item> TOME_HEAL = ITEMS.register("tome_heal",
                        () -> new SpellTomeItem(new HealSpell(), defaultProps().stacksTo(1)));

        public static final RegistryObject<Item> TOME_TELEPORT = ITEMS.register("tome_teleport",
                        () -> new SpellTomeItem(new TeleportSpell(), defaultProps().stacksTo(1)));

        public static final RegistryObject<Item> TOME_METEOR = ITEMS.register("tome_meteor",
                        () -> new SpellTomeItem(new MeteorShowerSpell(), defaultProps().stacksTo(1)
                                        .rarity(net.minecraft.item.Rarity.EPIC)));

        public static final RegistryObject<Item> TOME_VOID_RIFT = ITEMS.register("tome_void_rift",
                        () -> new SpellTomeItem(new VoidRiftSpell(), defaultProps().stacksTo(1)
                                        .rarity(net.minecraft.item.Rarity.RARE)));

        public static final RegistryObject<Item> TOME_DRAGONS_WRATH = ITEMS.register("tome_dragons_wrath",
                        () -> new SpellTomeItem(new DragonsWrathSpell(), defaultProps().stacksTo(1)
                                        .rarity(net.minecraft.item.Rarity.EPIC)));

        public static final RegistryObject<Item> TOME_THE_WORLD = ITEMS.register("tome_the_world",
                        () -> new SpellTomeItem(new TheWorldSpell(), defaultProps().stacksTo(1)
                                        .rarity(net.minecraft.item.Rarity.EPIC)));

        // ========== DIVINE STAFF ==========
        public static final RegistryObject<Item> DIVINE_CORE = ITEMS.register("divine_core",
                        () -> new Item(defaultProps().stacksTo(1).rarity(net.minecraft.item.Rarity.EPIC)));

        public static final RegistryObject<Item> DIVINE_STAFF = ITEMS.register("divine_staff",
                        () -> new WandItem(WandItem.WandTier.DIVINE, defaultProps().stacksTo(1)
                                        .rarity(net.minecraft.item.Rarity.EPIC)));

        // ========== BLOCK ITEMS ==========
        public static final RegistryObject<Item> MANA_ALTAR_ITEM = ITEMS.register("mana_altar",
                        () -> new BlockItem(ModBlocks.MANA_ALTAR.get(), defaultProps()));

        public static final RegistryObject<Item> ARCANE_PEDESTAL_ITEM = ITEMS.register("arcane_pedestal",
                        () -> new BlockItem(ModBlocks.ARCANE_PEDESTAL.get(), defaultProps()));

        public static final RegistryObject<Item> MAGIC_ORE_ITEM = ITEMS.register("magic_ore",
                        () -> new BlockItem(ModBlocks.MAGIC_ORE.get(), defaultProps()));

        public static final RegistryObject<Item> RUNE_STONE_ITEM = ITEMS.register("rune_stone",
                        () -> new BlockItem(ModBlocks.RUNE_STONE.get(), defaultProps()));
}
