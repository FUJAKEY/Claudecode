package com.arcanemagic.client;

import com.arcanemagic.ArcaneMagicMod;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Renders the mana bar HUD overlay
 */
@OnlyIn(Dist.CLIENT)
public class ManaHudOverlay {

    private static final ResourceLocation MANA_BAR_TEXTURE = new ResourceLocation(ArcaneMagicMod.MOD_ID,
            "textures/gui/mana_bar.png");

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 8;
    private static final int BAR_X_OFFSET = 10;
    private static final int BAR_Y_OFFSET = 10;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        MatrixStack matrixStack = event.getMatrixStack();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int x = screenWidth - BAR_WIDTH - BAR_X_OFFSET;
        int y = BAR_Y_OFFSET;

        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Draw background bar
        mc.getTextureManager().bind(MANA_BAR_TEXTURE);
        AbstractGui.blit(matrixStack, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT, 128, 32);

        // Draw filled portion based on mana percentage
        float percentage = ClientManaData.getManaPercentage();
        int filledWidth = (int) (BAR_WIDTH * percentage);

        if (filledWidth > 0) {
            AbstractGui.blit(matrixStack, x, y, 0, BAR_HEIGHT, filledWidth, BAR_HEIGHT, 128, 32);
        }

        // Draw mana text
        String manaText = ClientManaData.getMana() + "/" + ClientManaData.getMaxMana();
        int textX = x + (BAR_WIDTH - mc.font.width(manaText)) / 2;
        int textY = y + BAR_HEIGHT + 2;

        // Draw text with shadow
        mc.font.drawShadow(matrixStack, manaText, textX, textY, 0x00BFFF);

        RenderSystem.disableBlend();
    }
}
