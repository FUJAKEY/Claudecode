package com.arcanemagic.item;

import com.arcanemagic.capability.ManaCapability;
import com.arcanemagic.network.ManaPacket;
import com.arcanemagic.network.NetworkHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ManaUpgradeItem extends Item {

    public ManaUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        player.startUsingItem(hand);
        return ActionResult.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;

            player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
                int newMax = mana.getMaxMana() + 10;
                mana.setMaxMana(newMax);

                // Refill mana too
                mana.setMana(newMax);

                NetworkHandler.getChannel().send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ManaPacket(mana.getMana(), mana.getMaxMana()));

                player.displayClientMessage(new StringTextComponent(
                        "Max Mana increased! (+10)").withStyle(TextFormatting.GOLD), true);

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            });
        }

        stack.shrink(1);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.DRINK;
    }
}
