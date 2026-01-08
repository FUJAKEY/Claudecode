package com.infinitygauntlet.items.armor;

import com.infinitygauntlet.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class NanoArmorItem extends ArmorItem {
    
    public static final IArmorMaterial NANO_MATERIAL = new IArmorMaterial() {
        private final int[] DURABILITY = {546, 630, 672, 462};
        private final int[] PROTECTION = {4, 7, 9, 4};
        
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
            return 20;
        }
        
        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_NETHERITE;
        }
        
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.VIBRANIUM_INGOT.get());
        }
        
        @Override
        public String getName() {
            return "infinitygauntlet:nano";
        }
        
        @Override
        public float getToughness() {
            return 4.0F;
        }
        
        @Override
        public float getKnockbackResistance() {
            return 0.2F;
        }
    };
    
    public NanoArmorItem(EquipmentSlotType slot, Properties properties) {
        super(NANO_MATERIAL, slot, properties);
    }
    
    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClientSide) return;
        
        // Check for full set
        boolean hasFullSet = true;
        for (ItemStack armorPiece : player.getArmorSlots()) {
            if (!(armorPiece.getItem() instanceof NanoArmorItem)) {
                hasFullSet = false;
                break;
            }
        }
        
        if (hasFullSet && world.getGameTime() % 20 == 0) {
            // Full set bonuses
            player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 25, 1, false, false));
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 25, 1, false, false));
            player.addEffect(new EffectInstance(Effects.REGENERATION, 25, 0, false, false));
            
            // Auto-repair armor
            for (ItemStack armor : player.getArmorSlots()) {
                if (armor.isDamaged() && world.getGameTime() % 100 == 0) {
                    armor.setDamageValue(armor.getDamageValue() - 1);
                }
            }
            
            // Particle effect
            if (world instanceof ServerWorld && world.getGameTime() % 40 == 0) {
                ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1, player.getZ(), 3, 0.3, 0.5, 0.3, 0.02);
            }
        }
    }
    
    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, net.minecraft.entity.LivingEntity wearer) {
        return true;
    }
}
