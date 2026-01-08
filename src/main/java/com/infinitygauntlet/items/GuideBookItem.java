package com.infinitygauntlet.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Guide Book - In-game guide for the Infinity Gauntlet mod
 */
public class GuideBookItem extends Item {
    
    public GuideBookItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClientSide) {
            // Send guide information to chat
            player.sendMessage(new StringTextComponent(""), player.getUUID());
            player.sendMessage(new StringTextComponent("═══════════════════════════════")
                .withStyle(TextFormatting.GOLD), player.getUUID());
            player.sendMessage(new StringTextComponent("    ◆ INFINITY GAUNTLET GUIDE ◆")
                .withStyle(TextFormatting.LIGHT_PURPLE).withStyle(TextFormatting.BOLD), player.getUUID());
            player.sendMessage(new StringTextComponent("═══════════════════════════════")
                .withStyle(TextFormatting.GOLD), player.getUUID());
            
            player.sendMessage(new StringTextComponent(""), player.getUUID());
            player.sendMessage(new StringTextComponent("【CONTROLS】")
                .withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD), player.getUUID());
            player.sendMessage(new StringTextComponent("• R - Use ability")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• G - Switch stone")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• H - Switch sub-ability")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• V - Combo ability (2+ stones)")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• B - Toggle Infinity mode")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            
            player.sendMessage(new StringTextComponent(""), player.getUUID());
            player.sendMessage(new StringTextComponent("【STONES & ABILITIES】")
                .withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD), player.getUUID());
            
            player.sendMessage(new StringTextComponent("◆ Space Stone")
                .withStyle(TextFormatting.BLUE).append(
                    new StringTextComponent(" - Teleport / Portals / Warp Zone")
                        .withStyle(TextFormatting.GRAY)), player.getUUID());
            
            player.sendMessage(new StringTextComponent("◆ Time Stone")
                .withStyle(TextFormatting.GREEN).append(
                    new StringTextComponent(" - Time Freeze / INFINITY (Gojo)")
                        .withStyle(TextFormatting.GRAY)), player.getUUID());
            
            player.sendMessage(new StringTextComponent("◆ Reality Stone")
                .withStyle(TextFormatting.RED).append(
                    new StringTextComponent(" - Transform blocks / Mob transformation")
                        .withStyle(TextFormatting.GRAY)), player.getUUID());
            
            player.sendMessage(new StringTextComponent("◆ Power Stone")
                .withStyle(TextFormatting.DARK_PURPLE).append(
                    new StringTextComponent(" - Power Wave / Laser Beam / Destruction")
                        .withStyle(TextFormatting.GRAY)), player.getUUID());
            
            player.sendMessage(new StringTextComponent("◆ Mind Stone")
                .withStyle(TextFormatting.YELLOW).append(
                    new StringTextComponent(" - Mind Control / Telepathy")
                        .withStyle(TextFormatting.GRAY)), player.getUUID());
            
            player.sendMessage(new StringTextComponent("◆ Soul Stone")
                .withStyle(TextFormatting.GOLD).append(
                    new StringTextComponent(" - Life Steal / Resurrection / Summon Dead")
                        .withStyle(TextFormatting.GRAY)), player.getUUID());
            
            player.sendMessage(new StringTextComponent(""), player.getUUID());
            player.sendMessage(new StringTextComponent("【COMBOS】")
                .withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD), player.getUUID());
            player.sendMessage(new StringTextComponent("• Space+Power = Teleport Bomb")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• Time+Soul = Resurrection Wave")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• Reality+Mind = Mass Illusion")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• Power+Reality = World Breaker")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• Space+Time = Time Warp")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• Soul+Mind = Spirit Army")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            
            player.sendMessage(new StringTextComponent(""), player.getUUID());
            player.sendMessage(new StringTextComponent("【SNAP】")
                .withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD), player.getUUID());
            player.sendMessage(new StringTextComponent("With all 6 stones: Double RMB to SNAP!")
                .withStyle(TextFormatting.RED), player.getUUID());
            player.sendMessage(new StringTextComponent("Erases 50% of all entities nearby")
                .withStyle(TextFormatting.GRAY), player.getUUID());
            
            player.sendMessage(new StringTextComponent(""), player.getUUID());
            player.sendMessage(new StringTextComponent("【THANOS BOSS】")
                .withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD), player.getUUID());
            player.sendMessage(new StringTextComponent("• Spawns when you have 4+ stones")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• 500 HP, 4 attack patterns")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            player.sendMessage(new StringTextComponent("• Drops: Gauntlet Core, Vibranium, rare stone")
                .withStyle(TextFormatting.WHITE), player.getUUID());
            
            player.sendMessage(new StringTextComponent(""), player.getUUID());
            player.sendMessage(new StringTextComponent("═══════════════════════════════")
                .withStyle(TextFormatting.GOLD), player.getUUID());
        }
        
        return ActionResult.success(player.getItemInHand(hand));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Right-click to open guide")
            .withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("Learn about all abilities!")
            .withStyle(TextFormatting.AQUA));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
