package com.schematicsbuilder.client;

import com.schematicsbuilder.SchematicsBuilderMod;
import com.schematicsbuilder.client.gui.SchematicMenuScreen;
import com.schematicsbuilder.schematic.SchematicData;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.util.List;

/**
 * Client-side event handler for tick and input processing
 */
@Mod.EventBusSubscriber(modid = SchematicsBuilderMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * Client tick - runs auto builder
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (mc.player == null || mc.level == null)
            return;
        if (mc.isPaused())
            return;

        // Tick the auto builder
        ClientAutoBuilder.getInstance().onClientTick();
    }

    /**
     * Key input handler
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.player == null)
            return;
        if (mc.screen != null)
            return; // Don't process if GUI open

        if (event.getAction() != 1)
            return; // Only key press

        ClientAutoBuilder builder = ClientAutoBuilder.getInstance();

        // O - Open GUI Menu
        if (ModKeyBindings.OPEN_MENU.consumeClick()) {
            mc.setScreen(new SchematicMenuScreen());
        }

        // P - Toggle Preview
        if (ModKeyBindings.TOGGLE_PREVIEW != null && ModKeyBindings.TOGGLE_PREVIEW.consumeClick()) {
            SchematicPreviewRenderer.togglePreview();
        }

        // B - Start build
        if (ModKeyBindings.START_BUILD.consumeClick()) {
            builder.start();
        }

        // N - Stop build
        if (ModKeyBindings.STOP_BUILD.consumeClick()) {
            builder.stop();
        }

        // , - Pause
        if (ModKeyBindings.TOGGLE_PAUSE.consumeClick()) {
            builder.togglePause();
        }

        // [ or ] - Rotate
        if (ModKeyBindings.ROTATE_LEFT.consumeClick() || ModKeyBindings.ROTATE_RIGHT.consumeClick()) {
            SchematicData data = builder.getSchematic();
            if (data != null) {
                data.rotate90();
                sendMessage("§b⟳ Rotated to " + data.getRotation() + "°");
            }
        }
    }

    /**
     * Get block player is looking at
     */
    public static BlockPos getLookedAtBlock(double maxDistance) {
        if (mc.player == null)
            return null;

        Vector3d eyePos = mc.player.getEyePosition(1.0F);
        Vector3d lookVec = mc.player.getLookAngle();
        Vector3d endPos = eyePos.add(lookVec.scale(maxDistance));

        BlockRayTraceResult result = mc.level.clip(new RayTraceContext(
                eyePos, endPos,
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                mc.player));

        if (result.getType() == RayTraceResult.Type.BLOCK) {
            return result.getBlockPos();
        }
        return null;
    }

    private static void sendMessage(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(new StringTextComponent(msg), false);
        }
    }
}
