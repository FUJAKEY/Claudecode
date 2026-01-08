package com.demonslayer.systems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import com.demonslayer.init.ModItems;

/**
 * Quest System - Missions from the Demon Slayer Corps
 */
public class QuestSystem {
    
    public enum Quest {
        DEMON_HUNTER("Demon Hunter", "Kill 10 demons", 10, 50, 0),
        DEMON_SLAYER("Demon Slayer", "Kill 25 demons", 25, 150, 1),
        DEMON_DESTROYER("Demon Destroyer", "Kill 50 demons", 50, 400, 2),
        UPPER_MOON_HUNTER("Upper Moon Hunter", "Kill an Upper Moon demon", 1, 500, 3),
        MUZAN_KILLER("Muzan Killer", "Defeat Muzan Kibutsuji", 1, 1000, 4),
        BREATHING_MASTER("Breathing Master", "Use breathing forms 100 times", 100, 300, 5),
        HASHIRA_CANDIDATE("Hashira Candidate", "Reach Kinoe rank", 1, 500, 6);
        
        public final String name;
        public final String description;
        public final int target;
        public final int xpReward;
        public final int id;
        
        Quest(String name, String description, int target, int xpReward, int id) {
            this.name = name;
            this.description = description;
            this.target = target;
            this.xpReward = xpReward;
            this.id = id;
        }
    }
    
    private static final String QUEST_PREFIX = "quest_";
    private static final String QUEST_PROGRESS_PREFIX = "quest_progress_";
    private static final String ACTIVE_QUEST = "active_quest";
    
    /**
     * Start a quest
     */
    public static boolean startQuest(PlayerEntity player, Quest quest) {
        CompoundNBT data = player.getPersistentData();
        
        // Check if already completed
        if (data.getBoolean(QUEST_PREFIX + quest.id)) {
            player.displayClientMessage(
                new StringTextComponent("Quest already completed!")
                    .withStyle(TextFormatting.RED), true);
            return false;
        }
        
        data.putInt(ACTIVE_QUEST, quest.id);
        data.putInt(QUEST_PROGRESS_PREFIX + quest.id, 0);
        
        player.displayClientMessage(
            new StringTextComponent("═══════════════════════")
                .withStyle(TextFormatting.GOLD), false);
        player.displayClientMessage(
            new StringTextComponent("  📜 NEW QUEST: " + quest.name)
                .withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD), false);
        player.displayClientMessage(
            new StringTextComponent("  " + quest.description)
                .withStyle(TextFormatting.WHITE), false);
        player.displayClientMessage(
            new StringTextComponent("  Reward: " + quest.xpReward + " XP")
                .withStyle(TextFormatting.GREEN), false);
        player.displayClientMessage(
            new StringTextComponent("═══════════════════════")
                .withStyle(TextFormatting.GOLD), false);
        
        return true;
    }
    
    /**
     * Add progress to current quest
     */
    public static void addProgress(PlayerEntity player, int questId, int amount) {
        CompoundNBT data = player.getPersistentData();
        int activeQuest = data.getInt(ACTIVE_QUEST);
        
        if (activeQuest != questId) return;
        
        Quest quest = getQuestById(questId);
        if (quest == null) return;
        
        int progress = data.getInt(QUEST_PROGRESS_PREFIX + questId) + amount;
        data.putInt(QUEST_PROGRESS_PREFIX + questId, progress);
        
        if (progress >= quest.target) {
            completeQuest(player, quest);
        } else {
            player.displayClientMessage(
                new StringTextComponent("Quest Progress: " + progress + "/" + quest.target)
                    .withStyle(TextFormatting.YELLOW), true);
        }
    }
    
    /**
     * Complete a quest and give rewards
     */
    private static void completeQuest(PlayerEntity player, Quest quest) {
        CompoundNBT data = player.getPersistentData();
        data.putBoolean(QUEST_PREFIX + quest.id, true);
        data.putInt(ACTIVE_QUEST, -1);
        
        // Give XP reward
        SlayerRankSystem.addXP(player, quest.xpReward);
        
        // Special rewards
        switch (quest) {
            case DEMON_DESTROYER:
                player.inventory.add(new ItemStack(ModItems.BREATHING_SCROLL.get()));
                break;
            case UPPER_MOON_HUNTER:
                player.inventory.add(new ItemStack(ModItems.SCARLET_CRIMSON_INGOT.get(), 5));
                break;
            case MUZAN_KILLER:
                player.inventory.add(new ItemStack(ModItems.NICHIRIN_SWORD_BLACK.get()));
                break;
            case HASHIRA_CANDIDATE:
                player.inventory.add(new ItemStack(ModItems.HASHIRA_BADGE.get()));
                break;
        }
        
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.5F, 1.0F);
        
        player.displayClientMessage(
            new StringTextComponent("═══════════════════════")
                .withStyle(TextFormatting.GREEN), false);
        player.displayClientMessage(
            new StringTextComponent("  ✓ QUEST COMPLETE!")
                .withStyle(TextFormatting.GREEN).withStyle(TextFormatting.BOLD), false);
        player.displayClientMessage(
            new StringTextComponent("  " + quest.name)
                .withStyle(TextFormatting.WHITE), false);
        player.displayClientMessage(
            new StringTextComponent("  +" + quest.xpReward + " XP")
                .withStyle(TextFormatting.YELLOW), false);
        player.displayClientMessage(
            new StringTextComponent("═══════════════════════")
                .withStyle(TextFormatting.GREEN), false);
    }
    
    private static Quest getQuestById(int id) {
        for (Quest q : Quest.values()) {
            if (q.id == id) return q;
        }
        return null;
    }
    
    /**
     * Get quest progress
     */
    public static int getProgress(PlayerEntity player, Quest quest) {
        return player.getPersistentData().getInt(QUEST_PROGRESS_PREFIX + quest.id);
    }
    
    /**
     * Check if quest is completed
     */
    public static boolean isCompleted(PlayerEntity player, Quest quest) {
        return player.getPersistentData().getBoolean(QUEST_PREFIX + quest.id);
    }
    
    /**
     * Get active quest
     */
    public static Quest getActiveQuest(PlayerEntity player) {
        int id = player.getPersistentData().getInt(ACTIVE_QUEST);
        return getQuestById(id);
    }
}
