package com.infinitygauntlet.client;

import com.infinitygauntlet.InfinityGauntletMod;
import com.infinitygauntlet.init.ModItems;
import com.infinitygauntlet.network.ModPackets;
import com.infinitygauntlet.network.UseAbilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = InfinityGauntletMod.MOD_ID, value = Dist.CLIENT)
public class ModKeyBindings {
    
    public static final String CATEGORY = "key.categories.infinitygauntlet";
    
    public static KeyBinding KEY_USE_ABILITY;
    public static KeyBinding KEY_SWITCH_STONE;
    public static KeyBinding KEY_SWITCH_SUB_ABILITY;
    public static KeyBinding KEY_COMBO_ABILITY;
    public static KeyBinding KEY_TOGGLE_INFINITY;
    
    public static void register() {
        KEY_USE_ABILITY = registerKey("use_ability", GLFW.GLFW_KEY_R);
        KEY_SWITCH_STONE = registerKey("switch_stone", GLFW.GLFW_KEY_G);
        KEY_SWITCH_SUB_ABILITY = registerKey("switch_sub_ability", GLFW.GLFW_KEY_H);
        KEY_COMBO_ABILITY = registerKey("combo_ability", GLFW.GLFW_KEY_V);
        KEY_TOGGLE_INFINITY = registerKey("toggle_infinity", GLFW.GLFW_KEY_B);
    }
    
    private static KeyBinding registerKey(String name, int key) {
        KeyBinding keyBinding = new KeyBinding(
            "key.infinitygauntlet." + name,
            KeyConflictContext.IN_GAME,
            InputMappings.Type.KEYSYM,
            key,
            CATEGORY
        );
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        
        PlayerEntity player = mc.player;
        ItemStack mainHand = player.getItemInHand(Hand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(Hand.OFF_HAND);
        
        ItemStack gauntlet = null;
        if (mainHand.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
            gauntlet = mainHand;
        } else if (offHand.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
            gauntlet = offHand;
        }
        
        if (gauntlet == null) return;
        
        CompoundNBT nbt = gauntlet.getOrCreateTag();
        
        if (KEY_USE_ABILITY != null && KEY_USE_ABILITY.consumeClick()) {
            // Use current ability
            ModPackets.sendToServer(new UseAbilityPacket(0));
        }
        
        if (KEY_SWITCH_STONE != null && KEY_SWITCH_STONE.consumeClick()) {
            // Switch to next stone
            ModPackets.sendToServer(new UseAbilityPacket(1));
        }
        
        if (KEY_SWITCH_SUB_ABILITY != null && KEY_SWITCH_SUB_ABILITY.consumeClick()) {
            // Switch sub-ability
            ModPackets.sendToServer(new UseAbilityPacket(2));
        }
        
        if (KEY_COMBO_ABILITY != null && KEY_COMBO_ABILITY.consumeClick()) {
            // Use combo ability
            ModPackets.sendToServer(new UseAbilityPacket(3));
        }
        
        if (KEY_TOGGLE_INFINITY != null && KEY_TOGGLE_INFINITY.consumeClick()) {
            // Toggle infinity mode
            ModPackets.sendToServer(new UseAbilityPacket(4));
        }
    }
}
