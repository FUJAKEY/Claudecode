package com.arcanemagic.event;

import com.arcanemagic.ArcaneMagicMod;
import com.arcanemagic.capability.ManaCapability;
import com.arcanemagic.capability.ManaProvider;
import com.arcanemagic.network.ManaPacket;
import com.arcanemagic.network.NetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

/**
 * Event handlers for mana system
 */
public class ManaEvents {

    public static final ResourceLocation MANA_CAP_ID = new ResourceLocation(ArcaneMagicMod.MOD_ID, "mana");

    private int tickCounter = 0;

    /**
     * Attach mana capability to players
     */
    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getObject();
            if (!player.getCapability(ManaCapability.getCapability()).isPresent()) {
                event.addCapability(MANA_CAP_ID, new ManaProvider(player));
            }
        }
    }

    /**
     * Preserve mana on death/respawn
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath())
            return;

        event.getOriginal().getCapability(ManaCapability.getCapability()).ifPresent(oldMana -> {
            event.getPlayer().getCapability(ManaCapability.getCapability()).ifPresent(newMana -> {
                newMana.copyFrom(oldMana);
            });
        });
    }

    /**
     * Tick handler for mana regeneration
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (event.player.level.isClientSide)
            return;

        tickCounter++;

        // Regenerate mana every second (20 ticks)
        if (tickCounter >= 20) {
            tickCounter = 0;

            event.player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
                int oldMana = mana.getMana();
                mana.regenerateMana(mana.getManaRegenRate());

                // Sync to client if mana changed
                if (oldMana != mana.getMana() && event.player instanceof ServerPlayerEntity) {
                    syncManaToClient((ServerPlayerEntity) event.player, mana);
                }
            });
        }
    }

    /**
     * Sync mana when player logs in
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
                syncManaToClient(player, mana);
            });
        }
    }

    /**
     * Sync mana when player respawns
     */
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
                syncManaToClient(player, mana);
            });
        }
    }

    /**
     * Sync mana when player changes dimension
     */
    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            player.getCapability(ManaCapability.getCapability()).ifPresent(mana -> {
                syncManaToClient(player, mana);
            });
        }
    }

    /**
     * Send mana data to client
     */
    public static void syncManaToClient(ServerPlayerEntity player, ManaCapability mana) {
        NetworkHandler.getChannel().send(
                PacketDistributor.PLAYER.with(() -> player),
                new ManaPacket(mana.getMana(), mana.getMaxMana()));
    }
}
