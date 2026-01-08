package com.schematicsbuilder.client;

import com.schematicsbuilder.SchematicsBuilderMod;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side keybindings v3.0
 */
public class ModKeyBindings {

        public static KeyBinding OPEN_MENU;
        public static KeyBinding TOGGLE_PREVIEW;
        public static KeyBinding START_BUILD;
        public static KeyBinding STOP_BUILD;
        public static KeyBinding TOGGLE_PAUSE;
        public static KeyBinding ROTATE_LEFT;
        public static KeyBinding ROTATE_RIGHT;

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

                ClientRegistry.registerKeyBinding(OPEN_MENU);
                ClientRegistry.registerKeyBinding(TOGGLE_PREVIEW);
                ClientRegistry.registerKeyBinding(START_BUILD);
                ClientRegistry.registerKeyBinding(STOP_BUILD);
                ClientRegistry.registerKeyBinding(TOGGLE_PAUSE);
                ClientRegistry.registerKeyBinding(ROTATE_LEFT);
                ClientRegistry.registerKeyBinding(ROTATE_RIGHT);
        }
}
