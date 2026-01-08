package com.demonslayer.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for spawning particles on client (Server -> Client)
 */
public class ParticleEffectPacket {

    private final double x, y, z;
    private final int particleType;
    private final int count;
    private final float spreadX, spreadY, spreadZ;
    private final float speed;

    public ParticleEffectPacket(double x, double y, double z, int particleType,
            int count, float spreadX, float spreadY, float spreadZ, float speed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.particleType = particleType;
        this.count = count;
        this.spreadX = spreadX;
        this.spreadY = spreadY;
        this.spreadZ = spreadZ;
        this.speed = speed;
    }

    public static void encode(ParticleEffectPacket packet, PacketBuffer buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeInt(packet.particleType);
        buffer.writeInt(packet.count);
        buffer.writeFloat(packet.spreadX);
        buffer.writeFloat(packet.spreadY);
        buffer.writeFloat(packet.spreadZ);
        buffer.writeFloat(packet.speed);
    }

    public static ParticleEffectPacket decode(PacketBuffer buffer) {
        return new ParticleEffectPacket(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readInt(), buffer.readInt(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(ParticleEffectPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = Minecraft.getInstance().level;
            if (world != null) {
                IParticleData particle = getParticleType(packet.particleType);
                for (int i = 0; i < packet.count; i++) {
                    double px = packet.x + (world.random.nextDouble() - 0.5) * packet.spreadX * 2;
                    double py = packet.y + (world.random.nextDouble() - 0.5) * packet.spreadY * 2;
                    double pz = packet.z + (world.random.nextDouble() - 0.5) * packet.spreadZ * 2;
                    double vx = (world.random.nextDouble() - 0.5) * packet.speed;
                    double vy = (world.random.nextDouble() - 0.5) * packet.speed;
                    double vz = (world.random.nextDouble() - 0.5) * packet.speed;
                    world.addParticle(particle, px, py, pz, vx, vy, vz);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static IParticleData getParticleType(int type) {
        switch (type) {
            case 0:
                return ParticleTypes.SPLASH;
            case 1:
                return ParticleTypes.FLAME;
            case 2:
                return ParticleTypes.ENCHANTED_HIT;
            case 3:
                return ParticleTypes.CLOUD;
            case 4:
                return ParticleTypes.ITEM_SNOWBALL;
            case 5:
                return ParticleTypes.HEART;
            case 6:
                return ParticleTypes.END_ROD;
            case 7:
                return ParticleTypes.CRIT;
            case 8:
                return ParticleTypes.CRIMSON_SPORE;
            case 9:
                return ParticleTypes.SOUL_FIRE_FLAME;
            default:
                return ParticleTypes.SWEEP_ATTACK;
        }
    }

    // Helper constants for particle types
    public static final int WATER = 0;
    public static final int FLAME = 1;
    public static final int THUNDER = 2;
    public static final int WIND = 3;
    public static final int MIST = 4;
    public static final int LOVE = 5;
    public static final int SUN = 6;
    public static final int BEAST = 7;
    public static final int DEMON = 8;
    public static final int DARK = 9;
}
