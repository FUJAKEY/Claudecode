package com.demonslayer.items;

import com.demonslayer.breathing.BreathingStyle;
import com.demonslayer.breathing.BreathingForm;
import com.demonslayer.init.ModEffects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class NichirinSwordItem extends SwordItem {
    
    public enum NichirinColor {
        BLACK("Black", TextFormatting.DARK_GRAY, BreathingStyle.SUN),
        BLUE("Blue", TextFormatting.BLUE, BreathingStyle.WATER),
        RED("Red", TextFormatting.RED, BreathingStyle.FLAME),
        YELLOW("Yellow", TextFormatting.YELLOW, BreathingStyle.THUNDER),
        GREEN("Green", TextFormatting.GREEN, BreathingStyle.WIND),
        PINK("Pink", TextFormatting.LIGHT_PURPLE, BreathingStyle.LOVE),
        WHITE("White", TextFormatting.WHITE, BreathingStyle.MIST);
        
        public final String name;
        public final TextFormatting color;
        public final BreathingStyle style;
        
        NichirinColor(String name, TextFormatting color, BreathingStyle style) {
            this.name = name;
            this.color = color;
            this.style = style;
        }
    }
    
    public static final IItemTier NICHIRIN_TIER = new IItemTier() {
        @Override public int getUses() { return 2000; }
        @Override public float getSpeed() { return 9.0F; }
        @Override public float getAttackDamageBonus() { return 7.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 18; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };
    
    private final NichirinColor nichirinColor;
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;
    
    public NichirinSwordItem(NichirinColor color, Properties properties) {
        super(NICHIRIN_TIER, 3, -2.2F, properties);
        this.nichirinColor = color;
        
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, 
            "Weapon modifier", 10.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, 
            "Weapon modifier", -2.2D, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }
    
    public NichirinColor getNichirinColor() {
        return nichirinColor;
    }
    
    public BreathingStyle getBreathingStyle() {
        return nichirinColor.style;
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundNBT nbt = stack.getOrCreateTag();
        
        // Check if player has Total Concentration active
        boolean hasTotalConcentration = player.hasEffect(ModEffects.TOTAL_CONCENTRATION.get());
        
        if (player.isShiftKeyDown()) {
            // Switch breathing form
            int currentForm = nbt.getInt("current_form");
            BreathingForm[] forms = nichirinColor.style.getForms();
            currentForm = (currentForm + 1) % forms.length;
            nbt.putInt("current_form", currentForm);
            
            if (!world.isClientSide) {
                player.displayClientMessage(
                    new StringTextComponent("◆ " + forms[currentForm].getName())
                        .withStyle(nichirinColor.color).withStyle(TextFormatting.BOLD), true);
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.5F, 1.5F);
            }
            return ActionResult.success(stack);
        }
        
        // Use breathing form
        int currentForm = nbt.getInt("current_form");
        BreathingForm[] forms = nichirinColor.style.getForms();
        BreathingForm form = forms[currentForm];
        
        // Check cooldown
        long lastUse = nbt.getLong("last_form_use");
        long currentTime = world.getGameTime();
        int cooldown = hasTotalConcentration ? form.getCooldown() / 2 : form.getCooldown();
        
        if (currentTime - lastUse < cooldown) {
            if (!world.isClientSide) {
                int remaining = (int)((cooldown - (currentTime - lastUse)) / 20) + 1;
                player.displayClientMessage(
                    new StringTextComponent("⏱ " + remaining + "s")
                        .withStyle(TextFormatting.GRAY), true);
            }
            return ActionResult.fail(stack);
        }
        
        if (!world.isClientSide) {
            // Execute breathing form
            float powerMultiplier = hasTotalConcentration ? 1.5F : 1.0F;
            form.execute(world, player, powerMultiplier, nichirinColor.color);
            nbt.putLong("last_form_use", currentTime);
            
            // Add XP
            int xp = nbt.getInt("breathing_xp") + 1;
            nbt.putInt("breathing_xp", xp);
            
            // Use stamina (hunger)
            if (!player.isCreative()) {
                player.getFoodData().addExhaustion(hasTotalConcentration ? 1.0F : 2.0F);
            }
        }
        
        player.getCooldowns().addCooldown(this, 10);
        return ActionResult.success(stack);
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Extra damage to demons
        if (target.getType().getDescriptionId().contains("demon")) {
            target.hurt(DamageSource.playerAttack((PlayerEntity) attacker), 5.0F);
        }
        
        // Sunlight damage effect on black sword
        if (nichirinColor == NichirinColor.BLACK && attacker.level.isDay()) {
            target.setSecondsOnFire(3);
        }
        
        // Particles
        if (attacker.level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) attacker.level;
            switch (nichirinColor) {
                case BLUE:
                    sw.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
                    break;
                case RED:
                    sw.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
                    break;
                case YELLOW:
                    sw.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.3, 0.3, 0.5);
                    break;
                case GREEN:
                    sw.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
                    break;
                default:
                    sw.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
            }
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("◆ " + nichirinColor.name + " Nichirin Blade")
            .withStyle(nichirinColor.color).withStyle(TextFormatting.BOLD));
        tooltip.add(new StringTextComponent("Style: " + nichirinColor.style.getName())
            .withStyle(TextFormatting.GRAY));
        
        CompoundNBT nbt = stack.getOrCreateTag();
        int currentForm = nbt.getInt("current_form");
        BreathingForm[] forms = nichirinColor.style.getForms();
        
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("【Forms】").withStyle(TextFormatting.YELLOW));
        for (int i = 0; i < forms.length; i++) {
            String prefix = i == currentForm ? "▶ " : "  ";
            TextFormatting color = i == currentForm ? nichirinColor.color : TextFormatting.DARK_GRAY;
            tooltip.add(new StringTextComponent(prefix + forms[i].getName()).withStyle(color));
        }
        
        int xp = nbt.getInt("breathing_xp");
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("XP: " + xp).withStyle(TextFormatting.GREEN));
        
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Shift+RMB: Switch form").withStyle(TextFormatting.DARK_GRAY));
        tooltip.add(new StringTextComponent("RMB: Use form").withStyle(TextFormatting.DARK_GRAY));
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        return slot == EquipmentSlotType.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot, stack);
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getOrCreateTag().getInt("breathing_xp") > 50;
    }
}
