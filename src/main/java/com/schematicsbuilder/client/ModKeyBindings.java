package com.schematicsbuilder.client;

import com.schematicsbuilder.network.ModNetworking;
import com.schematicsbuilder.network.SchematicActionPacket;
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
 * Client-side keybindings for schematic controls
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ModKeyBindings {

    public static KeyBinding OPEN_MENU;
    public static KeyBinding TOGGLE_PREVIEW;
    public static KeyBinding ROTATE_LEFT;
    public static KeyBinding ROTATE_RIGHT;
    public static KeyBinding START_BUILD;
    public static KeyBinding STOP_BUILD;
    public static KeyBinding TOGGLE_PAUSE;

    public static void register() {
        OPEN_MENU = new KeyBinding(
                "key.schematicsbuilder.open_menu",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "key.category.schematicsbuilder");

        TOGGLE_PREVIEW = new KeyBinding(
                "key.schematicsbuilder.toggle_preview",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "key.category.schematicsbuilder");

        ROTATE_LEFT = new KeyBinding(
                "key.schematicsbuilder.rotate_left",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                "key.category.schematicsbuilder");

        ROTATE_RIGHT = new KeyBinding(
                "key.schematicsbuilder.rotate_right",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                "key.category.schematicsbuilder");

        START_BUILD = new KeyBinding(
                "key.schematicsbuilder.start_build",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.category.schematicsbuilder");

        STOP_BUILD = new KeyBinding(
                "key.schematicsbuilder.stop_build",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "key.category.schematicsbuilder");

        TOGGLE_PAUSE = new KeyBinding(
                "key.schematicsbuilder.toggle_pause",
                KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                "key.category.schematicsbuilder");

        ClientRegistry.registerKeyBinding(OPEN_MENU);
        ClientRegistry.registerKeyBinding(TOGGLE_PREVIEW);
        ClientRegistry.registerKeyBinding(ROTATE_LEFT);
        ClientRegistry.registerKeyBinding(ROTATE_RIGHT);
        ClientRegistry.registerKeyBinding(START_BUILD);
        ClientRegistry.registerKeyBinding(STOP_BUILD);
        ClientRegistry.registerKeyBinding(TOGGLE_PAUSE);
    }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS)
            return;

        if (OPEN_MENU.isDown()) {
            // Open schematic selection menu
            ModNetworking.sendToServer(new SchematicActionPacket(SchematicActionPacket.Action.LIST_SCHEMATICS, ""));
        }

        if (ROTATE_LEFT.isDown() || ROTATE_RIGHT.isDown()) {
            ModNetworking.sendToServer(new SchematicActionPacket(SchematicActionPacket.Action.ROTATE, ""));
        }

        if (START_BUILD.isDown()) {
            ModNetworking.sendToServer(new SchematicActionPacket(SchematicActionPacket.Action.START_BUILD, ""));
        }

        if (STOP_BUILD.isDown()) {
            ModNetworking.sendToServer(new SchematicActionPacket(SchematicActionPacket.Action.STOP_BUILD, ""));
        }

        if (TOGGLE_PAUSE.isDown()) {
            ModNetworking.sendToServer(new SchematicActionPacket(SchematicActionPacket.Action.TOGGLE_PAUSE, ""));
        }
    }
}
