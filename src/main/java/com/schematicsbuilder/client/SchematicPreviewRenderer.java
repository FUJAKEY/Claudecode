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
 * Renders ghost preview of schematic
 * NOW SHOWS DURING BUILDING with progress indication!
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

        // ALWAYS show preview (removed check for isRunning)

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
        RenderSystem.disableDepthTest(); // Allow see-through

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        Matrix4f matrix = matrixStack.last().pose();

        int rendered = 0;
        int maxRender = 8000; // Higher limit

        for (Map.Entry<BlockPos, BlockState> entry : schematic.getBlocks().entrySet()) {
            if (rendered >= maxRender)
                break;

            BlockPos worldPos = schematic.toWorldPos(entry.getKey());
            BlockState state = entry.getValue();

            // Skip if already placed correctly
            if (mc.level.getBlockState(worldPos).getBlock() == state.getBlock()) {
                continue;
            }

            // Get block color - BRIGHTER colors
            float r, g, b, a;
            a = 0.6f; // More visible!

            String blockName = state.getBlock().getRegistryName().getPath();

            // Color coding by material type
            if (blockName.contains("stone") || blockName.contains("cobble") || blockName.contains("andesite")
                    || blockName.contains("diorite") || blockName.contains("granite")) {
                r = 0.6f;
                g = 0.6f;
                b = 0.6f; // Gray
            } else if (blockName.contains("dirt") || blockName.contains("grass") || blockName.contains("podzol")) {
                r = 0.4f;
                g = 0.3f;
                b = 0.2f; // Brown
            } else if (blockName.contains("wood") || blockName.contains("log") || blockName.contains("plank")
                    || blockName.contains("oak") || blockName.contains("spruce") || blockName.contains("birch")
                    || blockName.contains("jungle") || blockName.contains("acacia") || blockName.contains("dark_oak")) {
                r = 0.7f;
                g = 0.5f;
                b = 0.3f; // Wood brown
            } else if (blockName.contains("glass")) {
                r = 0.6f;
                g = 0.9f;
                b = 1.0f;
                a = 0.4f; // Light blue
            } else if (blockName.contains("brick")) {
                r = 0.8f;
                g = 0.4f;
                b = 0.3f; // Brick red
            } else if (blockName.contains("iron") || blockName.contains("metal")) {
                r = 0.9f;
                g = 0.9f;
                b = 0.9f; // White/silver
            } else if (blockName.contains("gold")) {
                r = 1.0f;
                g = 0.85f;
                b = 0.0f; // Gold
            } else if (blockName.contains("diamond") || blockName.contains("lapis")) {
                r = 0.3f;
                g = 0.7f;
                b = 1.0f; // Blue
            } else if (blockName.contains("emerald") || blockName.contains("leaves")) {
                r = 0.2f;
                g = 0.9f;
                b = 0.3f; // Green
            } else if (blockName.contains("redstone") || blockName.contains("nether")) {
                r = 1.0f;
                g = 0.2f;
                b = 0.2f; // Red
            } else if (blockName.contains("obsidian") || blockName.contains("coal")) {
                r = 0.2f;
                g = 0.1f;
                b = 0.3f; // Dark purple
            } else if (blockName.contains("sand") || blockName.contains("sandstone")) {
                r = 0.95f;
                g = 0.9f;
                b = 0.6f; // Sandy yellow
            } else if (blockName.contains("wool") || blockName.contains("concrete")
                    || blockName.contains("terracotta")) {
                // Colorful blocks - cyan default
                r = 0.3f;
                g = 0.8f;
                b = 0.9f;
            } else if (blockName.contains("water")) {
                r = 0.2f;
                g = 0.4f;
                b = 0.9f;
                a = 0.5f;
            } else if (blockName.contains("lava")) {
                r = 1.0f;
                g = 0.5f;
                b = 0.0f;
            } else {
                // Default - bright green for unknown
                r = 0.3f;
                g = 0.95f;
                b = 0.4f;
            }

            // Render cube with outline
            renderGhostCube(buffer, matrix, worldPos, r, g, b, a);
            rendered++;
        }

        tessellator.end();

        // Render wireframes for better visibility
        RenderSystem.lineWidth(1.5f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        rendered = 0;
        for (Map.Entry<BlockPos, BlockState> entry : schematic.getBlocks().entrySet()) {
            if (rendered >= maxRender / 2)
                break; // Less wireframes

            BlockPos worldPos = schematic.toWorldPos(entry.getKey());
            BlockState state = entry.getValue();

            if (mc.level.getBlockState(worldPos).getBlock() == state.getBlock()) {
                continue;
            }

            renderWireframeCube(buffer, matrix, worldPos, 1.0f, 1.0f, 1.0f, 0.8f);
            rendered++;
        }

        tessellator.end();

        RenderSystem.enableDepthTest();
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
        float s = 0.98f;
        float o = 0.01f; // offset

        // All 6 faces
        // Bottom
        buffer.vertex(matrix, x + o, y + o, z + o).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + o, z + o).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + o, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + o, y + o, z + s).color(r, g, b, a).endVertex();

        // Top
        buffer.vertex(matrix, x + o, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + o).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + o, y + s, z + o).color(r, g, b, a).endVertex();

        // North
        buffer.vertex(matrix, x + o, y + o, z + o).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + o, y + s, z + o).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + o).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + o, z + o).color(r, g, b, a).endVertex();

        // South
        buffer.vertex(matrix, x + s, y + o, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + o, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + o, y + o, z + s).color(r, g, b, a).endVertex();

        // West
        buffer.vertex(matrix, x + o, y + o, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + o, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + o, y + s, z + o).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + o, y + o, z + o).color(r, g, b, a).endVertex();

        // East
        buffer.vertex(matrix, x + s, y + o, z + o).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + o).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + o, z + s).color(r, g, b, a).endVertex();
    }

    private static void renderWireframeCube(BufferBuilder buffer, Matrix4f matrix, BlockPos pos,
            float r, float g, float b, float a) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float s = 1.0f;

        // Bottom edges
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();

        // Top edges
        buffer.vertex(matrix, x, y + s, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + s, z).color(r, g, b, a).endVertex();

        // Vertical edges
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + s, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + s, y + s, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z + s).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + s, z + s).color(r, g, b, a).endVertex();
    }

    private static void renderBoundingBox(MatrixStack matrixStack, SchematicData schematic) {
        BlockPos origin = schematic.getOrigin();
        int w = schematic.getWidth();
        int h = schematic.getHeight();
        int l = schematic.getLength();

        RenderSystem.lineWidth(3.0f); // Thicker line
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();

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

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
    }
}
