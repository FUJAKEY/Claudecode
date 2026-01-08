package com.demonslayer.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class WisteriaItem extends Item {
    
    public WisteriaItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!world.isClientSide) {
            // Wisteria repels demons
            AxisAlignedBB area = player.getBoundingBox().inflate(10);
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.getType().getDescriptionId().contains("demon"));
            
            for (LivingEntity entity : entities) {
                // Damage and poison demons
                entity.hurt(DamageSource.MAGIC, 5.0F);
                entity.addEffect(new EffectInstance(Effects.POISON, 100, 1));
                entity.addEffect(new EffectInstance(Effects.WEAKNESS, 100, 1));
                entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, 1));
            }
            
            // Also protects from undead
            List<LivingEntity> undead = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isInvertedHealAndHarm());
            
            for (LivingEntity entity : undead) {
                entity.hurt(DamageSource.MAGIC, 3.0F);
                entity.addEffect(new EffectInstance(Effects.WEAKNESS, 60, 0));
            }
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1, player.getZ(), 100, 5, 3, 5, 0.1);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.5F);
            
            player.displayClientMessage(
                new StringTextComponent("藤の花 Wisteria protection activated!")
                    .withStyle(TextFormatting.LIGHT_PURPLE), true);
            
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        
        return ActionResult.success(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("藤の花 Wisteria Flower")
            .withStyle(TextFormatting.LIGHT_PURPLE));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Repels and weakens demons")
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("Also effective against undead")
            .withStyle(TextFormatting.DARK_GRAY));
    }
}
