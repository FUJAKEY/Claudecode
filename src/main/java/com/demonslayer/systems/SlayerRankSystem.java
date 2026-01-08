package com.demonslayer.systems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Slayer Rank System - Progress from Mizunoto to Hashira
 */
public class SlayerRankSystem {
    
    public enum Rank {
        MIZUNOTO("癸 Mizunoto", 0, 1.0F, TextFormatting.GRAY),
        MIZUNOE("壬 Mizunoe", 100, 1.1F, TextFormatting.GRAY),
        KANOTO("辛 Kanoto", 250, 1.2F, TextFormatting.WHITE),
        KANOE("庚 Kanoe", 500, 1.3F, TextFormatting.WHITE),
        TSUCHINOTO("己 Tsuchinoto", 800, 1.4F, TextFormatting.YELLOW),
        TSUCHINOE("戊 Tsuchinoe", 1200, 1.5F, TextFormatting.YELLOW),
        HINOTO("丁 Hinoto", 1700, 1.6F, TextFormatting.GOLD),
        HINOE("丙 Hinoe", 2300, 1.7F, TextFormatting.GOLD),
        KINOTO("乙 Kinoto", 3000, 1.8F, TextFormatting.RED),
        KINOE("甲 Kinoe", 4000, 1.9F, TextFormatting.RED),
        HASHIRA("柱 Hashira", 5000, 2.5F, TextFormatting.LIGHT_PURPLE);
        
        public final String name;
        public final int requiredXP;
        public final float powerMultiplier;
        public final TextFormatting color;
        
        Rank(String name, int requiredXP, float powerMultiplier, TextFormatting color) {
            this.name = name;
            this.requiredXP = requiredXP;
            this.powerMultiplier = powerMultiplier;
            this.color = color;
        }
    }
    
    private static final String XP_KEY = "demonslayer_xp";
    private static final String RANK_KEY = "demonslayer_rank";
    private static final String WATER_LVL = "water_breathing_lvl";
    private static final String FLAME_LVL = "flame_breathing_lvl";
    private static final String THUNDER_LVL = "thunder_breathing_lvl";
    private static final String WIND_LVL = "wind_breathing_lvl";
    private static final String MIST_LVL = "mist_breathing_lvl";
    private static final String LOVE_LVL = "love_breathing_lvl";
    private static final String SUN_LVL = "sun_breathing_lvl";
    
    public static int getXP(PlayerEntity player) {
        return player.getPersistentData().getInt(XP_KEY);
    }
    
    public static void addXP(PlayerEntity player, int amount) {
        CompoundNBT data = player.getPersistentData();
        int currentXP = data.getInt(XP_KEY) + amount;
        data.putInt(XP_KEY, currentXP);
        
        // Check for rank up
        Rank oldRank = getRank(player);
        Rank newRank = calculateRank(currentXP);
        
        if (newRank.ordinal() > oldRank.ordinal()) {
            data.putInt(RANK_KEY, newRank.ordinal());
            notifyRankUp(player, newRank);
        }
    }
    
    public static Rank getRank(PlayerEntity player) {
        int rankOrdinal = player.getPersistentData().getInt(RANK_KEY);
        Rank[] ranks = Rank.values();
        if (rankOrdinal >= 0 && rankOrdinal < ranks.length) {
            return ranks[rankOrdinal];
        }
        return Rank.MIZUNOTO;
    }
    
    private static Rank calculateRank(int xp) {
        Rank[] ranks = Rank.values();
        for (int i = ranks.length - 1; i >= 0; i--) {
            if (xp >= ranks[i].requiredXP) {
                return ranks[i];
            }
        }
        return Rank.MIZUNOTO;
    }
    
    private static void notifyRankUp(PlayerEntity player, Rank newRank) {
        player.displayClientMessage(
            new StringTextComponent("═══════════════════════")
                .withStyle(TextFormatting.GOLD), false);
        player.displayClientMessage(
            new StringTextComponent("  ⚔ RANK UP! ⚔")
                .withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD), false);
        player.displayClientMessage(
            new StringTextComponent("  " + newRank.name)
                .withStyle(newRank.color).withStyle(TextFormatting.BOLD), false);
        player.displayClientMessage(
            new StringTextComponent("  Power: +" + (int)((newRank.powerMultiplier - 1) * 100) + "%")
                .withStyle(TextFormatting.GREEN), false);
        player.displayClientMessage(
            new StringTextComponent("═══════════════════════")
                .withStyle(TextFormatting.GOLD), false);
        
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
            net.minecraft.util.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 
            net.minecraft.util.SoundCategory.PLAYERS, 1.5F, 1.0F);
    }
    
    // Breathing Style Levels
    public static int getBreathingLevel(PlayerEntity player, int styleIndex) {
        String[] keys = {WATER_LVL, FLAME_LVL, THUNDER_LVL, WIND_LVL, MIST_LVL, LOVE_LVL, SUN_LVL};
        if (styleIndex >= 0 && styleIndex < keys.length) {
            return player.getPersistentData().getInt(keys[styleIndex]);
        }
        return 0;
    }
    
    public static void addBreathingXP(PlayerEntity player, int styleIndex, int amount) {
        String[] keys = {WATER_LVL, FLAME_LVL, THUNDER_LVL, WIND_LVL, MIST_LVL, LOVE_LVL, SUN_LVL};
        String[] names = {"Water", "Flame", "Thunder", "Wind", "Mist", "Love", "Sun"};
        
        if (styleIndex < 0 || styleIndex >= keys.length) return;
        
        CompoundNBT data = player.getPersistentData();
        int currentXP = data.getInt(keys[styleIndex] + "_xp") + amount;
        data.putInt(keys[styleIndex] + "_xp", currentXP);
        
        int oldLevel = data.getInt(keys[styleIndex]);
        int newLevel = calculateBreathingLevel(currentXP);
        
        if (newLevel > oldLevel && newLevel <= 6) {
            data.putInt(keys[styleIndex], newLevel);
            
            player.displayClientMessage(
                new StringTextComponent("★ " + names[styleIndex] + " Breathing Level " + newLevel + " ★")
                    .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
            
            if (newLevel <= 6) {
                player.displayClientMessage(
                    new StringTextComponent("  Unlocked Form " + newLevel + "!")
                        .withStyle(TextFormatting.GREEN), false);
            }
        }
    }
    
    private static int calculateBreathingLevel(int xp) {
        int[] thresholds = {0, 20, 50, 100, 180, 300, 500};
        for (int i = thresholds.length - 1; i >= 0; i--) {
            if (xp >= thresholds[i]) {
                return i;
            }
        }
        return 0;
    }
    
    public static float getBreathingMultiplier(PlayerEntity player, int styleIndex) {
        int level = getBreathingLevel(player, styleIndex);
        return 1.0F + (level * 0.1F); // 10% bonus per level
    }
    
    public static int getMaxUnlockedForm(PlayerEntity player, int styleIndex) {
        return Math.min(6, getBreathingLevel(player, styleIndex) + 1);
    }
}
