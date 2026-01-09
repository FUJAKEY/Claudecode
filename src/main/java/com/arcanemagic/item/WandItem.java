package com.arcanemagic.item;

import com.arcanemagic.ArcaneMagicMod;
import com.arcanemagic.capability.ManaCapability;
import com.arcanemagic.event.ManaEvents;
import com.arcanemagic.network.ManaPacket;
import com.arcanemagic.network.NetworkHandler;
import com.arcanemagic.spell.Spell;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wand item - the primary tool for casting spells
 */
public class WandItem extends Item {

    private final WandTier tier;
    private static final String SPELLS_TAG = "BoundSpells";
    private static final String SELECTED_SPELL_TAG = "SelectedSpell";
    private static final String COOLDOWN_TAG = "SpellCooldowns";

    // Registry of all spells
    private static final Map<String, Spell> SPELL_REGISTRY = new HashMap<>();

    public WandItem(WandTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public static void registerSpell(Spell spell) {
        SPELL_REGISTRY.put(spell.getSpellId(), spell);
    }

    public static Spell getSpell(String id) {
        return SPELL_REGISTRY.get(id);
    }

    public WandTier getTier() {
        return tier;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Get selected spell
        String selectedSpellId = getSelectedSpellId(stack);
        if (selectedSpellId == null || selectedSpellId.isEmpty()) {
            if (!world.isClientSide) {
                player.displayClientMessage(
                        new TranslationTextComponent("message.arcanemagic.no_spell_bound")
                                .withStyle(TextFormatting.RED),
                        true);
            }
            return ActionResult.fail(stack);
        }

        Spell spell = SPELL_REGISTRY.get(selectedSpellId);
        if (spell == null) {
            return ActionResult.fail(stack);
        }

        // Check tier requirement
        if (!spell.canCast(player, tier)) {
            if (!world.isClientSide) {
                player.displayClientMessage(
                        new TranslationTextComponent("message.arcanemagic.tier_too_low")
                                .withStyle(TextFormatting.RED),
                        true);
            }
            return ActionResult.fail(stack);
        }

        // Check cooldown
        if (isOnCooldown(stack, selectedSpellId, world)) {
            if (!world.isClientSide) {
                player.displayClientMessage(
                        new TranslationTextComponent("message.arcanemagic.spell_cooldown")
                                .withStyle(TextFormatting.YELLOW),
                        true);
            }
            return ActionResult.fail(stack);
        }

        // Check mana
        int manaCost = spell.getManaCost();

        if (!world.isClientSide) {
            boolean[] hasMana = { false };

            player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.consumeMana(manaCost)) {
                    hasMana[0] = true;

                    // Sync mana to client
                    if (player instanceof ServerPlayerEntity) {
                        NetworkHandler.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                                new ManaPacket(mana.getMana(), mana.getMaxMana()));
                    }
                }
            });

            if (!hasMana[0]) {
                player.displayClientMessage(
                        new TranslationTextComponent("message.arcanemagic.not_enough_mana")
                                .withStyle(TextFormatting.RED),
                        true);
                return ActionResult.fail(stack);
            }

            // Cast the spell
            if (spell.cast(player, world)) {
                // Apply cooldown (reduced by tier)
                int cooldown = (int) (spell.getCooldown() * (1.0 - tier.getCooldownReduction()));
                setCooldown(stack, selectedSpellId, world.getGameTime() + cooldown);

                // Damage wand (except Archmage)
                if (tier != WandTier.ARCHMAGE) {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }

                // Play cast sound
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.5f, 1.5f);
            }
        }

        // Swing arm animation
        player.swing(hand);

        return ActionResult.success(stack);
    }

    /**
     * Bind a spell to this wand
     */
    public boolean bindSpell(ItemStack stack, Spell spell) {
        List<String> boundSpells = getBoundSpellIds(stack);

        // Check if already bound
        if (boundSpells.contains(spell.getSpellId())) {
            return false;
        }

        // Check slot limit
        if (boundSpells.size() >= tier.getSpellSlots()) {
            return false;
        }

        // Check tier requirement
        if (spell.getMinTier().ordinal() > tier.ordinal()) {
            return false;
        }

        boundSpells.add(spell.getSpellId());
        setBoundSpellIds(stack, boundSpells);

        // Auto-select if first spell
        if (boundSpells.size() == 1) {
            setSelectedSpellId(stack, spell.getSpellId());
        }

        return true;
    }

    public List<String> getBoundSpellIds(ItemStack stack) {
        List<String> spells = new ArrayList<>();
        CompoundNBT tag = stack.getOrCreateTag();

        if (tag.contains(SPELLS_TAG)) {
            ListNBT list = tag.getList(SPELLS_TAG, 8); // 8 = String
            for (int i = 0; i < list.size(); i++) {
                spells.add(list.getString(i));
            }
        }

        return spells;
    }

    private void setBoundSpellIds(ItemStack stack, List<String> spells) {
        CompoundNBT tag = stack.getOrCreateTag();
        ListNBT list = new ListNBT();
        for (String spell : spells) {
            list.add(StringNBT.valueOf(spell));
        }
        tag.put(SPELLS_TAG, list);
    }

    public String getSelectedSpellId(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        return tag.getString(SELECTED_SPELL_TAG);
    }

    public void setSelectedSpellId(ItemStack stack, String spellId) {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(SELECTED_SPELL_TAG, spellId);
    }

    public void cycleSpell(ItemStack stack, boolean forward) {
        List<String> spells = getBoundSpellIds(stack);
        if (spells.isEmpty())
            return;

        String current = getSelectedSpellId(stack);
        int index = spells.indexOf(current);

        if (forward) {
            index = (index + 1) % spells.size();
        } else {
            index = (index - 1 + spells.size()) % spells.size();
        }

        setSelectedSpellId(stack, spells.get(index));
    }

    private boolean isOnCooldown(ItemStack stack, String spellId, World world) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(COOLDOWN_TAG))
            return false;

        CompoundNBT cooldowns = tag.getCompound(COOLDOWN_TAG);
        long endTime = cooldowns.getLong(spellId);
        return world.getGameTime() < endTime;
    }

    private void setCooldown(ItemStack stack, String spellId, long endTime) {
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT cooldowns = tag.getCompound(COOLDOWN_TAG);
        cooldowns.putLong(spellId, endTime);
        tag.put(COOLDOWN_TAG, cooldowns);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip,
            ITooltipFlag flag) {
        // Tier info
        tooltip.add(new TranslationTextComponent("tooltip.arcanemagic.wand_tier", tier.getDisplayName())
                .withStyle(tier.getColor()));

        // Spell slots
        List<String> spells = getBoundSpellIds(stack);
        tooltip.add(new StringTextComponent("Spells: " + spells.size() + "/" + tier.getSpellSlots())
                .withStyle(TextFormatting.GRAY));

        // Bound spells
        if (!spells.isEmpty()) {
            tooltip.add(new StringTextComponent(""));
            String selected = getSelectedSpellId(stack);

            for (String spellId : spells) {
                Spell spell = SPELL_REGISTRY.get(spellId);
                if (spell != null) {
                    ITextComponent name = spell.getDisplayName();
                    if (spellId.equals(selected)) {
                        tooltip.add(new StringTextComponent("► ")
                                .withStyle(TextFormatting.AQUA)
                                .append(name.copy().withStyle(TextFormatting.AQUA)));
                    } else {
                        tooltip.add(new StringTextComponent("  ")
                                .append(name.copy().withStyle(TextFormatting.GRAY)));
                    }
                }
            }
        }

        // Cooldown reduction
        if (tier.getCooldownReduction() > 0) {
            tooltip.add(new StringTextComponent(""));
            tooltip.add(new StringTextComponent("-" + (int) (tier.getCooldownReduction() * 100) + "% Cooldown")
                    .withStyle(TextFormatting.GREEN));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return tier == WandTier.ARCHMAGE || !getBoundSpellIds(stack).isEmpty();
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return tier.getRarity();
    }

    /**
     * Wand tiers with different capabilities
     */
    public enum WandTier {
        APPRENTICE(1, 20, 0.0f, TextFormatting.WHITE, Rarity.COMMON, "Apprentice"),
        ADEPT(2, 40, 0.15f, TextFormatting.GREEN, Rarity.UNCOMMON, "Adept"),
        MASTER(3, 60, 0.30f, TextFormatting.BLUE, Rarity.RARE, "Master"),
        ARCHMAGE(4, 100, 0.50f, TextFormatting.LIGHT_PURPLE, Rarity.EPIC, "Archmage");

        private final int spellSlots;
        private final int maxManaCost;
        private final float cooldownReduction;
        private final TextFormatting color;
        private final Rarity rarity;
        private final String displayName;

        WandTier(int spellSlots, int maxManaCost, float cooldownReduction,
                TextFormatting color, Rarity rarity, String displayName) {
            this.spellSlots = spellSlots;
            this.maxManaCost = maxManaCost;
            this.cooldownReduction = cooldownReduction;
            this.color = color;
            this.rarity = rarity;
            this.displayName = displayName;
        }

        public int getSpellSlots() {
            return spellSlots;
        }

        public int getMaxManaCost() {
            return maxManaCost;
        }

        public float getCooldownReduction() {
            return cooldownReduction;
        }

        public TextFormatting getColor() {
            return color;
        }

        public Rarity getRarity() {
            return rarity;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
