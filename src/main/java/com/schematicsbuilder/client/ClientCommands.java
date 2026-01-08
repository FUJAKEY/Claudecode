package com.schematicsbuilder.client;

import com.schematicsbuilder.SchematicsBuilderMod;
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
                    sendMessage("§aSpeed set to " + delay + " ticks per block");
                } catch (NumberFormatException e) {
                    sendMessage("§cInvalid number");
                }
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
                sendMessage("§e • " + f.getName());
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

        if (builder.isRunning()) {
            String status = builder.isPaused() ? " (paused)" : "";
            sendMessage("§aBuilding" + status + ": " + builder.getProgress() + "% | " +
                    builder.getBlocksPlaced() + "/" + builder.getTotalBlocks());
        } else {
            SchematicData data = builder.getSchematic();
            if (data != null) {
                sendMessage("§eLoaded: " + data.getName() + " at " + data.getOrigin().toShortString());
            } else {
                sendMessage("§7No schematic loaded");
            }
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
        sendMessage("§6═══ Schematic Builder (Client-Side) ═══");
        sendMessage("§e/schem list §7- List schematics");
        sendMessage("§e/schem load <file> §7- Load schematic");
        sendMessage("§e/schem pos §7- Set build position");
        sendMessage("§e/schem rotate §7- Rotate 90°");
        sendMessage("§e/schem build §7- Start building");
        sendMessage("§e/schem pause §7- Pause/resume");
        sendMessage("§e/schem stop §7- Stop");
        sendMessage("§e/schem speed <ticks> §7- Set speed (1-20)");
        sendMessage("§b═══ Chest Commands ═══");
        sendMessage("§e/schem chest link §7- Link chest");
        sendMessage("§e/schem chest list §7- List chests");
        sendMessage("§e/schem chest clear §7- Unlink all");
        sendMessage("§7Works on ANY server - no server mod needed!");
    }

    private static void sendMessage(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(new StringTextComponent(msg), false);
        }
    }
}
