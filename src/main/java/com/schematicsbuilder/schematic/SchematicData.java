package com.schematicsbuilder.schematic;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Schematic data container and parser
 * Supports both .schematic and .litematic formats
 */
public class SchematicData {

    private final String name;
    private final int width, height, length;
    private final Map<BlockPos, BlockState> blocks;
    private BlockPos origin = BlockPos.ZERO;
    private int rotation = 0; // 0, 90, 180, 270

    public SchematicData(String name, int width, int height, int length) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = new HashMap<>();
    }

    public void addBlock(BlockPos pos, BlockState state) {
        if (!state.isAir()) {
            blocks.put(pos.immutable(), state);
        }
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public int getBlockCount() {
        return blocks.size();
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public void setOrigin(BlockPos origin) {
        this.origin = origin;
    }

    public int getRotation() {
        return rotation;
    }

    public void rotate90() {
        rotation = (rotation + 90) % 360;
    }

    public Map<BlockPos, BlockState> getBlocks() {
        return blocks;
    }

    /**
     * Get blocks for a specific Y layer (for layer-by-layer building)
     */
    public List<Map.Entry<BlockPos, BlockState>> getBlocksAtLayer(int y) {
        List<Map.Entry<BlockPos, BlockState>> layer = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            if (entry.getKey().getY() == y) {
                layer.add(entry);
            }
        }
        // Sort by X then Z for optimal path
        layer.sort((a, b) -> {
            int cmp = Integer.compare(a.getKey().getX(), b.getKey().getX());
            return cmp != 0 ? cmp : Integer.compare(a.getKey().getZ(), b.getKey().getZ());
        });
        return layer;
    }

    /**
     * Get world position for a schematic block (with offset and rotation)
     */
    public BlockPos toWorldPos(BlockPos schematicPos) {
        int x = schematicPos.getX();
        int y = schematicPos.getY();
        int z = schematicPos.getZ();

        // Apply rotation around center
        int cx = width / 2;
        int cz = length / 2;
        int rx = x - cx;
        int rz = z - cz;

        switch (rotation) {
            case 90:
                int temp = rx;
                rx = -rz;
                rz = temp;
                break;
            case 180:
                rx = -rx;
                rz = -rz;
                break;
            case 270:
                temp = rx;
                rx = rz;
                rz = -temp;
                break;
        }

        x = rx + cx;
        z = rz + cz;

        return origin.offset(x, y, z);
    }

    /**
     * Load .schematic file (MCEdit/WorldEdit format)
     */
    public static SchematicData loadSchematic(File file) throws Exception {
        CompoundNBT nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));

        short width = nbt.getShort("Width");
        short height = nbt.getShort("Height");
        short length = nbt.getShort("Length");

        String name = file.getName().replace(".schematic", "");
        SchematicData data = new SchematicData(name, width, height, length);

        // Get block data
        byte[] blockIds = nbt.getByteArray("Blocks");
        byte[] blockData = nbt.getByteArray("Data");

        ListNBT palette = nbt.getList("Palette", 10);
        Map<Integer, BlockState> paletteMap = new HashMap<>();

        if (palette != null && !palette.isEmpty()) {
            // New format with palette
            for (int i = 0; i < palette.size(); i++) {
                CompoundNBT entry = palette.getCompound(i);
                BlockState state = NBTUtil.readBlockState(entry);
                paletteMap.put(i, state);
            }
        }

        // Parse blocks
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    if (index < blockIds.length) {
                        int id = blockIds[index] & 0xFF;
                        if (id != 0) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = paletteMap.getOrDefault(id, null);
                            if (state != null && !state.isAir()) {
                                data.addBlock(pos, state);
                            }
                        }
                    }
                    index++;
                }
            }
        }

        return data;
    }

    /**
     * Load .litematic file (Litematica format)
     */
    public static SchematicData loadLitematic(File file) throws Exception {
        CompoundNBT nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));

        String name = file.getName().replace(".litematic", "");

        // Get metadata
        CompoundNBT metadata = nbt.getCompound("Metadata");
        CompoundNBT enclosingSize = metadata.getCompound("EnclosingSize");
        int width = enclosingSize.getInt("x");
        int height = enclosingSize.getInt("y");
        int length = enclosingSize.getInt("z");

        SchematicData data = new SchematicData(name, Math.abs(width), Math.abs(height), Math.abs(length));

        // Get regions
        CompoundNBT regions = nbt.getCompound("Regions");

        for (String regionName : regions.getAllKeys()) {
            CompoundNBT region = regions.getCompound(regionName);

            CompoundNBT posNBT = region.getCompound("Position");
            int offsetX = posNBT.getInt("x");
            int offsetY = posNBT.getInt("y");
            int offsetZ = posNBT.getInt("z");

            CompoundNBT sizeNBT = region.getCompound("Size");
            int sizeX = Math.abs(sizeNBT.getInt("x"));
            int sizeY = Math.abs(sizeNBT.getInt("y"));
            int sizeZ = Math.abs(sizeNBT.getInt("z"));

            // Get palette
            ListNBT blockStatePalette = region.getList("BlockStatePalette", 10);
            List<BlockState> palette = new ArrayList<>();
            for (int i = 0; i < blockStatePalette.size(); i++) {
                CompoundNBT stateNBT = blockStatePalette.getCompound(i);
                BlockState state = NBTUtil.readBlockState(stateNBT);
                palette.add(state);
            }

            // Get block states (long array packed)
            long[] blockStates = region.getLongArray("BlockStates");
            int bits = Math.max(2, (int) Math.ceil(Math.log(palette.size()) / Math.log(2)));
            long mask = (1L << bits) - 1;

            int totalBlocks = sizeX * sizeY * sizeZ;
            int index = 0;

            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    for (int x = 0; x < sizeX; x++) {
                        if (index < totalBlocks && blockStates.length > 0) {
                            int bitIndex = index * bits;
                            int longIndex = bitIndex / 64;
                            int bitOffset = bitIndex % 64;

                            if (longIndex < blockStates.length) {
                                int paletteIndex;
                                if (bitOffset + bits <= 64) {
                                    paletteIndex = (int) ((blockStates[longIndex] >> bitOffset) & mask);
                                } else {
                                    int part1 = (int) ((blockStates[longIndex] >> bitOffset) & mask);
                                    int part2 = (int) ((blockStates[longIndex + 1] << (64 - bitOffset)) & mask);
                                    paletteIndex = part1 | part2;
                                }

                                if (paletteIndex >= 0 && paletteIndex < palette.size()) {
                                    BlockState state = palette.get(paletteIndex);
                                    if (!state.isAir()) {
                                        BlockPos pos = new BlockPos(x + offsetX, y + offsetY, z + offsetZ);
                                        data.addBlock(pos, state);
                                    }
                                }
                            }
                        }
                        index++;
                    }
                }
            }
        }

        return data;
    }

    /**
     * Auto-detect format and load
     */
    public static SchematicData load(File file) throws Exception {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".litematic")) {
            return loadLitematic(file);
        } else if (name.endsWith(".schematic") || name.endsWith(".schem")) {
            return loadSchematic(file);
        }
        throw new IllegalArgumentException("Unknown format: " + name);
    }
}
