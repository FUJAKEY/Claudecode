package com.demonslayer.network;

import com.demonslayer.systems.QuestSystem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for starting a quest (Client -> Server)
 */
public class StartQuestPacket {

    private final int questId;

    public StartQuestPacket(int questId) {
        this.questId = questId;
    }

    public static void encode(StartQuestPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.questId);
    }

    public static StartQuestPacket decode(PacketBuffer buffer) {
        return new StartQuestPacket(buffer.readInt());
    }

    public static void handle(StartQuestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                QuestSystem.Quest[] quests = QuestSystem.Quest.values();
                if (packet.questId >= 0 && packet.questId < quests.length) {
                    QuestSystem.startQuest(player, quests[packet.questId]);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
