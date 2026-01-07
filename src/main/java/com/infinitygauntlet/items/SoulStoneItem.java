package com.infinitygauntlet.items;

import com.infinitygauntlet.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class SoulStoneItem extends Item {
    
    public SoulStoneItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        
        if (otherStack.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
            if (InfinityGauntletItem.insertStone(otherStack, "soul_stone")) {
                if (!world.isClientSide) {
                    player.displayClientMessage(
                        new StringTextComponent("◆ Soul Stone inserted into gauntlet!")
                            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
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
                        new StringTextComponent("Soul Stone already in gauntlet!")
                            .withStyle(TextFormatting.RED), true);
                }
            }
            return ActionResult.pass(stack);
        }
        
        // Use stone alone
        if (!world.isClientSide) {
            // Soul stone doesn't hurt as much - it heals!
            player.heal(8.0F);
            player.addEffect(new EffectInstance(Effects.REGENERATION, 100, 1));
            player.addEffect(new EffectInstance(Effects.ABSORPTION, 200, 1));
            
            // But there's a cost - hunger
            player.getFoodData().addExhaustion(10.0F);
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.SOUL,
                    player.getX(), player.getY() + 1, player.getZ(), 50, 1, 1, 1, 0.1);
                ((ServerWorld) world).sendParticles(ParticleTypes.HEART,
                    player.getX(), player.getY() + 2, player.getZ(), 10, 0.5, 0.3, 0.5, 0);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SOUL_ESCAPE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            
            player.displayClientMessage(
                new StringTextComponent("❤ Healed! (Costs hunger)")
                    .withStyle(TextFormatting.GOLD), true);
        }
        
        player.getCooldowns().addCooldown(this, 60);
        return ActionResult.success(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("◆ Soul Stone").withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD));
        tooltip.add(new StringTextComponent("Power: Life Force").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("In Gauntlet: Heal + life steal").withStyle(TextFormatting.DARK_GRAY));
        tooltip.add(new StringTextComponent("Raw: Heal only (costs hunger)").withStyle(TextFormatting.RED));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Hold gauntlet + RMB to insert")
            .withStyle(TextFormatting.DARK_GRAY).withStyle(TextFormatting.ITALIC));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
