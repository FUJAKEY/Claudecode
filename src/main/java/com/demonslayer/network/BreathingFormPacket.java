package com.demonslayer.network;

import com.demonslayer.breathing.BreathingForm;
import com.demonslayer.breathing.BreathingStyle;
import com.demonslayer.init.ModEffects;
import com.demonslayer.systems.SlayerRankSystem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for using breathing forms (Client -> Server)
 */
public class BreathingFormPacket {

    private final int styleIndex;
    private final int formIndex;

    public BreathingFormPacket(int styleIndex, int formIndex) {
        this.styleIndex = styleIndex;
        this.formIndex = formIndex;
    }

    public static void encode(BreathingFormPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.styleIndex);
        buffer.writeInt(packet.formIndex);
    }

    public static BreathingFormPacket decode(PacketBuffer buffer) {
        return new BreathingFormPacket(buffer.readInt(), buffer.readInt());
    }

    public static void handle(BreathingFormPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                BreathingStyle[] styles = BreathingStyle.values();
                if (packet.styleIndex >= 0 && packet.styleIndex < styles.length) {
                    BreathingStyle style = styles[packet.styleIndex];
                    BreathingForm[] forms = style.getForms();

                    if (packet.formIndex >= 0 && packet.formIndex < forms.length) {
                        BreathingForm form = forms[packet.formIndex];

                        // Check unlocked
                        int maxForm = SlayerRankSystem.getMaxUnlockedForm(player, packet.styleIndex);
                        if (packet.formIndex < maxForm) {
                            // Calculate power
                            float power = SlayerRankSystem.getRank(player).powerMultiplier;
                            power *= SlayerRankSystem.getBreathingMultiplier(player, packet.styleIndex);

                            if (player.hasEffect(ModEffects.TOTAL_CONCENTRATION.get())) {
                                power *= 1.5F;
                            }

                            // Execute form
                            form.execute(player.level, player, power, TextFormatting.AQUA);

                            // Add XP
                            SlayerRankSystem.addXP(player, 2);
                            SlayerRankSystem.addBreathingXP(player, packet.styleIndex, 1);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
