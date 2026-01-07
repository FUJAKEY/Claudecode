package com.infinitygauntlet.items;

import com.infinitygauntlet.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealityStoneItem extends Item {
    
    private static final Map<Block, Block> TRANSFORMS = new HashMap<Block, Block>() {{
        put(Blocks.STONE, Blocks.GOLD_ORE);
        put(Blocks.DIRT, Blocks.IRON_ORE);
        put(Blocks.COBBLESTONE, Blocks.LAPIS_ORE);
    }};
    
    public RealityStoneItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        
        if (otherStack.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
            if (InfinityGauntletItem.insertStone(otherStack, "reality_stone")) {
                if (!world.isClientSide) {
                    player.displayClientMessage(
                        new StringTextComponent("◆ Reality Stone inserted into gauntlet!")
                            .withStyle(TextFormatting.RED).withStyle(TextFormatting.BOLD), true);
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
                        new StringTextComponent("Reality Stone already in gauntlet!")
                            .withStyle(TextFormatting.RED), true);
                }
            }
            return ActionResult.pass(stack);
        }
        
        // Use stone alone (limited transform)
        if (!world.isClientSide) {
            player.hurt(DamageSource.MAGIC, 2.0F);
            
            BlockPos playerPos = player.blockPosition();
            int transformed = 0;
            
            for (int x = -2; x <= 2; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -2; z <= 2; z++) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        Block block = world.getBlockState(pos).getBlock();
                        if (TRANSFORMS.containsKey(block)) {
                            world.setBlock(pos, TRANSFORMS.get(block).defaultBlockState(), 3);
                            transformed++;
                        }
                    }
                }
            }
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.CRIMSON_SPORE,
                    player.getX(), player.getY() + 1, player.getZ(), 100, 3, 2, 3, 0.1);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 0.5F);
            
            player.displayClientMessage(
                new StringTextComponent("✦ " + transformed + " blocks warped!")
                    .withStyle(TextFormatting.RED), true);
        }
        
        player.getCooldowns().addCooldown(this, 80);
        return ActionResult.success(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("◆ Reality Stone").withStyle(TextFormatting.RED).withStyle(TextFormatting.BOLD));
        tooltip.add(new StringTextComponent("Power: Reality Warping").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("In Gauntlet: Large area transform").withStyle(TextFormatting.DARK_GRAY));
        tooltip.add(new StringTextComponent("Raw: Small area (hurts)").withStyle(TextFormatting.RED));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Hold gauntlet + RMB to insert")
            .withStyle(TextFormatting.DARK_GRAY).withStyle(TextFormatting.ITALIC));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
