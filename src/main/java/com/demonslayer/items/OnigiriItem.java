package com.demonslayer.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

/**
 * Onigiri - Special food for demon slayers
 */
public class OnigiriItem extends Item {
    
    public OnigiriItem(Properties properties) {
        super(properties.food(new Food.Builder()
            .nutrition(8)
            .saturationMod(0.8F)
            .alwaysEat()
            .build()));
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity && !world.isClientSide) {
            PlayerEntity player = (PlayerEntity) entity;
            
            // Special buffs
            player.addEffect(new EffectInstance(Effects.REGENERATION, 200, 1));
            player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 400, 0));
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 400, 0));
            
            // Heal extra
            player.heal(4.0F);
        }
        
        return super.finishUsingItem(stack, world, entity);
    }
}
