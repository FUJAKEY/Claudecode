package com.arcanemagic.item;

import com.arcanemagic.capability.ManaCapability;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Mana Crystal - consumable item that restores mana
 */
public class ManaCrystalItem extends Item {

    private static final int MANA_RESTORE = 25;
    private static final int USE_DURATION = 20; // 1 second

    public ManaCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Check if player needs mana
        boolean[] needsMana = { false };
        player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {
            if (mana.getMana() < mana.getMaxMana()) {
                needsMana[0] = true;
            }
        });

        if (needsMana[0]) {
            player.startUsingItem(hand);
            return ActionResult.consume(stack);
        }

        return ActionResult.fail(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        if (entity instanceof PlayerEntity && !world.isClientSide) {
            PlayerEntity player = (PlayerEntity) entity;

            player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {
                mana.regenerateMana(MANA_RESTORE);

                // Play sound
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.5f);

                // Spawn particles
                if (world instanceof ServerWorld) {
                    ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANT,
                            player.getX(), player.getY() + 1, player.getZ(),
                            30, 0.5, 0.5, 0.5, 0.5);
                }
            });

            // Consume item
            if (!player.abilities.instabuild) {
                stack.shrink(1);
            }
        }

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip,
            ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("Restores " + MANA_RESTORE + " mana")
                .withStyle(TextFormatting.BLUE));
        tooltip.add(new StringTextComponent("Right-click to use")
                .withStyle(TextFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Glowing effect
    }
}
