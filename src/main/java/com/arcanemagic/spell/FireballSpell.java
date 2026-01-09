package com.arcanemagic.spell;

import com.arcanemagic.item.WandItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Fireball spell - launches an explosive fire projectile
 */
public class FireballSpell extends Spell {

    @Override
    public int getManaCost() {
        return 15;
    }

    @Override
    public int getCooldown() {
        return 40; // 2 seconds
    }

    @Override
    public WandItem.WandTier getMinTier() {
        return WandItem.WandTier.APPRENTICE;
    }

    @Override
    public String getSpellId() {
        return "fireball";
    }

    @Override
    public int getSpellColor() {
        return 0xFF4500; // Orange-red
    }

    @Override
    public boolean cast(PlayerEntity player, World world) {
        if (world.isClientSide)
            return true;

        Vector3d look = player.getLookAngle();
        double speed = 1.5;

        SmallFireballEntity fireball = new SmallFireballEntity(
                world,
                player.getX() + look.x,
                player.getEyeY() - 0.1,
                player.getZ() + look.z,
                look.x * speed,
                look.y * speed,
                look.z * speed);

        fireball.setOwner(player);
        world.addFreshEntity(fireball);

        // Play sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);

        return true;
    }
}
