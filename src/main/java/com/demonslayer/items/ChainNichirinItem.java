package com.demonslayer.items;

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
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Chain Nichirin - Tengen Uzui style weapon with range attacks
 */
public class ChainNichirinItem extends SwordItem {
    
    public static final IItemTier CHAIN_TIER = new IItemTier() {
        @Override public int getUses() { return 2000; }
        @Override public float getSpeed() { return 9.0F; }
        @Override public float getAttackDamageBonus() { return 7.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 20; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };
    
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;
    
    public ChainNichirinItem(Properties properties) {
        super(CHAIN_TIER, 3, -2.2F, properties);
        
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, 
            "Weapon modifier", 10.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, 
            "Weapon modifier", -2.2D, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundNBT nbt = stack.getOrCreateTag();
        
        boolean hasTotalConcentration = player.hasEffect(ModEffects.TOTAL_CONCENTRATION.get());
        
        if (player.isShiftKeyDown()) {
            int form = nbt.getInt("current_form");
            form = (form + 1) % 5;
            nbt.putInt("current_form", form);
            
            String[] formNames = {
                "Sound Breathing: First Form - Roar",
                "Sound Breathing: Second Form - String Performance",
                "Sound Breathing: Third Form - Score",
                "Sound Breathing: Fourth Form - Constant Resounding Slashes",
                "Sound Breathing: Fifth Form - Flashy God"
            };
            
            if (!world.isClientSide) {
                player.displayClientMessage(
                    new StringTextComponent("♪ " + formNames[form])
                        .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
            }
            return ActionResult.success(stack);
        }
        
        // Use form
        int form = nbt.getInt("current_form");
        long lastUse = nbt.getLong("last_use");
        int cooldown = hasTotalConcentration ? 25 : 50;
        
        if (world.getGameTime() - lastUse < cooldown) {
            return ActionResult.fail(stack);
        }
        
        if (!world.isClientSide) {
            float power = hasTotalConcentration ? 1.5F : 1.0F;
            power *= SlayerRankSystem.getRank(player).powerMultiplier;
            
            executeSoundForm(world, player, form, power);
            nbt.putLong("last_use", world.getGameTime());
            
            SlayerRankSystem.addXP(player, 3);
        }
        
        player.getCooldowns().addCooldown(this, 15);
        return ActionResult.success(stack);
    }
    
    private void executeSoundForm(World world, PlayerEntity player, int form, float power) {
        Vector3d look = player.getLookAngle();
        
        switch (form) {
            case 0: roar(world, player, power); break;
            case 1: stringPerformance(world, player, look, power); break;
            case 2: score(world, player, power); break;
            case 3: constantSlashes(world, player, look, power); break;
            case 4: flashyGod(world, player, power); break;
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 2.0F, 1.5F);
    }
    
    // First Form - Roar
    private void roar(World world, PlayerEntity player, float power) {
        AxisAlignedBB area = player.getBoundingBox().inflate(8);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player).setMagic(), 20.0F * power);
            Vector3d knockback = target.position().subtract(player.position()).normalize().scale(2);
            target.setDeltaMovement(knockback.x, 0.5, knockback.z);
            target.hurtMarked = true;
        }
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                player.getX(), player.getY() + 1, player.getZ(), 5, 2, 1, 2, 0);
        }
        
        player.displayClientMessage(new StringTextComponent("【音の呼吸】壱ノ型 轟！")
            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
    }
    
    // Second Form - String Performance
    private void stringPerformance(World world, PlayerEntity player, Vector3d look, float power) {
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            // Chain whip attack
            for (double d = 0; d < 12; d += 0.3) {
                Vector3d pos = player.position().add(look.scale(d)).add(0, 1.5, 0);
                // Spiral pattern
                double angle = d * 0.5;
                double offsetX = Math.cos(angle) * 0.5;
                double offsetZ = Math.sin(angle) * 0.5;
                sw.sendParticles(ParticleTypes.END_ROD, pos.x + offsetX, pos.y, pos.z + offsetZ, 2, 0.1, 0.1, 0.1, 0.02);
                
                AxisAlignedBB hitBox = new AxisAlignedBB(pos.x - 1, pos.y - 1, pos.z - 1, pos.x + 1, pos.y + 1, pos.z + 1);
                for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player)) {
                    target.hurt(DamageSource.playerAttack(player), 18.0F * power);
                }
            }
        }
        
        player.displayClientMessage(new StringTextComponent("【音の呼吸】弐ノ型 弦奏！")
            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
    }
    
    // Third Form - Score
    private void score(World world, PlayerEntity player, float power) {
        // Multi-directional attack
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 / 8) * i;
            Vector3d dir = new Vector3d(Math.cos(angle), 0, Math.sin(angle));
            
            if (world instanceof ServerWorld) {
                ServerWorld sw = (ServerWorld) world;
                for (double d = 0; d < 8; d += 0.5) {
                    Vector3d pos = player.position().add(dir.scale(d)).add(0, 1, 0);
                    sw.sendParticles(ParticleTypes.NOTE, pos.x, pos.y, pos.z, 2, 0.1, 0.1, 0.1, 0.5);
                }
            }
        }
        
        AxisAlignedBB area = player.getBoundingBox().inflate(8);
        for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player)) {
            target.hurt(DamageSource.playerAttack(player), 22.0F * power);
        }
        
        player.displayClientMessage(new StringTextComponent("【音の呼吸】参ノ型 響斬無間！")
            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
    }
    
    // Fourth Form - Constant Resounding Slashes
    private void constantSlashes(World world, PlayerEntity player, Vector3d look, float power) {
        AxisAlignedBB area = player.getBoundingBox().expandTowards(look.scale(10)).inflate(2);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            // Rapid hits
            for (int i = 0; i < 8; i++) {
                target.hurt(DamageSource.playerAttack(player), 5.0F * power);
            }
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (double d = 0; d < 10; d += 0.3) {
                Vector3d pos = player.position().add(look.scale(d)).add(0, 1.5, 0);
                sw.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 3, 0.3, 0.3, 0.3, 0);
            }
        }
        
        player.displayClientMessage(new StringTextComponent("【音の呼吸】肆ノ型 響斬連打！")
            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
    }
    
    // Fifth Form - Flashy God
    private void flashyGod(World world, PlayerEntity player, float power) {
        // Ultimate attack - teleport and massive damage
        LivingEntity closest = null;
        double closestDist = 30;
        
        for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(30), e -> e != player)) {
            double dist = player.distanceToSqr(entity);
            if (dist < closestDist * closestDist) {
                closestDist = Math.sqrt(dist);
                closest = entity;
            }
        }
        
        if (closest != null) {
            // Teleport behind
            Vector3d behind = closest.position().subtract(closest.getLookAngle().scale(2));
            player.teleportTo(behind.x, closest.getY(), behind.z);
            
            // Massive damage with explosions
            closest.hurt(DamageSource.playerAttack(player).bypassArmor(), 50.0F * power);
            
            if (world instanceof ServerWorld) {
                ServerWorld sw = (ServerWorld) world;
                sw.sendParticles(ParticleTypes.EXPLOSION, closest.getX(), closest.getY() + 1, closest.getZ(), 5, 1, 1, 1, 0);
                sw.sendParticles(ParticleTypes.END_ROD, closest.getX(), closest.getY() + 1, closest.getZ(), 100, 2, 2, 2, 0.5);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.5F, 1.5F);
        
        player.displayClientMessage(new StringTextComponent("【音の呼吸】伍ノ型 派手な神！")
            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("♪ Chain Nichirin Blades")
            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD));
        tooltip.add(new StringTextComponent("Style: Sound Breathing")
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("Master: Tengen Uzui")
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Long range chain attacks")
            .withStyle(TextFormatting.WHITE));
        tooltip.add(new StringTextComponent("5 Sound Forms available")
            .withStyle(TextFormatting.WHITE));
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        return slot == EquipmentSlotType.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot, stack);
    }
}
