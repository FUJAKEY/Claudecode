package com.demonslayer.items;

import com.demonslayer.breathing.BreathingForm;
import com.demonslayer.breathing.BreathingStyle;
import com.demonslayer.init.ModEffects;
import com.demonslayer.systems.SlayerRankSystem;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Dual Nichirin Swords - Like Inosuke's style
 */
public class DualNichirinItem extends SwordItem {
    
    public static final IItemTier DUAL_TIER = new IItemTier() {
        @Override public int getUses() { return 1800; }
        @Override public float getSpeed() { return 10.0F; }
        @Override public float getAttackDamageBonus() { return 6.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 18; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };
    
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;
    
    public DualNichirinItem(Properties properties) {
        super(DUAL_TIER, 2, -1.8F, properties);
        
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, 
            "Weapon modifier", 8.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, 
            "Weapon modifier", -1.8D, AttributeModifier.Operation.ADDITION)); // Faster attacks
        this.attributeModifiers = builder.build();
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundNBT nbt = stack.getOrCreateTag();
        
        boolean hasTotalConcentration = player.hasEffect(ModEffects.TOTAL_CONCENTRATION.get());
        
        if (player.isShiftKeyDown()) {
            int form = nbt.getInt("current_form");
            form = (form + 1) % 6;
            nbt.putInt("current_form", form);
            
            String[] formNames = {
                "Beast Breathing: First Fang - Pierce",
                "Beast Breathing: Second Fang - Slice",
                "Beast Breathing: Third Fang - Devour",
                "Beast Breathing: Fourth Fang - Crazy Cutting",
                "Beast Breathing: Fifth Fang - Crazy Slashing",
                "Beast Breathing: Sixth Fang - Palisade Bite"
            };
            
            if (!world.isClientSide) {
                player.displayClientMessage(
                    new StringTextComponent("◆ " + formNames[form])
                        .withStyle(TextFormatting.DARK_AQUA).withStyle(TextFormatting.BOLD), true);
            }
            return ActionResult.success(stack);
        }
        
        // Use form
        int form = nbt.getInt("current_form");
        long lastUse = nbt.getLong("last_use");
        int cooldown = hasTotalConcentration ? 20 : 40;
        
        if (world.getGameTime() - lastUse < cooldown) {
            return ActionResult.fail(stack);
        }
        
        if (!world.isClientSide) {
            float power = hasTotalConcentration ? 1.5F : 1.0F;
            power *= SlayerRankSystem.getRank(player).powerMultiplier;
            
            executeBeastForm(world, player, form, power);
            nbt.putLong("last_use", world.getGameTime());
            
            SlayerRankSystem.addXP(player, 2);
        }
        
        player.getCooldowns().addCooldown(this, 10);
        return ActionResult.success(stack);
    }
    
    private void executeBeastForm(World world, PlayerEntity player, int form, float power) {
        float[] damages = {12, 15, 18, 22, 26, 35};
        float[] ranges = {4, 5, 6, 5, 8, 4};
        
        float damage = damages[form] * power;
        float range = ranges[form] * power;
        
        net.minecraft.util.math.vector.Vector3d look = player.getLookAngle();
        net.minecraft.util.math.AxisAlignedBB area = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player), damage);
            
            // Multi-hit for some forms
            if (form >= 3) {
                for (int i = 0; i < 3; i++) {
                    target.hurt(DamageSource.playerAttack(player), damage / 4);
                }
            }
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (double d = 0; d < range; d += 0.3) {
                net.minecraft.util.math.vector.Vector3d pos = player.position().add(look.scale(d)).add(0, 1.5, 0);
                sw.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 2, 0.3, 0.3, 0.3, 0);
                sw.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 3, 0.2, 0.2, 0.2, 0.1);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.5F, 1.5F);
        
        String[] callouts = {"牙突！", "切り裂き！", "喰い噛み！", "乱れ斬り！", "狂い裂き！", "柵噛！"};
        player.displayClientMessage(new StringTextComponent("【Beast Breathing】 " + callouts[form])
            .withStyle(TextFormatting.DARK_AQUA).withStyle(TextFormatting.BOLD), true);
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Double swing
        if (attacker.level instanceof ServerWorld) {
            ((ServerWorld) attacker.level).sendParticles(ParticleTypes.SWEEP_ATTACK,
                target.getX(), target.getY() + 1, target.getZ(), 3, 0.5, 0.5, 0.5, 0);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("◆ Dual Nichirin Blades")
            .withStyle(TextFormatting.DARK_AQUA).withStyle(TextFormatting.BOLD));
        tooltip.add(new StringTextComponent("Style: Beast Breathing")
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Fast dual-wielding attacks")
            .withStyle(TextFormatting.WHITE));
        tooltip.add(new StringTextComponent("6 Beast Forms available")
            .withStyle(TextFormatting.WHITE));
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        return slot == EquipmentSlotType.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot, stack);
    }
}
