package com.infinitygauntlet.items;

import com.infinitygauntlet.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class SpaceStoneItem extends Item {
    
    public SpaceStoneItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Find gauntlet in other hand
        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        
        if (otherStack.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
            if (InfinityGauntletItem.insertStone(otherStack, "space_stone")) {
                if (!world.isClientSide) {
                    player.displayClientMessage(
                        new StringTextComponent("◆ Space Stone inserted into gauntlet!")
                            .withStyle(TextFormatting.BLUE).withStyle(TextFormatting.BOLD), true);
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
                        new StringTextComponent("Space Stone already in gauntlet!")
                            .withStyle(TextFormatting.RED), true);
                }
            }
            return ActionResult.pass(stack);
        }
        
        // Use stone alone (weaker version)
        if (!world.isClientSide) {
            // Raw stone has a cost - damages player
            player.hurt(DamageSource.MAGIC, 2.0F);
            
            // Short teleport (only 20 blocks)
            Vector3d lookVec = player.getLookAngle();
            Vector3d eyePos = player.getEyePosition(1.0F);
            Vector3d targetPos = eyePos.add(lookVec.scale(20));
            
            BlockRayTraceResult result = world.clip(new RayTraceContext(
                eyePos, targetPos, RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE, player));
            
            BlockPos teleportPos = result.getBlockPos().relative(result.getDirection());
            player.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5);
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(), 50, 0.5, 1, 0.5, 0.5);
            }
            
            player.displayClientMessage(
                new StringTextComponent("⬡ Short teleport! (Raw stone hurts)")
                    .withStyle(TextFormatting.BLUE), true);
        }
        
        player.getCooldowns().addCooldown(this, 60);
        return ActionResult.success(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("◆ Space Stone").withStyle(TextFormatting.BLUE).withStyle(TextFormatting.BOLD));
        tooltip.add(new StringTextComponent("Power: Teleportation").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("In Gauntlet: Teleport 50+ blocks").withStyle(TextFormatting.DARK_GRAY));
        tooltip.add(new StringTextComponent("Raw: Teleport 20 blocks (hurts)").withStyle(TextFormatting.RED));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Hold gauntlet + RMB to insert")
            .withStyle(TextFormatting.DARK_GRAY).withStyle(TextFormatting.ITALIC));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
