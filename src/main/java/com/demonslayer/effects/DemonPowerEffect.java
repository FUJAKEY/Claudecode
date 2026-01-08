package com.demonslayer.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class DemonPowerEffect extends Effect {
    
    public DemonPowerEffect() {
        super(EffectType.HARMFUL, 0x8B0000); // Dark red
        
        // Demon strength
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, 
            "d70f9e0g-5f6h-7h2i-1d4e-6i8h0g2f4d5e", 
            5.0D, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.MAX_HEALTH, 
            "e81g0f1h-6g7i-8i3j-2e5f-7j9i1h3g5e6f", 
            10.0D, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Demons take sunlight damage
        if (entity.level.isDay() && entity.level.canSeeSky(entity.blockPosition())) {
            entity.setSecondsOnFire(2);
            entity.hurt(net.minecraft.util.DamageSource.ON_FIRE, 2.0F * (amplifier + 1));
        } else {
            // Regenerate at night
            if (entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(1.0F * (amplifier + 1));
            }
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0; // Every second
    }
}
