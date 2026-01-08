package com.demonslayer.events;

import com.demonslayer.DemonSlayerMod;
import com.demonslayer.config.ModConfig;
import com.demonslayer.init.ModItems;
import com.demonslayer.network.ModNetworking;
import com.demonslayer.network.SyncPlayerDataPacket;
import com.demonslayer.systems.QuestSystem;
import com.demonslayer.systems.SlayerMarkSystem;
import com.demonslayer.systems.SlayerRankSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Global event handler for all mod mechanics
 */
@Mod.EventBusSubscriber(modid = DemonSlayerMod.MOD_ID)
public class ModEventHandler {

    /**
     * Handle demon kills for XP and quests
     */
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        LivingEntity target = event.getEntityLiving();

        if (killer instanceof PlayerEntity && !killer.level.isClientSide) {
            PlayerEntity player = (PlayerEntity) killer;
            String targetName = target.getType().getDescriptionId();

            // Check if killed a demon
            if (targetName.contains("demon") || targetName.contains("muzan") ||
                    targetName.contains("akaza") || targetName.contains("kokushibo")) {

                int xp = ModConfig.XP_PER_DEMON_KILL.get();
                xp = (int) (xp * ModConfig.XP_MULTIPLIER.get());

                // Bonus XP for bosses
                if (targetName.contains("muzan"))
                    xp *= 10;
                else if (targetName.contains("kokushibo"))
                    xp *= 5;
                else if (targetName.contains("akaza"))
                    xp *= 3;

                SlayerRankSystem.addXP(player, xp);
                SlayerMarkSystem.addDemonKill(player);

                // Quest progress
                QuestSystem.addProgress(player, QuestSystem.Quest.DEMON_HUNTER.id, 1);
                QuestSystem.addProgress(player, QuestSystem.Quest.DEMON_SLAYER.id, 1);
                QuestSystem.addProgress(player, QuestSystem.Quest.DEMON_DESTROYER.id, 1);

                if (targetName.contains("akaza") || targetName.contains("kokushibo")) {
                    QuestSystem.addProgress(player, QuestSystem.Quest.UPPER_MOON_HUNTER.id, 1);
                }

                if (targetName.contains("muzan")) {
                    QuestSystem.addProgress(player, QuestSystem.Quest.MUZAN_KILLER.id, 1);
                }

                // Sync to client
                if (player instanceof ServerPlayerEntity) {
                    syncPlayerData((ServerPlayerEntity) player);
                }
            }
        }
    }

    /**
     * Handle damage for nichirin bonus vs demons
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntityLiving();

        if (attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) attacker;
            ItemStack weapon = player.getMainHandItem();

            // Nichirin sword bonus vs demons
            if (weapon.getItem().getDescriptionId().contains("nichirin")) {
                String targetName = target.getType().getDescriptionId();
                if (targetName.contains("demon") || targetName.contains("muzan") ||
                        targetName.contains("akaza") || targetName.contains("kokushibo")) {

                    float bonus = 1.5F; // 50% bonus damage
                    event.setAmount(event.getAmount() * bonus);
                }
            }

            // Slayer Mark bonus
            if (SlayerMarkSystem.isMarkActive(player)) {
                event.setAmount(event.getAmount() * 1.5F);
            }
        }
    }

    /**
     * Player tick for mark activation check
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (event.player.level.isClientSide)
            return;

        PlayerEntity player = event.player;

        // Check for slayer mark activation
        if (ModConfig.SLAYER_MARK_ENABLED.get()) {
            SlayerMarkSystem.checkMarkActivation(player);
        }

        // Hashira rank check
        SlayerRankSystem.Rank rank = SlayerRankSystem.getRank(player);
        if (rank == SlayerRankSystem.Rank.KINOE) {
            QuestSystem.addProgress(player, QuestSystem.Quest.HASHIRA_CANDIDATE.id, 1);
        }

        // Sync data periodically (every 5 seconds)
        if (player instanceof ServerPlayerEntity && player.tickCount % 100 == 0) {
            syncPlayerData((ServerPlayerEntity) player);
        }
    }

    /**
     * Sync player join
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

            // Send welcome message
            player.displayClientMessage(
                    new StringTextComponent("═════════════════════════════")
                            .withStyle(TextFormatting.RED),
                    false);
            player.displayClientMessage(
                    new StringTextComponent("  鬼滅の刃 Demon Slayer Mod v4.0")
                            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD),
                    false);
            player.displayClientMessage(
                    new StringTextComponent("  Press K to view stats, J for quests")
                            .withStyle(TextFormatting.GRAY),
                    false);
            player.displayClientMessage(
                    new StringTextComponent("═════════════════════════════")
                            .withStyle(TextFormatting.RED),
                    false);

            // Sync data
            syncPlayerData(player);
        }
    }

    /**
     * Sync data on respawn
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            syncPlayerData((ServerPlayerEntity) event.getPlayer());
        }
    }

    /**
     * Sync player data to client
     */
    private static void syncPlayerData(ServerPlayerEntity player) {
        CompoundNBT data = player.getPersistentData();

        int xp = data.getInt("demonslayer_xp");
        int rank = data.getInt("demonslayer_rank");
        boolean mark = data.getBoolean("slayer_mark_active");

        int[] levels = new int[7];
        String[] keys = { "water_breathing_lvl", "flame_breathing_lvl", "thunder_breathing_lvl",
                "wind_breathing_lvl", "mist_breathing_lvl", "love_breathing_lvl", "sun_breathing_lvl" };
        for (int i = 0; i < keys.length; i++) {
            levels[i] = data.getInt(keys[i]);
        }

        ModNetworking.sendToPlayer(new SyncPlayerDataPacket(xp, rank, mark, levels), player);
    }
}
