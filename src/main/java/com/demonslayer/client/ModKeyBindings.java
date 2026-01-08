package com.demonslayer.client;

import com.demonslayer.network.ActivateMarkPacket;
import com.demonslayer.network.ModNetworking;
import com.demonslayer.network.SwitchFormPacket;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side keybindings for abilities
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ModKeyBindings {

    public static KeyBinding SWITCH_FORM_FORWARD;
    public static KeyBinding SWITCH_FORM_BACKWARD;
    public static KeyBinding ACTIVATE_MARK;
    public static KeyBinding OPEN_QUEST_MENU;
    public static KeyBinding SHOW_STATS;

    public static void register() {
        SWITCH_FORM_FORWARD = new KeyBinding(
                "key.demonslayer.switch_form_forward",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.category.demonslayer");

        SWITCH_FORM_BACKWARD = new KeyBinding(
                "key.demonslayer.switch_form_backward",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.category.demonslayer");

        ACTIVATE_MARK = new KeyBinding(
                "key.demonslayer.activate_mark",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "key.category.demonslayer");

        OPEN_QUEST_MENU = new KeyBinding(
                "key.demonslayer.open_quest_menu",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "key.category.demonslayer");

        SHOW_STATS = new KeyBinding(
                "key.demonslayer.show_stats",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key.category.demonslayer");

        ClientRegistry.registerKeyBinding(SWITCH_FORM_FORWARD);
        ClientRegistry.registerKeyBinding(SWITCH_FORM_BACKWARD);
        ClientRegistry.registerKeyBinding(ACTIVATE_MARK);
        ClientRegistry.registerKeyBinding(OPEN_QUEST_MENU);
        ClientRegistry.registerKeyBinding(SHOW_STATS);
    }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS)
            return;

        if (SWITCH_FORM_FORWARD.isDown()) {
            ModNetworking.sendToServer(new SwitchFormPacket(true));
        }

        if (SWITCH_FORM_BACKWARD.isDown()) {
            ModNetworking.sendToServer(new SwitchFormPacket(false));
        }

        if (ACTIVATE_MARK.isDown()) {
            ModNetworking.sendToServer(new ActivateMarkPacket());
        }

        if (SHOW_STATS.isDown()) {
            showStats();
        }
    }

    private static void showStats() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null)
            return;

        net.minecraft.nbt.CompoundNBT data = mc.player.getPersistentData();
        int xp = data.getInt("demonslayer_xp");
        int rank = data.getInt("demonslayer_rank");
        boolean mark = data.getBoolean("slayer_mark_active");

        String[] rankNames = { "Mizunoto", "Mizunoe", "Kanoto", "Kanoe", "Tsuchinoto",
                "Tsuchinoe", "Hinoto", "Hinoe", "Kinoto", "Kinoe", "Hashira" };
        String rankName = rank < rankNames.length ? rankNames[rank] : "Unknown";

        mc.player.displayClientMessage(
                new net.minecraft.util.text.StringTextComponent("═════ Demon Slayer Stats ═════")
                        .withStyle(net.minecraft.util.text.TextFormatting.GOLD),
                false);
        mc.player.displayClientMessage(
                new net.minecraft.util.text.StringTextComponent("Rank: " + rankName)
                        .withStyle(net.minecraft.util.text.TextFormatting.YELLOW),
                false);
        mc.player.displayClientMessage(
                new net.minecraft.util.text.StringTextComponent("XP: " + xp)
                        .withStyle(net.minecraft.util.text.TextFormatting.GREEN),
                false);
        mc.player.displayClientMessage(
                new net.minecraft.util.text.StringTextComponent("Mark: " + (mark ? "ACTIVE" : "Inactive"))
                        .withStyle(mark ? net.minecraft.util.text.TextFormatting.RED
                                : net.minecraft.util.text.TextFormatting.GRAY),
                false);
    }
}
