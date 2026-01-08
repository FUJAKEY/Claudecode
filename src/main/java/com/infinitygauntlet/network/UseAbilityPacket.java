package com.infinitygauntlet.network;

import com.infinitygauntlet.abilities.ComboAbilities;
import com.infinitygauntlet.init.ModItems;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UseAbilityPacket {
    
    private final int action;
    
    public UseAbilityPacket(int action) {
        this.action = action;
    }
    
    public static void encode(UseAbilityPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.action);
    }
    
    public static UseAbilityPacket decode(PacketBuffer buf) {
        return new UseAbilityPacket(buf.readInt());
    }
    
    public static void handle(UseAbilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;
            
            ItemStack gauntlet = null;
            for (Hand hand : Hand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
                    gauntlet = stack;
                    break;
                }
            }
            
            if (gauntlet == null) return;
            
            CompoundNBT nbt = gauntlet.getOrCreateTag();
            
            String[] stoneKeys = {"space_stone", "time_stone", "reality_stone", 
                                  "power_stone", "mind_stone", "soul_stone"};
            String[] stoneNames = {"Space", "Time", "Reality", "Power", "Mind", "Soul"};
            TextFormatting[] colors = {TextFormatting.BLUE, TextFormatting.GREEN, TextFormatting.RED,
                                       TextFormatting.DARK_PURPLE, TextFormatting.YELLOW, TextFormatting.GOLD};
            
            switch (msg.action) {
                case 0: // Use ability
                    gauntlet.getItem().use(player.level, player, Hand.MAIN_HAND);
                    break;
                    
                case 1: // Switch stone
                    int activeStone = nbt.getInt("active_stone");
                    int nextStone = activeStone;
                    for (int i = 0; i < 6; i++) {
                        nextStone = (nextStone + 1) % 6;
                        if (nbt.getBoolean(stoneKeys[nextStone])) break;
                    }
                    nbt.putInt("active_stone", nextStone);
                    player.displayClientMessage(
                        new StringTextComponent("◆ " + stoneNames[nextStone] + " Stone")
                            .withStyle(colors[nextStone]), true);
                    player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5F, 1.5F);
                    break;
                    
                case 2: // Switch sub-ability
                    int stone = nbt.getInt("active_stone");
                    if (nbt.getBoolean(stoneKeys[stone])) {
                        int subAbility = nbt.getInt("sub_ability_" + stone);
                        subAbility = (subAbility + 1) % 2;
                        nbt.putInt("sub_ability_" + stone, subAbility);
                        
                        if (stone == 1) { // Time stone
                            nbt.putBoolean("infinity_active", subAbility == 1);
                            if (subAbility == 1) {
                                player.displayClientMessage(
                                    new StringTextComponent("∞ INFINITY ACTIVATED ∞")
                                        .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
                            } else {
                                player.displayClientMessage(
                                    new StringTextComponent("Infinity deactivated")
                                        .withStyle(TextFormatting.GRAY), true);
                            }
                        }
                        
                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.5F, 2.0F);
                    }
                    break;
                    
                case 3: // Combo ability
                    int primary = nbt.getInt("active_stone");
                    // Find secondary stone (next available)
                    int secondary = -1;
                    for (int i = 0; i < 6; i++) {
                        int idx = (primary + 1 + i) % 6;
                        if (idx != primary && nbt.getBoolean(stoneKeys[idx])) {
                            secondary = idx;
                            break;
                        }
                    }
                    if (secondary != -1) {
                        ComboAbilities.tryCombo(player.level, player, gauntlet, primary, secondary);
                    } else {
                        player.displayClientMessage(
                            new StringTextComponent("Need 2+ stones for combo!")
                                .withStyle(TextFormatting.RED), true);
                    }
                    break;
                    
                case 4: // Toggle infinity
                    if (nbt.getBoolean("time_stone")) {
                        boolean active = !nbt.getBoolean("infinity_active");
                        nbt.putBoolean("infinity_active", active);
                        nbt.putInt("sub_ability_1", active ? 1 : 0);
                        if (active) {
                            player.displayClientMessage(
                                new StringTextComponent("∞ INFINITY ACTIVATED ∞")
                                    .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0F, 0.5F);
                        } else {
                            player.displayClientMessage(
                                new StringTextComponent("Infinity deactivated")
                                    .withStyle(TextFormatting.GRAY), true);
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        }
                    } else {
                        player.displayClientMessage(
                            new StringTextComponent("Need Time Stone for Infinity!")
                                .withStyle(TextFormatting.RED), true);
                    }
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
