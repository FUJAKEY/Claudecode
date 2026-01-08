package com.demonslayer.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
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

public class SlayerArmorItem extends ArmorItem {
    
    public static final IArmorMaterial SLAYER_MATERIAL = new IArmorMaterial() {
        private final int[] DURABILITY = {330, 480, 510, 360};
        private final int[] PROTECTION = {3, 6, 8, 3};
        
        @Override
        public int getDurabilityForSlot(EquipmentSlotType slot) {
            return DURABILITY[slot.getIndex()];
        }
        
        @Override
        public int getDefenseForSlot(EquipmentSlotType slot) {
            return PROTECTION[slot.getIndex()];
        }
        
        @Override
        public int getEnchantmentValue() {
            return 15;
        }
        
        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }
        
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
        
        @Override
        public String getName() {
            return "demonslayer:slayer";
        }
        
        @Override
        public float getToughness() {
            return 2.0F;
        }
        
        @Override
        public float getKnockbackResistance() {
            return 0.1F;
        }
    };
    
    public SlayerArmorItem(EquipmentSlotType slot, Properties properties) {
        super(SLAYER_MATERIAL, slot, properties);
    }
    
    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClientSide) return;
        
        // Check for full set (we only have 3 pieces, no helmet)
        int pieces = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof SlayerArmorItem) {
                pieces++;
            }
        }
        
        if (pieces >= 3 && world.getGameTime() % 100 == 0) {
            // Set bonus: slight movement speed and stamina retention
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 105, 0, false, false));
        }
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Demon Slayer Corps Uniform")
            .withStyle(TextFormatting.DARK_GREEN));
        tooltip.add(new StringTextComponent("Set Bonus (3 pieces):")
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("  • Speed I")
            .withStyle(TextFormatting.WHITE));
    }
}
