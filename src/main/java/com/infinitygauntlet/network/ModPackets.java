package com.infinitygauntlet.network;

import com.infinitygauntlet.InfinityGauntletMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ModPackets {
    
    private static final String PROTOCOL_VERSION = "1";
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(InfinityGauntletMod.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    private static int packetId = 0;
    
    public static void register() {
        CHANNEL.registerMessage(
            packetId++,
            UseAbilityPacket.class,
            UseAbilityPacket::encode,
            UseAbilityPacket::decode,
            UseAbilityPacket::handle
        );
    }
    
    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}
