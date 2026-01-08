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

/**
 * Demon Art Book - Learn to use Blood Demon Arts
 */
public class DemonArtBookItem extends Item {
    
    public DemonArtBookItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!world.isClientSide) {
            // Apply demon power (with sunlight weakness)
            player.addEffect(new EffectInstance(ModEffects.DEMON_POWER.get(), 1200, 0));
            
            player.displayClientMessage(
                new StringTextComponent("血鬼術 Blood Demon Art activated!")
                    .withStyle(TextFormatting.DARK_RED).withStyle(TextFormatting.BOLD), true);
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.5F, 0.5F);
            
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        
        return ActionResult.success(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("血鬼術 Blood Demon Art")
            .withStyle(TextFormatting.DARK_RED));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Grants demon powers for 1 minute:")
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("  • +5 Attack Damage")
            .withStyle(TextFormatting.WHITE));
        tooltip.add(new StringTextComponent("  • +10 Max Health")
            .withStyle(TextFormatting.WHITE));
        tooltip.add(new StringTextComponent("  • Night Regeneration")
            .withStyle(TextFormatting.GREEN));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("⚠ Sunlight causes damage!")
            .withStyle(TextFormatting.RED));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
