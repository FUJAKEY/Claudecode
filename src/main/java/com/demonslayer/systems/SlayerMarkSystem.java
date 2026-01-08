package com.demonslayer.systems;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

/**
 * Slayer Mark System - Activates at low HP for massive power boost
 */
public class SlayerMarkSystem {
    
    private static final String MARK_ACTIVE = "slayer_mark_active";
    private static final String MARK_COOLDOWN = "slayer_mark_cooldown";
    private static final String MARK_KILLS = "slayer_mark_kills";
    
    /**
     * Check if mark should activate (low HP trigger)
     */
    public static void checkMarkActivation(PlayerEntity player) {
        CompoundNBT data = player.getPersistentData();
        
        if (data.getBoolean(MARK_ACTIVE)) return; // Already active
        
        long cooldown = data.getLong(MARK_COOLDOWN);
        if (System.currentTimeMillis() < cooldown) return; // On cooldown
        
        // Activate at 25% HP or lower
        if (player.getHealth() <= player.getMaxHealth() * 0.25F) {
            activateMark(player);
        }
    }
    
    /**
     * Activate the Slayer Mark - massive power boost
     */
    public static void activateMark(PlayerEntity player) {
        CompoundNBT data = player.getPersistentData();
        data.putBoolean(MARK_ACTIVE, true);
        
        // Apply powerful buffs (30 seconds)
        player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 600, 2)); // +60% damage
        player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 600, 2)); // +60% speed
        player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 600, 1)); // -40% damage taken
        player.addEffect(new EffectInstance(Effects.REGENERATION, 600, 1)); // Regen
        
        // Visual feedback
        if (player.level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) player.level;
            sw.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1, player.getZ(), 100, 1, 2, 1, 0.5);
            sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1, player.getZ(), 50, 1, 2, 1, 0.2);
        }
        
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.5F, 1.0F);
        
        // Message
        player.displayClientMessage(
            new StringTextComponent("═══════════════════════")
                .withStyle(TextFormatting.RED), false);
        player.displayClientMessage(
            new StringTextComponent("  ⚔ SLAYER MARK AWAKENED ⚔")
                .withStyle(TextFormatting.RED).withStyle(TextFormatting.BOLD), false);
        player.displayClientMessage(
            new StringTextComponent("  痣が発現した！")
                .withStyle(TextFormatting.GOLD), false);
        player.displayClientMessage(
            new StringTextComponent("═══════════════════════")
                .withStyle(TextFormatting.RED), false);
        
        // Schedule deactivation
        // (This would normally be handled by a tick event)
    }
    
    /**
     * Deactivate the mark (called after duration)
     */
    public static void deactivateMark(PlayerEntity player) {
        CompoundNBT data = player.getPersistentData();
        data.putBoolean(MARK_ACTIVE, false);
        data.putLong(MARK_COOLDOWN, System.currentTimeMillis() + 300000); // 5 min cooldown
        
        player.displayClientMessage(
            new StringTextComponent("Slayer Mark faded...")
                .withStyle(TextFormatting.GRAY), true);
    }
    
    /**
     * Check if mark is currently active
     */
    public static boolean isMarkActive(PlayerEntity player) {
        return player.getPersistentData().getBoolean(MARK_ACTIVE);
    }
    
    /**
     * Get power multiplier from mark
     */
    public static float getMarkMultiplier(PlayerEntity player) {
        return isMarkActive(player) ? 2.0F : 1.0F;
    }
    
    /**
     * Track demon kills for mark progression
     */
    public static void addDemonKill(PlayerEntity player) {
        CompoundNBT data = player.getPersistentData();
        int kills = data.getInt(MARK_KILLS) + 1;
        data.putInt(MARK_KILLS, kills);
        
        // Milestone messages
        if (kills == 50) {
            player.displayClientMessage(
                new StringTextComponent("★ Slayer Mark is awakening... Kill more demons!")
                    .withStyle(TextFormatting.YELLOW), false);
        } else if (kills == 100) {
            player.displayClientMessage(
                new StringTextComponent("★★ Slayer Mark fully awakened! You can now activate it manually.")
                    .withStyle(TextFormatting.GOLD), false);
        }
    }
    
    public static int getDemonKills(PlayerEntity player) {
        return player.getPersistentData().getInt(MARK_KILLS);
    }
}
