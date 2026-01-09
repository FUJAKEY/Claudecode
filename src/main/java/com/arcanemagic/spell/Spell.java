package com.arcanemagic.spell;

import com.arcanemagic.item.WandItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Base class for all spells in the mod
 */
public abstract class Spell {

    /**
     * Get the mana cost to cast this spell
     */
    public abstract int getManaCost();

    /**
     * Get the cooldown in ticks between casts
     */
    public abstract int getCooldown();

    /**
     * Get the minimum wand tier required to cast this spell
     */
    public abstract WandItem.WandTier getMinTier();

    /**
     * Get the spell's registry name
     */
    public abstract String getSpellId();

    /**
     * Execute the spell's effect
     * 
     * @param player The player casting the spell
     * @param world  The world
     * @return true if the spell was cast successfully
     */
    public abstract boolean cast(PlayerEntity player, World world);

    /**
     * Get the spell's display name
     */
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("spell.arcanemagic." + getSpellId());
    }

    /**
     * Get the spell's description
     */
    public ITextComponent getDescription() {
        return new TranslationTextComponent("spell.arcanemagic." + getSpellId() + ".desc");
    }

    /**
     * Get the spell's particle color (RGB packed int)
     */
    public int getSpellColor() {
        return 0xFFFFFF; // White by default
    }

    /**
     * Check if the player can cast this spell
     */
    public boolean canCast(PlayerEntity player, WandItem.WandTier wandTier) {
        return wandTier.ordinal() >= getMinTier().ordinal();
    }
}
