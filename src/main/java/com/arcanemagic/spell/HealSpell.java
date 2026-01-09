package com.arcanemagic.spell;

import com.arcanemagic.item.WandItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Heal spell - restores player health
 */
public class HealSpell extends Spell {

    @Override
    public int getManaCost() {
        return 30;
    }

    @Override
    public int getCooldown() {
        return 100; // 5 seconds
    }

    @Override
    public WandItem.WandTier getMinTier() {
        return WandItem.WandTier.ADEPT;
    }

    @Override
    public String getSpellId() {
        return "heal";
    }

    @Override
    public int getSpellColor() {
        return 0x00FF00; // Green
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();

        // Don't cast if already at full health
        if (currentHealth >= maxHealth) {
            return false;
        }

        // Heal 12 HP (6 hearts)
        float healAmount = 12.0f;
        player.heal(healAmount);

        // Play sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.5f);

        // Spawn healing particles
        if (world instanceof ServerWorld) {
            for (int i = 0; i < 30; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 2;
                double offsetY = world.random.nextDouble() * 2;
                double offsetZ = (world.random.nextDouble() - 0.5) * 2;

                ((ServerWorld) world).sendParticles(ParticleTypes.HEART,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        1, 0, 0, 0, 0);
            }

            // Green sparkles
            ((ServerWorld) world).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1, player.getZ(),
                    20, 0.5, 1.0, 0.5, 0.1);
        }

        return true;
    }
}
