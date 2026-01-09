package com.arcanemagic.client;

import com.arcanemagic.item.WandItem;
import com.arcanemagic.network.NetworkHandler;
import com.arcanemagic.network.SpellSwitchPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handles keyboard input for spell switching
 */
@OnlyIn(Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;

        if (player == null || mc.screen != null)
            return;

        // Check if player is holding a wand
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        ItemStack wandStack = null;
        if (mainHand.getItem() instanceof WandItem) {
            wandStack = mainHand;
        } else if (offHand.getItem() instanceof WandItem) {
            wandStack = offHand;
        }

        if (wandStack == null)
            return;

        WandItem wand = (WandItem) wandStack.getItem();

        // Handle key presses
        if (ModKeyBindings.NEXT_SPELL.consumeClick()) {
            // Cycle to next spell
            wand.cycleSpell(wandStack, true);
            // Send packet to server
            NetworkHandler.getChannel().sendToServer(new SpellSwitchPacket(true));
        }

        if (ModKeyBindings.PREV_SPELL.consumeClick()) {
            // Cycle to previous spell
            wand.cycleSpell(wandStack, false);
            // Send packet to server
            NetworkHandler.getChannel().sendToServer(new SpellSwitchPacket(false));
        }
    }
}
