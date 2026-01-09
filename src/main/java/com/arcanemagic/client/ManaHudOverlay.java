package com.arcanemagic.client;

import com.arcanemagic.ArcaneMagicMod;
import com.arcanemagic.item.WandItem;
import com.arcanemagic.spell.Spell;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Renders the mana bar and current spell HUD overlay
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

        // ========== CURRENT SPELL DISPLAY ==========
        renderCurrentSpell(mc, matrixStack, screenWidth, screenHeight);

        RenderSystem.disableBlend();
    }

    private void renderCurrentSpell(Minecraft mc, MatrixStack matrixStack, int screenWidth, int screenHeight) {
        // Check if player is holding a wand
        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();

        ItemStack wandStack = null;
        WandItem wand = null;

        if (mainHand.getItem() instanceof WandItem) {
            wandStack = mainHand;
            wand = (WandItem) mainHand.getItem();
        } else if (offHand.getItem() instanceof WandItem) {
            wandStack = offHand;
            wand = (WandItem) offHand.getItem();
        }

        if (wandStack == null || wand == null)
            return;

        // Get selected spell
        String selectedSpellId = wand.getSelectedSpellId(wandStack);
        if (selectedSpellId == null || selectedSpellId.isEmpty())
            return;

        Spell spell = WandItem.getSpell(selectedSpellId);
        if (spell == null)
            return;

        // Position: TOP LEFT corner, below mana bar (not center screen)
        int x = BAR_X_OFFSET;
        int y = BAR_Y_OFFSET + BAR_HEIGHT + 25; // Below mana bar and text

        // Draw spell name with fancy formatting
        String spellName = "◆ " + spell.getDisplayName().getString();
        int spellColor = spell.getSpellColor();

        // Draw background box
        int textWidth = mc.font.width(spellName);
        int boxX = x - 3;
        int boxY = y - 2;
        int boxWidth = textWidth + 6;
        int boxHeight = 14;

        // Semi-transparent background
        AbstractGui.fill(matrixStack, screenWidth - BAR_X_OFFSET - boxWidth - 3, boxY,
                screenWidth - BAR_X_OFFSET + 3, boxY + boxHeight, 0x80000000);

        // Draw border with spell color
        int borderColor = (0xFF << 24) | spellColor;
        int rightX = screenWidth - BAR_X_OFFSET - boxWidth - 3;
        AbstractGui.fill(matrixStack, rightX, boxY, screenWidth - BAR_X_OFFSET + 3, boxY + 1, borderColor);
        AbstractGui.fill(matrixStack, rightX, boxY + boxHeight - 1, screenWidth - BAR_X_OFFSET + 3, boxY + boxHeight,
                borderColor);

        // Draw spell name (right-aligned, below mana bar)
        mc.font.drawShadow(matrixStack, spellName, screenWidth - BAR_X_OFFSET - textWidth, y, spellColor);

        // Draw tier indicator smaller, right aligned
        String tierName = "[" + wand.getTier().getDisplayName() + "]";
        TextFormatting tierColor = wand.getTier().getColor();
        int tierWidth = mc.font.width(tierName);
        mc.font.drawShadow(matrixStack, tierName, screenWidth - BAR_X_OFFSET - tierWidth, y + 11,
                tierColor.getColor() != null ? tierColor.getColor() : 0xAAAAAA);
    }
}
