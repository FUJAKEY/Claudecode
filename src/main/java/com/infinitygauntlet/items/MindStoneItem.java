package com.infinitygauntlet.items;

import com.infinitygauntlet.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.MobEntity;
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

public class MindStoneItem extends Item {
    
    public MindStoneItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        
        if (otherStack.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
            if (InfinityGauntletItem.insertStone(otherStack, "mind_stone")) {
                if (!world.isClientSide) {
                    player.displayClientMessage(
                        new StringTextComponent("◆ Mind Stone inserted into gauntlet!")
                            .withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD), true);
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
                        new StringTextComponent("Mind Stone already in gauntlet!")
                            .withStyle(TextFormatting.RED), true);
                }
            }
            return ActionResult.pass(stack);
        }
        
        // Use stone alone
        if (!world.isClientSide) {
            player.hurt(DamageSource.MAGIC, 2.0F);
            
            // Confuse nearby mobs
            AxisAlignedBB area = player.getBoundingBox().inflate(10);
            List<MobEntity> entities = world.getEntitiesOfClass(MobEntity.class, area);
            
            for (MobEntity mob : entities) {
                mob.setTarget(null);
                mob.addEffect(new EffectInstance(Effects.CONFUSION, 100, 0));
                mob.addEffect(new EffectInstance(Effects.GLOWING, 100, 0));
            }
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 2, player.getZ(), 100, 5, 2, 5, 0.5);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0F, 1.5F);
            
            player.displayClientMessage(
                new StringTextComponent("◉ " + entities.size() + " minds confused!")
                    .withStyle(TextFormatting.YELLOW), true);
        }
        
        player.getCooldowns().addCooldown(this, 100);
        return ActionResult.success(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("◆ Mind Stone").withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD));
        tooltip.add(new StringTextComponent("Power: Mind Control").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("In Gauntlet: Full mob control").withStyle(TextFormatting.DARK_GRAY));
        tooltip.add(new StringTextComponent("Raw: Confuse mobs (hurts)").withStyle(TextFormatting.RED));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Hold gauntlet + RMB to insert")
            .withStyle(TextFormatting.DARK_GRAY).withStyle(TextFormatting.ITALIC));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
