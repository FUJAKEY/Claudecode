package com.schematicsbuilder.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.schematicsbuilder.SchematicsBuilderMod;
import com.schematicsbuilder.schematic.SchematicData;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.Map;

/**
 * Renders ghost preview of schematic before building
 * Shows where blocks will be placed with semi-transparent overlay
 */
@Mod.EventBusSubscriber(modid = SchematicsBuilderMod.MOD_ID, value = Dist.CLIENT)
public class SchematicPreviewRenderer {

    private static boolean previewEnabled = true;
    private static final Minecraft mc = Minecraft.getInstance();

    public static void togglePreview() {
        previewEnabled = !previewEnabled;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    new net.minecraft.util.text.StringTextComponent(
                            previewEnabled ? "§aPreview ON" : "§cPreview OFF"),
                    true);
        }
    }

    public static boolean isPreviewEnabled() {
        return previewEnabled;
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderWorldLastEvent event) {
        if (!previewEnabled)
            return;
        if (mc.player == null || mc.level == null)
            return;

        SchematicData schematic = ClientAutoBuilder.getInstance().getSchematic();
        if (schematic == null)
            return;
        if (ClientAutoBuilder.getInstance().isRunning())
            return; // Don't show during building

        MatrixStack matrixStack = event.getMatrixStack();

        // Get camera position
        Vector3d projectedView = mc.gameRenderer.getMainCamera().getPosition();

        matrixStack.pushPose();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        // Render ghost blocks
        renderGhostBlocks(matrixStack, schematic);

        // Render bounding box
        renderBoundingBox(matrixStack, schematic);

        matrixStack.popPose();
    }

    private static void renderGhostBlocks(MatrixStack matrixStack, SchematicData schematic) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        Matrix4f matrix = matrixStack.last().pose();

        int rendered = 0;
        int maxRender = 5000; // Limit for performance

        for (Map.Entry<BlockPos, BlockState> entry : schematic.getBlocks().entrySet()) {
            if (rendered >= maxRender)
                break;

            BlockPos worldPos = schematic.toWorldPos(entry.getKey());
            BlockState state = entry.getValue();

            // Skip if already placed
            if (mc.level.getBlockState(worldPos).getBlock() == state.getBlock()) {
                continue;
            }

            // Get block color based on type
            float r = 0.2f, g = 0.8f, b = 0.2f, a = 0.3f;

            // Different colors for different block types
            String blockName = state.getBlock().getRegistryName().getPath();
            if (blockName.contains("stone") || blockName.contains("cobble")) {
                r = 0.5f;
                g = 0.5f;
                b = 0.5f;
            } else if (blockName.contains("wood") || blockName.contains("log") || blockName.contains("plank")) {
                r = 0.6f;
                g = 0.4f;
                b = 0.2f;
            } else if (blockName.contains("glass")) {
                r = 0.4f;
                g = 0.8f;
                b = 0.9f;
                a = 0.2f;
            } else if (blockName.contains("brick")) {
                r = 0.7f;
                g = 0.3f;
                b = 0.2f;
            }

            // Render cube
            renderGhostCube(buffer, matrix, worldPos, r, g, b, a);
            rendered++;
        }

        tessellator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void renderGhostCube(BufferBuilder buffer, Matrix4f matrix, BlockPos pos,
            float r, float g, float b, float a) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float s = 0.98f; // Slightly smaller to avoid z-fighting
        float offset = 0.01f;

        // Bottom
        buffer.vertex(matrix, x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + offset, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + offset, y + offset, z + s).color(r, g, b, a).endVertex();

        // Top
        buffer.vertex(matrix, x + offset, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + offset).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + offset, y + s, z + offset).color(r, g, b, a).endVertex();

        // North
        buffer.vertex(matrix, x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + offset, y + s, z + offset).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + offset).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + offset, z + offset).color(r, g, b, a).endVertex();

        // South
        buffer.vertex(matrix, x + s, y + offset, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + offset, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + offset, y + offset, z + s).color(r, g, b, a).endVertex();

        // West
        buffer.vertex(matrix, x + offset, y + offset, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + offset, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + offset, y + s, z + offset).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + offset, y + offset, z + offset).color(r, g, b, a).endVertex();

        // East
        buffer.vertex(matrix, x + s, y + offset, z + offset).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + offset).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + offset, z + s).color(r, g, b, a).endVertex();
    }

    private static void renderBoundingBox(MatrixStack matrixStack, SchematicData schematic) {
        BlockPos origin = schematic.getOrigin();
        int w = schematic.getWidth();
        int h = schematic.getHeight();
        int l = schematic.getLength();

        RenderSystem.lineWidth(2.0f);
        RenderSystem.disableTexture();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        Matrix4f matrix = matrixStack.last().pose();

        float x1 = origin.getX();
        float y1 = origin.getY();
        float z1 = origin.getZ();
        float x2 = x1 + w;
        float y2 = y1 + h;
        float z2 = z1 + l;

        float r = 1.0f, g = 0.5f, b = 0.0f, a = 1.0f; // Orange

        // Bottom square
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();

        // Top square
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();

        // Verticals
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();

        tessellator.end();

        RenderSystem.enableTexture();
    }
}
