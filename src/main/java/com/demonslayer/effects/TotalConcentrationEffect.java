package com.demonslayer.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class TotalConcentrationEffect extends Effect {
    
    public TotalConcentrationEffect() {
        super(EffectType.BENEFICIAL, 0x00FFFF); // Cyan color
        
        // Attribute modifiers
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, 
            "a47c6b6d-2c3e-4e9f-8a1b-3f5e7d9c1a2b", 
            2.0D, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, 
            "b58d7c7e-3d4f-5f0g-9b2c-4g6f8e0d2b3c", 
            0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, 
            "c69e8d8f-4e5g-6g1h-0c3d-5h7g9f1e3c4d", 
            0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Slowly regenerate health
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(0.5F * (amplifier + 1));
        }
        
        // Reduce hunger drain slightly
        if (entity instanceof net.minecraft.entity.player.PlayerEntity) {
            net.minecraft.entity.player.PlayerEntity player = (net.minecraft.entity.player.PlayerEntity) entity;
            if (player.getFoodData().getFoodLevel() < 6) {
                player.getFoodData().setFoodLevel(6);
            }
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0; // Every 2 seconds
    }
}
