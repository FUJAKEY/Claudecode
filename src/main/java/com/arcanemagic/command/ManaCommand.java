package com.arcanemagic.command;

import com.arcanemagic.capability.ManaCapability;
import com.arcanemagic.network.ManaPacket;
import com.arcanemagic.network.NetworkHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.PacketDistributor;

/**
 * Admin commands for mana management
 * /mana set <player> <amount> - Set player's current mana
 * /mana setmax <player> <amount> - Set player's max mana
 * /mana add <player> <amount> - Add mana to player
 * /mana addmax <player> <amount> - Increase max mana
 * /mana get <player> - Get player's mana info
 */
public class ManaCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("mana")
                        .requires(source -> source.hasPermission(2)) // Requires OP level 2

                        // /mana set <player> <amount>
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(ctx -> setMana(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "amount"))))))

                        // /mana setmax <player> <amount>
                        .then(Commands.literal("setmax")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000))
                                                .executes(ctx -> setMaxMana(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "amount"))))))

                        // /mana add <player> <amount>
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(ctx -> addMana(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "amount"))))))

                        // /mana addmax <player> <amount>
                        .then(Commands.literal("addmax")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(ctx -> addMaxMana(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "amount"))))))

                        // /mana get <player>
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> getMana(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")))))

                        // /mana fill <player> - fill mana to max
                        .then(Commands.literal("fill")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> fillMana(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"))))));
    }

    private static int setMana(CommandSource source, ServerPlayerEntity player, int amount) {
        player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
            mana.setMana(Math.min(amount, mana.getMaxMana()));
            syncMana(player, mana);
            source.sendSuccess(new StringTextComponent(
                    "Set " + player.getName().getString() + "'s mana to " + mana.getMana())
                    .withStyle(TextFormatting.GREEN), true);
        });
        return 1;
    }

    private static int setMaxMana(CommandSource source, ServerPlayerEntity player, int amount) {
        player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
            mana.setMaxMana(amount);
            // Adjust current mana if it exceeds new max
            if (mana.getMana() > amount) {
                mana.setMana(amount);
            }
            syncMana(player, mana);
            source.sendSuccess(new StringTextComponent(
                    "Set " + player.getName().getString() + "'s max mana to " + amount).withStyle(TextFormatting.GREEN),
                    true);
        });
        return 1;
    }

    private static int addMana(CommandSource source, ServerPlayerEntity player, int amount) {
        player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
            int newMana = Math.max(0, Math.min(mana.getMana() + amount, mana.getMaxMana()));
            mana.setMana(newMana);
            syncMana(player, mana);
            source.sendSuccess(new StringTextComponent(
                    (amount >= 0 ? "Added " : "Removed ") + Math.abs(amount) +
                            " mana " + (amount >= 0 ? "to " : "from ") + player.getName().getString() +
                            " (now " + mana.getMana() + "/" + mana.getMaxMana() + ")")
                    .withStyle(TextFormatting.GREEN), true);
        });
        return 1;
    }

    private static int addMaxMana(CommandSource source, ServerPlayerEntity player, int amount) {
        player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
            int newMax = Math.max(1, mana.getMaxMana() + amount);
            mana.setMaxMana(newMax);
            syncMana(player, mana);
            source.sendSuccess(new StringTextComponent(
                    (amount >= 0 ? "Increased " : "Decreased ") + player.getName().getString() +
                            "'s max mana by " + Math.abs(amount) + " (now " + newMax + ")")
                    .withStyle(TextFormatting.GREEN), true);
        });
        return 1;
    }

    private static int getMana(CommandSource source, ServerPlayerEntity player) {
        player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
            source.sendSuccess(new StringTextComponent(
                    player.getName().getString() + "'s mana: " +
                            mana.getMana() + "/" + mana.getMaxMana() +
                            " (regen: " + mana.getManaRegenRate() + "/sec)")
                    .withStyle(TextFormatting.AQUA), false);
        });
        return 1;
    }

    private static int fillMana(CommandSource source, ServerPlayerEntity player) {
        player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
            mana.setMana(mana.getMaxMana());
            syncMana(player, mana);
            source.sendSuccess(new StringTextComponent(
                    "Filled " + player.getName().getString() + "'s mana to max (" + mana.getMaxMana() + ")")
                    .withStyle(TextFormatting.GREEN), true);
        });
        return 1;
    }

    private static void syncMana(ServerPlayerEntity player, ManaCapability mana) {
        NetworkHandler.getChannel().send(
                PacketDistributor.PLAYER.with(() -> player),
                new ManaPacket(mana.getMana(), mana.getMaxMana()));
    }
}
