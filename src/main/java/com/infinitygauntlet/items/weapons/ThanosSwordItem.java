package com.infinitygauntlet.items.weapons;

import com.infinitygauntlet.init.ModItems;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class ThanosSwordItem extends SwordItem {
    
    public static final IItemTier THANOS_TIER = new IItemTier() {
        @Override
        public int getUses() { return 3000; }
        
        @Override
        public float getSpeed() { return 10.0F; }
        
        @Override
        public float getAttackDamageBonus() { return 8.0F; }
        
        @Override
        public int getLevel() { return 5; }
        
        @Override
        public int getEnchantmentValue() { return 22; }
        
        @Override
        public net.minecraft.item.crafting.Ingredient getRepairIngredient() {
            return net.minecraft.item.crafting.Ingredient.of(ModItems.VIBRANIUM_INGOT.get());
        }
    };
    
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;
    
    public ThanosSwordItem(Properties properties) {
        super(THANOS_TIER, 3, -2.0F, properties);
        
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, 
            "Weapon modifier", 14.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, 
            "Weapon modifier", -2.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
            java.util.UUID.fromString("9e7ce82f-2468-4e63-9d77-66b8f6a4e43e"),
            "Weapon modifier", 0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        this.attributeModifiers = builder.build();
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Cleave attack - damage nearby enemies
        if (!attacker.level.isClientSide && attacker instanceof PlayerEntity) {
            World world = attacker.level;
            AxisAlignedBB area = target.getBoundingBox().inflate(3);
            List<LivingEntity> nearby = world.getEntitiesOfClass(LivingEntity.class, area, 
                e -> e != attacker && e != target);
            
            for (LivingEntity entity : nearby) {
                entity.hurt(net.minecraft.util.DamageSource.playerAttack((PlayerEntity) attacker), 8.0F);
                
                // Knockback
                Vector3d knockback = entity.position().subtract(attacker.position()).normalize().scale(1.5);
                entity.setDeltaMovement(knockback.x, 0.3, knockback.z);
                entity.hurtMarked = true;
            }
            
            // Particles
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.SWEEP_ATTACK,
                    target.getX(), target.getY() + 1, target.getZ(), 5, 1, 0.5, 1, 0);
                ((ServerWorld) world).sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
    
    @Override
    public boolean canAttackBlock(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        return !player.isCreative();
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        return slot == EquipmentSlotType.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot, stack);
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
