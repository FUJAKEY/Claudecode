package com.demonslayer.items;

import com.demonslayer.init.ModEffects;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BreathingScrollItem extends Item {
    
    public BreathingScrollItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!world.isClientSide) {
            // Apply Total Concentration
            player.addEffect(new EffectInstance(ModEffects.TOTAL_CONCENTRATION.get(), 600, 0));
            
            player.displayClientMessage(
                new StringTextComponent("全集中！ Total Concentration!")
                    .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_BREATH, SoundCategory.PLAYERS, 1.5F, 0.8F);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.5F, 2.0F);
            
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        
        return ActionResult.success(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("全集中 Total Concentration Scroll")
            .withStyle(TextFormatting.AQUA));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Use to activate Total Concentration")
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("for 30 seconds:")
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("  • +2 Attack Damage")
            .withStyle(TextFormatting.WHITE));
        tooltip.add(new StringTextComponent("  • +15% Movement Speed")
            .withStyle(TextFormatting.WHITE));
        tooltip.add(new StringTextComponent("  • +20% Attack Speed")
            .withStyle(TextFormatting.WHITE));
        tooltip.add(new StringTextComponent("  • Slow Regeneration")
            .withStyle(TextFormatting.WHITE));
        tooltip.add(new StringTextComponent("  • Halved ability cooldowns")
            .withStyle(TextFormatting.GREEN));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
