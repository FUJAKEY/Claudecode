package com.demonslayer.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Hashira Haori - Special cloaks for each Hashira with unique effects
 */
public class HashiraHaoriItem extends ArmorItem {
    
    public enum HashiraType {
        TANJURO("Tanjuro", TextFormatting.RED, Effects.FIRE_RESISTANCE, "Sun Breathing"),
        GIYU("Giyu Tomioka", TextFormatting.BLUE, Effects.WATER_BREATHING, "Water Breathing"),
        SHINOBU("Shinobu Kocho", TextFormatting.LIGHT_PURPLE, Effects.MOVEMENT_SPEED, "Insect Breathing"),
        RENGOKU("Kyojuro Rengoku", TextFormatting.GOLD, Effects.DAMAGE_BOOST, "Flame Breathing"),
        TENGEN("Tengen Uzui", TextFormatting.WHITE, Effects.DIG_SPEED, "Sound Breathing"),
        MITSURI("Mitsuri Kanroji", TextFormatting.LIGHT_PURPLE, Effects.REGENERATION, "Love Breathing"),
        MUICHIRO("Muichiro Tokito", TextFormatting.AQUA, Effects.INVISIBILITY, "Mist Breathing"),
        GYOMEI("Gyomei Himejima", TextFormatting.GRAY, Effects.DAMAGE_RESISTANCE, "Stone Breathing"),
        SANEMI("Sanemi Shinazugawa", TextFormatting.GREEN, Effects.JUMP, "Wind Breathing");
        
        public final String name;
        public final TextFormatting color;
        public final net.minecraft.potion.Effect effect;
        public final String breathing;
        
        HashiraType(String name, TextFormatting color, net.minecraft.potion.Effect effect, String breathing) {
            this.name = name;
            this.color = color;
            this.effect = effect;
            this.breathing = breathing;
        }
    }
    
    public static final IArmorMaterial HASHIRA_MATERIAL = new IArmorMaterial() {
        @Override public int getDurabilityForSlot(EquipmentSlotType slot) { return 400; }
        @Override public int getDefenseForSlot(EquipmentSlotType slot) { return 6; }
        @Override public int getEnchantmentValue() { return 20; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
        @Override public String getName() { return "demonslayer:hashira"; }
        @Override public float getToughness() { return 2.0F; }
        @Override public float getKnockbackResistance() { return 0.1F; }
    };
    
    private final HashiraType hashiraType;
    
    public HashiraHaoriItem(HashiraType type, Properties properties) {
        super(HASHIRA_MATERIAL, EquipmentSlotType.CHEST, properties);
        this.hashiraType = type;
    }
    
    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClientSide) return;
        
        // Apply hashira-specific effect
        if (world.getGameTime() % 100 == 0) {
            player.addEffect(new EffectInstance(hashiraType.effect, 120, 0, false, false));
        }
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("柱 Hashira Haori")
            .withStyle(hashiraType.color).withStyle(TextFormatting.BOLD));
        tooltip.add(new StringTextComponent(hashiraType.name)
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(hashiraType.breathing)
            .withStyle(TextFormatting.WHITE));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Effect: " + hashiraType.effect.getDisplayName().getString())
            .withStyle(TextFormatting.GREEN));
    }
    
    public HashiraType getHashiraType() {
        return hashiraType;
    }
}
