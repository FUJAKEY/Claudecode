package com.schematicsbuilder.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.schematicsbuilder.SchematicsBuilderMod;
import com.schematicsbuilder.client.ClientAutoBuilder;
import com.schematicsbuilder.client.MaterialCalculator;
import com.schematicsbuilder.client.SchematicPreviewRenderer;
import com.schematicsbuilder.schematic.SchematicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main GUI Screen for Schematics Builder
 */
public class SchematicMenuScreen extends Screen {

    private final Minecraft mc = Minecraft.getInstance();
    private List<File> schematicFiles = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private final int listHeight = 160;
    private final int itemHeight = 20;

    public SchematicMenuScreen() {
        super(new StringTextComponent("Schematics Builder"));
        loadSchematicFiles();
    }

    private void loadSchematicFiles() {
        schematicFiles.clear();
        File folder = SchematicsBuilderMod.schematicsFolder;
        if (folder != null && folder.exists()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".schematic") || name.endsWith(".schem")
                    || name.endsWith(".litematic"));
            if (files != null) {
                for (File f : files) {
                    schematicFiles.add(f);
                }
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int startY = height / 2 + 100;

        // Load button
        addButton(new Button(centerX - 155, startY, 100, 20,
                new StringTextComponent("Load"),
                btn -> loadSelected()));

        // Build button
        addButton(new Button(centerX - 50, startY, 100, 20,
                new StringTextComponent("Start Build"),
                btn -> startBuild()));

        // Materials button
        addButton(new Button(centerX + 55, startY, 100, 20,
                new StringTextComponent("Materials"),
                btn -> showMaterials()));

        // Preview toggle
        addButton(new Button(centerX - 155, startY + 25, 100, 20,
                new StringTextComponent("Toggle Preview"),
                btn -> togglePreview()));

        // Rotate button
        addButton(new Button(centerX - 50, startY + 25, 100, 20,
                new StringTextComponent("Rotate 90°"),
                btn -> rotate()));

        // Close button
        addButton(new Button(centerX + 55, startY + 25, 100, 20,
                new StringTextComponent("Close"),
                btn -> onClose()));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);

        int centerX = width / 2;
        int startY = 30;

        // Title
        drawCenteredString(matrixStack, font, "§6§l═══ Schematics Builder ═══", centerX, startY, 0xFFFFFF);

        // Schematic list background
        int listX = centerX - 150;
        int listY = startY + 30;
        int listWidth = 300;

        fill(matrixStack, listX - 5, listY - 5, listX + listWidth + 5, listY + listHeight + 5, 0x80000000);

        // Render schematic list
        int visibleItems = listHeight / itemHeight;
        for (int i = 0; i < visibleItems && (scrollOffset + i) < schematicFiles.size(); i++) {
            int index = scrollOffset + i;
            File file = schematicFiles.get(index);
            int itemY = listY + i * itemHeight;

            // Selection highlight
            if (index == selectedIndex) {
                fill(matrixStack, listX, itemY, listX + listWidth, itemY + itemHeight, 0x80FFAA00);
            } else if (mouseX >= listX && mouseX <= listX + listWidth &&
                    mouseY >= itemY && mouseY <= itemY + itemHeight) {
                fill(matrixStack, listX, itemY, listX + listWidth, itemY + itemHeight, 0x40FFFFFF);
            }

            // File name
            String fileName = file.getName();
            long kb = file.length() / 1024;
            String text = fileName + " §7(" + kb + " KB)";

            font.draw(matrixStack, text, listX + 5, itemY + 6, 0xFFFFFF);
        }

        // Scrollbar
        if (schematicFiles.size() > visibleItems) {
            int scrollbarX = listX + listWidth + 2;
            int scrollbarHeight = listHeight * visibleItems / schematicFiles.size();
            int scrollbarY = listY
                    + (listHeight - scrollbarHeight) * scrollOffset / (schematicFiles.size() - visibleItems);

            fill(matrixStack, scrollbarX, listY, scrollbarX + 3, listY + listHeight, 0x40FFFFFF);
            fill(matrixStack, scrollbarX, scrollbarY, scrollbarX + 3, scrollbarY + scrollbarHeight, 0xFFFFFFFF);
        }

        // Current schematic info
        SchematicData current = ClientAutoBuilder.getInstance().getSchematic();
        int infoY = listY + listHeight + 15;

        if (current != null) {
            drawCenteredString(matrixStack, font, "§aLoaded: §e" + current.getName(), centerX, infoY, 0xFFFFFF);
            drawCenteredString(matrixStack, font,
                    "§7Size: " + current.getWidth() + "x" + current.getHeight() + "x" + current.getLength() +
                            " | Blocks: " + current.getBlockCount() + " | Rotation: " + current.getRotation() + "°",
                    centerX, infoY + 12, 0xFFFFFF);

            // Material percentage
            int materialPercent = MaterialCalculator.getMaterialPercentage(current);
            String matColor = materialPercent == 100 ? "§a" : (materialPercent > 50 ? "§e" : "§c");
            drawCenteredString(matrixStack, font,
                    "§7Materials: " + matColor + materialPercent + "% §7available",
                    centerX, infoY + 24, 0xFFFFFF);

            // Preview status
            String previewStatus = SchematicPreviewRenderer.isPreviewEnabled() ? "§aON" : "§cOFF";
            drawCenteredString(matrixStack, font, "§7Preview: " + previewStatus, centerX, infoY + 36, 0xFFFFFF);
        } else {
            drawCenteredString(matrixStack, font, "§7No schematic loaded", centerX, infoY, 0xFFFFFF);
            drawCenteredString(matrixStack, font, "§7Select a file and click Load", centerX, infoY + 12, 0xFFFFFF);
        }

        // Instructions
        drawCenteredString(matrixStack, font, "§8Click to select, scroll to navigate", centerX, height - 20, 0xFFFFFF);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = width / 2;
        int listX = centerX - 150;
        int listY = 60;
        int listWidth = 300;

        if (mouseX >= listX && mouseX <= listX + listWidth &&
                mouseY >= listY && mouseY <= listY + listHeight) {

            int clickedIndex = scrollOffset + (int) ((mouseY - listY) / itemHeight);
            if (clickedIndex >= 0 && clickedIndex < schematicFiles.size()) {
                selectedIndex = clickedIndex;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int visibleItems = listHeight / itemHeight;
        int maxScroll = Math.max(0, schematicFiles.size() - visibleItems);

        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) delta));

        return true;
    }

    private void loadSelected() {
        if (selectedIndex >= 0 && selectedIndex < schematicFiles.size()) {
            File file = schematicFiles.get(selectedIndex);
            try {
                SchematicData data = SchematicData.load(file);
                data.setOrigin(mc.player.blockPosition());
                ClientAutoBuilder.getInstance().loadSchematic(data);
            } catch (Exception e) {
                mc.player.displayClientMessage(
                        new StringTextComponent("§cFailed to load: " + e.getMessage()), false);
            }
        }
    }

    private void startBuild() {
        onClose();
        ClientAutoBuilder.getInstance().start();
    }

    private void showMaterials() {
        SchematicData current = ClientAutoBuilder.getInstance().getSchematic();
        if (current != null) {
            onClose();
            MaterialCalculator.showMaterials(current);
            MaterialCalculator.showMissing(current);
        }
    }

    private void togglePreview() {
        SchematicPreviewRenderer.togglePreview();
    }

    private void rotate() {
        SchematicData current = ClientAutoBuilder.getInstance().getSchematic();
        if (current != null) {
            current.rotate90();
            mc.player.displayClientMessage(
                    new StringTextComponent("§bRotated to " + current.getRotation() + "°"), true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
