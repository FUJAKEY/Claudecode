package com.infinitygauntlet.abilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Stone Leveling System
 * Stones get stronger with use
 */
public class StoneLevelSystem {
    
    private static final String[] STONE_KEYS = {
        "space_stone", "time_stone", "reality_stone",
        "power_stone", "mind_stone", "soul_stone"
    };
    
    private static final int[] XP_THRESHOLDS = {0, 50, 150, 300, 500, 750, 1000};
    private static final String[] LEVEL_NAMES = {
        "Dormant", "Awakened", "Empowered", "Mastered", "Transcendent", "Infinite", "Omnipotent"
    };
    
    /**
     * Add XP to a stone and return new level if leveled up
     */
    public static int addStoneXP(CompoundNBT nbt, int stoneIndex, int xp) {
        String xpKey = STONE_KEYS[stoneIndex] + "_xp";
        String levelKey = STONE_KEYS[stoneIndex] + "_level";
        
        int currentXP = nbt.getInt(xpKey) + xp;
        int currentLevel = nbt.getInt(levelKey);
        
        nbt.putInt(xpKey, currentXP);
        
        // Check for level up
        int newLevel = calculateLevel(currentXP);
        if (newLevel > currentLevel) {
            nbt.putInt(levelKey, newLevel);
            return newLevel; // Leveled up!
        }
        
        return -1; // No level up
    }
    
    /**
     * Get stone level
     */
    public static int getStoneLevel(CompoundNBT nbt, int stoneIndex) {
        String levelKey = STONE_KEYS[stoneIndex] + "_level";
        return nbt.getInt(levelKey);
    }
    
    /**
     * Get power multiplier based on stone level
     */
    public static float getLevelMultiplier(CompoundNBT nbt, int stoneIndex) {
        int level = getStoneLevel(nbt, stoneIndex);
        return 1.0F + (level * 0.15F); // 15% bonus per level
    }
    
    /**
     * Calculate level from XP
     */
    private static int calculateLevel(int xp) {
        for (int i = XP_THRESHOLDS.length - 1; i >= 0; i--) {
            if (xp >= XP_THRESHOLDS[i]) {
                return i;
            }
        }
        return 0;
    }
    
    /**
     * Get level name
     */
    public static String getLevelName(int level) {
        if (level >= 0 && level < LEVEL_NAMES.length) {
            return LEVEL_NAMES[level];
        }
        return LEVEL_NAMES[0];
    }
    
    /**
     * Get XP to next level
     */
    public static int getXPToNextLevel(CompoundNBT nbt, int stoneIndex) {
        String xpKey = STONE_KEYS[stoneIndex] + "_xp";
        int currentXP = nbt.getInt(xpKey);
        int currentLevel = calculateLevel(currentXP);
        
        if (currentLevel >= XP_THRESHOLDS.length - 1) {
            return 0; // Max level
        }
        
        return XP_THRESHOLDS[currentLevel + 1] - currentXP;
    }
    
    /**
     * Get current XP
     */
    public static int getStoneXP(CompoundNBT nbt, int stoneIndex) {
        String xpKey = STONE_KEYS[stoneIndex] + "_xp";
        return nbt.getInt(xpKey);
    }
    
    /**
     * Notify player about level up
     */
    public static void notifyLevelUp(PlayerEntity player, int stoneIndex, int newLevel) {
        String[] stoneNames = {"Space", "Time", "Reality", "Power", "Mind", "Soul"};
        TextFormatting[] colors = {
            TextFormatting.BLUE, TextFormatting.GREEN, TextFormatting.RED,
            TextFormatting.DARK_PURPLE, TextFormatting.YELLOW, TextFormatting.GOLD
        };
        
        player.displayClientMessage(
            new StringTextComponent("★ " + stoneNames[stoneIndex] + " Stone LEVEL UP! ★ ")
                .withStyle(colors[stoneIndex]).withStyle(TextFormatting.BOLD)
                .append(new StringTextComponent(getLevelName(newLevel))
                    .withStyle(TextFormatting.WHITE)), false);
    }
}
