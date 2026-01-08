package com.schematicsbuilder.client;

import com.schematicsbuilder.schematic.SchematicData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

/**
 * Calculates and displays required materials for schematic
 */
public class MaterialCalculator {

    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * Calculate all required materials for schematic
     */
    public static Map<Block, Integer> calculateMaterials(SchematicData schematic) {
        Map<Block, Integer> materials = new LinkedHashMap<>();

        for (Map.Entry<BlockPos, BlockState> entry : schematic.getBlocks().entrySet()) {
            Block block = entry.getValue().getBlock();
            materials.merge(block, 1, Integer::sum);
        }

        // Sort by count descending
        List<Map.Entry<Block, Integer>> sorted = new ArrayList<>(materials.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        Map<Block, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Block, Integer> entry : sorted) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /**
     * Calculate missing materials (what player doesn't have)
     */
    public static Map<Block, Integer> calculateMissing(SchematicData schematic) {
        Map<Block, Integer> required = calculateMaterials(schematic);
        Map<Block, Integer> missing = new LinkedHashMap<>();

        if (mc.player == null)
            return required;

        // Count player inventory
        Map<Block, Integer> inventory = new HashMap<>();
        for (int i = 0; i < mc.player.inventory.getContainerSize(); i++) {
            ItemStack stack = mc.player.inventory.getItem(i);
            if (!stack.isEmpty() && Block.byItem(stack.getItem()) != null) {
                Block block = Block.byItem(stack.getItem());
                if (block != null) {
                    inventory.merge(block, stack.getCount(), Integer::sum);
                }
            }
        }

        // Calculate missing
        for (Map.Entry<Block, Integer> entry : required.entrySet()) {
            int have = inventory.getOrDefault(entry.getKey(), 0);
            int need = entry.getValue() - have;
            if (need > 0) {
                missing.put(entry.getKey(), need);
            }
        }

        return missing;
    }

    /**
     * Display materials in chat
     */
    public static void showMaterials(SchematicData schematic) {
        Map<Block, Integer> materials = calculateMaterials(schematic);

        sendMessage("§6═══════════════════════════════════");
        sendMessage("§6  📦 Materials for: §e" + schematic.getName());
        sendMessage("§6═══════════════════════════════════");

        int totalBlocks = 0;
        int uniqueTypes = 0;

        for (Map.Entry<Block, Integer> entry : materials.entrySet()) {
            String name = entry.getKey().getName().getString();
            int count = entry.getValue();
            int stacks = count / 64;
            int remainder = count % 64;

            String stackInfo = "";
            if (stacks > 0) {
                stackInfo = " §7(" + stacks + " stacks";
                if (remainder > 0)
                    stackInfo += " + " + remainder;
                stackInfo += ")";
            }

            sendMessage("§e • " + name + ": §f" + count + stackInfo);

            totalBlocks += count;
            uniqueTypes++;

            // Limit display
            if (uniqueTypes >= 20) {
                int remaining = materials.size() - 20;
                if (remaining > 0) {
                    sendMessage("§7   ... and " + remaining + " more types");
                }
                break;
            }
        }

        sendMessage("§6═══════════════════════════════════");
        sendMessage("§a  Total: §f" + totalBlocks + " §ablocks, §f" + materials.size() + " §atypes");
        sendMessage("§6═══════════════════════════════════");
    }

    /**
     * Display missing materials
     */
    public static void showMissing(SchematicData schematic) {
        Map<Block, Integer> missing = calculateMissing(schematic);

        if (missing.isEmpty()) {
            sendMessage("§a✓ You have all required materials!");
            return;
        }

        sendMessage("§c═══ Missing Materials ═══");

        int totalMissing = 0;
        int shown = 0;

        for (Map.Entry<Block, Integer> entry : missing.entrySet()) {
            String name = entry.getKey().getName().getString();
            int count = entry.getValue();
            int stacks = count / 64;

            String stackInfo = stacks > 0 ? " §7(" + stacks + "+ stacks)" : "";
            sendMessage("§c • " + name + ": §f" + count + stackInfo);

            totalMissing += count;
            shown++;

            if (shown >= 15) {
                int remaining = missing.size() - 15;
                if (remaining > 0) {
                    sendMessage("§7   ... and " + remaining + " more");
                }
                break;
            }
        }

        sendMessage("§c  Total missing: §f" + totalMissing + " blocks");
    }

    /**
     * Get percentage of materials player has
     */
    public static int getMaterialPercentage(SchematicData schematic) {
        Map<Block, Integer> required = calculateMaterials(schematic);
        Map<Block, Integer> missing = calculateMissing(schematic);

        int totalRequired = required.values().stream().mapToInt(Integer::intValue).sum();
        int totalMissing = missing.values().stream().mapToInt(Integer::intValue).sum();

        if (totalRequired == 0)
            return 100;

        int have = totalRequired - totalMissing;
        return (have * 100) / totalRequired;
    }

    private static void sendMessage(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(new StringTextComponent(msg), false);
        }
    }
}
