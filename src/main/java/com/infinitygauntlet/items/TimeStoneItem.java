package com.infinitygauntlet.items;

import com.infinitygauntlet.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class TimeStoneItem extends Item {
    
    public TimeStoneItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        
        if (otherStack.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
            if (InfinityGauntletItem.insertStone(otherStack, "time_stone")) {
                if (!world.isClientSide) {
                    player.displayClientMessage(
                        new StringTextComponent("◆ Time Stone inserted into gauntlet!")
                            .withStyle(TextFormatting.GREEN).withStyle(TextFormatting.BOLD), true);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.5F);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.5F, 2.0F);
                }
                stack.shrink(1);
                return ActionResult.success(stack);
            } else {
                if (!world.isClientSide) {
                    player.displayClientMessage(
                        new StringTextComponent("Time Stone already in gauntlet!")
                            .withStyle(TextFormatting.RED), true);
                }
            }
            return ActionResult.pass(stack);
        }
        
        // Use stone alone (weaker & hurts)
        if (!world.isClientSide) {
            player.hurt(DamageSource.MAGIC, 2.0F);
            
            // Slow nearby mobs (smaller radius)
            AxisAlignedBB area = player.getBoundingBox().inflate(8);
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player);
            
            for (LivingEntity entity : entities) {
                entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, 4));
            }
            
            // Small speed boost for player
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 100, 1));
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1, player.getZ(), 50, 4, 2, 4, 0.02);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0F, 2.0F);
            
            player.displayClientMessage(
                new StringTextComponent("⏱ Short time slow! " + entities.size() + " affected")
                    .withStyle(TextFormatting.GREEN), true);
        }
        
        player.getCooldowns().addCooldown(this, 100);
        return ActionResult.success(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("◆ Time Stone").withStyle(TextFormatting.GREEN).withStyle(TextFormatting.BOLD));
        tooltip.add(new StringTextComponent("Power: Time Manipulation").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("In Gauntlet: Freeze enemies 10s").withStyle(TextFormatting.DARK_GRAY));
        tooltip.add(new StringTextComponent("Raw: Slow enemies 5s (hurts)").withStyle(TextFormatting.RED));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Hold gauntlet + RMB to insert")
            .withStyle(TextFormatting.DARK_GRAY).withStyle(TextFormatting.ITALIC));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
