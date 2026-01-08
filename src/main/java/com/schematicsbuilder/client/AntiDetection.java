package com.schematicsbuilder.client;

import java.util.Random;

/**
 * Anti-Detection utilities
 * Makes building look more human-like to avoid anti-cheat systems
 */
public class AntiDetection {

    private static final Random random = new Random();

    // Settings
    private static boolean enabled = true;
    private static int baseDelay = 2; // Base ticks between placements
    private static int randomVariation = 3; // Random variation in ticks
    private static boolean randomizeOrder = false;
    private static boolean addMisclicks = true;
    private static int misclickChance = 5; // % chance of fake misclick

    /**
     * Get randomized delay for next action
     */
    public static int getRandomDelay() {
        if (!enabled)
            return baseDelay;

        // Add random variation
        int delay = baseDelay + random.nextInt(randomVariation + 1);

        // Occasional longer pauses (simulates player distraction)
        if (random.nextInt(100) < 3) {
            delay += random.nextInt(10) + 5; // 5-15 extra ticks
        }

        return delay;
    }

    /**
     * Get slightly randomized position offset for block placement
     * Makes clicks look less robotic
     */
    public static float getRandomOffset() {
        if (!enabled)
            return 0.5f;

        // Random offset between 0.3 and 0.7 (center area of block face)
        return 0.3f + random.nextFloat() * 0.4f;
    }

    /**
     * Should we do a "misclick" (place nothing, just swing arm)
     */
    public static boolean shouldMisclick() {
        if (!enabled || !addMisclicks)
            return false;
        return random.nextInt(100) < misclickChance;
    }

    /**
     * Get randomized movement speed
     */
    public static float getMovementSpeed() {
        if (!enabled)
            return 1.0f;

        // Vary between 0.7 and 1.0
        return 0.7f + random.nextFloat() * 0.3f;
    }

    /**
     * Should we take a short break (simulates player looking around)
     */
    public static boolean shouldTakeBreak() {
        if (!enabled)
            return false;
        return random.nextInt(100) < 1; // 1% chance
    }

    /**
     * Get break duration in ticks
     */
    public static int getBreakDuration() {
        return 20 + random.nextInt(40); // 1-3 seconds
    }

    // Settings getters/setters
    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static int getBaseDelay() {
        return baseDelay;
    }

    public static void setBaseDelay(int value) {
        baseDelay = Math.max(1, value);
    }

    public static int getRandomVariation() {
        return randomVariation;
    }

    public static void setRandomVariation(int value) {
        randomVariation = Math.max(0, value);
    }

    /**
     * Set anti-detection preset
     */
    public static void setPreset(String preset) {
        switch (preset.toLowerCase()) {
            case "off":
                enabled = false;
                break;
            case "light":
                enabled = true;
                baseDelay = 2;
                randomVariation = 2;
                addMisclicks = false;
                break;
            case "normal":
                enabled = true;
                baseDelay = 3;
                randomVariation = 3;
                addMisclicks = true;
                misclickChance = 3;
                break;
            case "paranoid":
                enabled = true;
                baseDelay = 4;
                randomVariation = 5;
                addMisclicks = true;
                misclickChance = 8;
                break;
            default:
                // Keep current settings
                break;
        }
    }

    /**
     * Get current settings as string
     */
    public static String getSettingsString() {
        if (!enabled)
            return "Anti-Detection: OFF";
        return "Anti-Detection: ON (delay=" + baseDelay + "±" + randomVariation + ")";
    }
}
