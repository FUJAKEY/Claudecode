package com.arcanemagic.client;

import com.arcanemagic.ArcaneMagicMod;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

/**
 * Custom keybindings for ArcaneMagic mod
 */
@OnlyIn(Dist.CLIENT)
public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.arcanemagic";

    // Spell switching keys
    public static KeyBinding NEXT_SPELL;
    public static KeyBinding PREV_SPELL;
    public static KeyBinding CAST_SPELL;

    public static void register() {
        NEXT_SPELL = registerKey("next_spell", GLFW.GLFW_KEY_R);
        PREV_SPELL = registerKey("prev_spell", GLFW.GLFW_KEY_F);
        CAST_SPELL = registerKey("cast_spell", GLFW.GLFW_KEY_G);

        ArcaneMagicMod.LOGGER.info("ArcaneMagic keybindings registered!");
    }

    private static KeyBinding registerKey(String name, int defaultKey) {
        KeyBinding key = new KeyBinding(
                "key.arcanemagic." + name,
                defaultKey,
                CATEGORY);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
