package com.arcanemagic.item;

import com.arcanemagic.spell.Spell;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Spell Tome - consumable item that teaches spells to wands
 */
public class SpellTomeItem extends Item {

    private final Spell spell;

    public SpellTomeItem(Spell spell, Properties properties) {
        super(properties);
        this.spell = spell;

        // Register spell
        WandItem.registerSpell(spell);
    }

    public Spell getSpell() {
        return spell;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack tome = player.getItemInHand(hand);
        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        // Check if holding a wand in other hand
        if (!(otherStack.getItem() instanceof WandItem)) {
            if (!world.isClientSide) {
                player.displayClientMessage(
                        new TranslationTextComponent("message.arcanemagic.need_wand_in_hand")
                                .withStyle(TextFormatting.RED),
                        true);
            }
            return ActionResult.fail(tome);
        }

        WandItem wand = (WandItem) otherStack.getItem();

        // Try to bind the spell
        if (!world.isClientSide) {
            if (wand.bindSpell(otherStack, spell)) {
                // Success!
                player.displayClientMessage(
                        new TranslationTextComponent("message.arcanemagic.spell_learned", spell.getDisplayName())
                                .withStyle(TextFormatting.GREEN),
                        true);

                // Play sound
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);

                // Consume tome
                if (!player.abilities.instabuild) {
                    tome.shrink(1);
                }

                return ActionResult.success(tome);
            } else {
                // Failed - check why
                if (wand.getBoundSpellIds(otherStack).contains(spell.getSpellId())) {
                    player.displayClientMessage(
                            new TranslationTextComponent("message.arcanemagic.spell_already_bound")
                                    .withStyle(TextFormatting.YELLOW),
                            true);
                } else if (wand.getBoundSpellIds(otherStack).size() >= wand.getTier().getSpellSlots()) {
                    player.displayClientMessage(
                            new TranslationTextComponent("message.arcanemagic.wand_full")
                                    .withStyle(TextFormatting.RED),
                            true);
                } else if (spell.getMinTier().ordinal() > wand.getTier().ordinal()) {
                    player.displayClientMessage(
                            new TranslationTextComponent("message.arcanemagic.wand_tier_low")
                                    .withStyle(TextFormatting.RED),
                            true);
                }

                return ActionResult.fail(tome);
            }
        }

        return ActionResult.success(tome);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip,
            ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("Contains: ")
                .withStyle(TextFormatting.GRAY)
                .append(spell.getDisplayName().copy().withStyle(TextFormatting.AQUA)));

        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Mana Cost: " + spell.getManaCost())
                .withStyle(TextFormatting.BLUE));
        tooltip.add(new StringTextComponent("Cooldown: " + (spell.getCooldown() / 20.0f) + "s")
                .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("Min Tier: " + spell.getMinTier().getDisplayName())
                .withStyle(spell.getMinTier().getColor()));

        tooltip.add(new StringTextComponent(""));
        tooltip.add(spell.getDescription().copy().withStyle(TextFormatting.DARK_GRAY));

        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Hold wand in other hand and right-click")
                .withStyle(TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
