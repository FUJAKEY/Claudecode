package com.schematicsbuilder.client;

import com.schematicsbuilder.SchematicsBuilderMod;
import com.schematicsbuilder.client.gui.SchematicMenuScreen;
import com.schematicsbuilder.schematic.SchematicData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.io.File;

/**
 * Client-side command processor
 * Handles /schem commands on client
 */
public class ClientCommands {

    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * Process chat message - intercept /schem commands
     * Returns true if command was handled
     */
    public static boolean processCommand(String message) {
        if (!message.startsWith("/schem"))
            return false;

        String[] parts = message.trim().split("\\s+");
        if (parts.length < 2) {
            showHelp();
            return true;
        }

        String cmd = parts[1].toLowerCase();

        switch (cmd) {
            case "list":
                listSchematics();
                return true;

            case "load":
                if (parts.length < 3) {
                    sendMessage("§cUsage: /schem load <filename>");
                    return true;
                }
                StringBuilder filename = new StringBuilder();
                for (int i = 2; i < parts.length; i++) {
                    if (i > 2)
                        filename.append(" ");
                    filename.append(parts[i]);
                }
                loadSchematic(filename.toString());
                return true;

            case "pos":
                setPosition();
                return true;

            case "rotate":
                rotate();
                return true;

            case "build":
                ClientAutoBuilder.getInstance().start();
                return true;

            case "stop":
                ClientAutoBuilder.getInstance().stop();
                return true;

            case "pause":
                ClientAutoBuilder.getInstance().togglePause();
                return true;

            case "status":
                showStatus();
                return true;

            case "chest":
                if (parts.length < 3) {
                    sendMessage("§cUsage: /schem chest <link|unlink|list|clear>");
                    return true;
                }
                handleChestCommand(parts[2].toLowerCase());
                return true;

            case "speed":
                if (parts.length < 3) {
                    sendMessage("§cUsage: /schem speed <1-20>");
                    return true;
                }
                try {
                    int delay = Integer.parseInt(parts[2]);
                    ClientAutoBuilder.getInstance().setSpeed(delay);
                } catch (NumberFormatException e) {
                    sendMessage("§cInvalid number");
                }
                return true;

            case "antidetect":
            case "ad":
                if (parts.length < 3) {
                    sendMessage("§cUsage: /schem antidetect <off|light|normal|paranoid>");
                    return true;
                }
                ClientAutoBuilder.getInstance().setAntiDetection(parts[2]);
                return true;

            case "materials":
            case "mat":
                showMaterials();
                return true;

            case "missing":
                showMissing();
                return true;

            case "preview":
                SchematicPreviewRenderer.togglePreview();
                return true;

            case "menu":
            case "gui":
                mc.setScreen(new SchematicMenuScreen());
                return true;

            case "help":
                showHelp();
                return true;

            default:
                sendMessage("§cUnknown command. Use /schem help");
                return true;
        }
    }

    private static void listSchematics() {
        File folder = SchematicsBuilderMod.schematicsFolder;
        if (folder == null || !folder.exists()) {
            sendMessage("§cSchematics folder not found! Will be created on restart.");
            return;
        }

        File[] files = folder.listFiles(
                (dir, name) -> name.endsWith(".schematic") || name.endsWith(".schem") || name.endsWith(".litematic"));

        sendMessage("§6═══ Schematics (" + (files != null ? files.length : 0) + ") ═══");

        if (files == null || files.length == 0) {
            sendMessage("§cNo schematics found!");
            sendMessage("§7Put files in: " + folder.getAbsolutePath());
        } else {
            for (File f : files) {
                long kb = f.length() / 1024;
                sendMessage("§e • " + f.getName() + " §7(" + kb + " KB)");
            }
        }
    }

    private static void loadSchematic(String filename) {
        File folder = SchematicsBuilderMod.schematicsFolder;
        if (folder == null) {
            sendMessage("§cError: schematics folder not initialized");
            return;
        }

        File file = new File(folder, filename);
        if (!file.exists()) {
            sendMessage("§cFile not found: " + filename);
            return;
        }

        try {
            SchematicData data = SchematicData.load(file);

            // Set origin to player position
            if (mc.player != null) {
                data.setOrigin(mc.player.blockPosition());
            }

            ClientAutoBuilder.getInstance().loadSchematic(data);

        } catch (Exception e) {
            sendMessage("§cFailed to load: " + e.getMessage());
            SchematicsBuilderMod.LOGGER.error("Failed to load schematic", e);
        }
    }

    private static void setPosition() {
        if (mc.player == null)
            return;

        SchematicData data = ClientAutoBuilder.getInstance().getSchematic();
        if (data != null) {
            data.setOrigin(mc.player.blockPosition());
            sendMessage("§a📍 Position set to: " + mc.player.blockPosition().toShortString());
        } else {
            sendMessage("§cNo schematic loaded!");
        }
    }

    private static void rotate() {
        SchematicData data = ClientAutoBuilder.getInstance().getSchematic();
        if (data != null) {
            data.rotate90();
            sendMessage("§b⟳ Rotated to " + data.getRotation() + "°");
        } else {
            sendMessage("§cNo schematic loaded!");
        }
    }

    private static void showStatus() {
        ClientAutoBuilder builder = ClientAutoBuilder.getInstance();

        sendMessage("§6═══ Build Status ═══");

        if (builder.isRunning()) {
            String status = builder.isPaused() ? "§ePAUSED" : "§aRUNNING";
            sendMessage("§7Status: " + status);
            sendMessage("§7Progress: §e" + builder.getProgress() + "% §7(" +
                    builder.getBlocksPlaced() + "/" + builder.getTotalBlocks() + ")");
            sendMessage("§7Layer: §e" + (builder.getCurrentLayer() + 1) + "/" + builder.getMaxLayer());
        } else {
            SchematicData data = builder.getSchematic();
            if (data != null) {
                sendMessage("§7Loaded: §e" + data.getName());
                sendMessage("§7Position: §e" + data.getOrigin().toShortString());
                sendMessage("§7Rotation: §e" + data.getRotation() + "°");
                sendMessage("§7Size: §e" + data.getWidth() + "x" + data.getHeight() + "x" + data.getLength());

                int mat = MaterialCalculator.getMaterialPercentage(data);
                String matColor = mat == 100 ? "§a" : (mat > 50 ? "§e" : "§c");
                sendMessage("§7Materials: " + matColor + mat + "%");
            } else {
                sendMessage("§7No schematic loaded");
                sendMessage("§7Use: /schem load <file>");
            }
        }

        sendMessage("§7" + AntiDetection.getSettingsString());
        sendMessage("§7Preview: " + (SchematicPreviewRenderer.isPreviewEnabled() ? "§aON" : "§cOFF"));
    }

    private static void showMaterials() {
        SchematicData data = ClientAutoBuilder.getInstance().getSchematic();
        if (data != null) {
            MaterialCalculator.showMaterials(data);
        } else {
            sendMessage("§cNo schematic loaded!");
        }
    }

    private static void showMissing() {
        SchematicData data = ClientAutoBuilder.getInstance().getSchematic();
        if (data != null) {
            MaterialCalculator.showMissing(data);
        } else {
            sendMessage("§cNo schematic loaded!");
        }
    }

    private static void handleChestCommand(String sub) {
        ClientAutoBuilder builder = ClientAutoBuilder.getInstance();

        switch (sub) {
            case "link":
                BlockPos pos = ClientEventHandler.getLookedAtBlock(5.0);
                if (pos == null) {
                    sendMessage("§cLook at a chest!");
                    return;
                }
                BlockState state = mc.level.getBlockState(pos);
                if (!(state.getBlock() instanceof ChestBlock) &&
                        state.getBlock() != Blocks.BARREL) {
                    sendMessage("§cNot a chest or barrel!");
                    return;
                }
                builder.linkChest(pos);
                break;

            case "unlink":
                BlockPos unlinkPos = ClientEventHandler.getLookedAtBlock(5.0);
                if (unlinkPos != null) {
                    builder.unlinkChest(unlinkPos);
                }
                break;

            case "list":
                builder.listChests();
                break;

            case "clear":
                builder.clearChests();
                break;

            default:
                sendMessage("§cUnknown: /schem chest <link|unlink|list|clear>");
        }
    }

    private static void showHelp() {
        sendMessage("§6═══ Schematics Builder v3.0 ═══");
        sendMessage("§a§lBasic Commands:");
        sendMessage("§e/schem menu §7- Open GUI menu");
        sendMessage("§e/schem list §7- List schematics");
        sendMessage("§e/schem load <file> §7- Load schematic");
        sendMessage("§e/schem pos §7- Set build position");
        sendMessage("§e/schem rotate §7- Rotate 90°");
        sendMessage("§e/schem preview §7- Toggle preview");
        sendMessage("§a§lBuilding:");
        sendMessage("§e/schem build §7- Start");
        sendMessage("§e/schem pause §7- Pause/resume");
        sendMessage("§e/schem stop §7- Stop");
        sendMessage("§e/schem status §7- Show status");
        sendMessage("§a§lMaterials:");
        sendMessage("§e/schem materials §7- Show required");
        sendMessage("§e/schem missing §7- Show missing");
        sendMessage("§a§lChests:");
        sendMessage("§e/schem chest link/list/clear");
        sendMessage("§a§lAnti-Detection:");
        sendMessage("§e/schem antidetect <off|light|normal|paranoid>");
        sendMessage("§e/schem speed <ticks> §7- Set delay");
        sendMessage("§7─────────────────────────────");
        sendMessage("§a✓ Works on ANY server!");
    }

    private static void sendMessage(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(new StringTextComponent(msg), false);
        }
    }
}
