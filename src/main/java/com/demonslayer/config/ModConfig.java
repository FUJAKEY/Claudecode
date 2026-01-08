package com.demonslayer.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod configuration for server and client settings
 */
public class ModConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Combat settings
    public static final ForgeConfigSpec.DoubleValue BREATHING_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue DEMON_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue FORM_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.BooleanValue TOTAL_CONCENTRATION_HALVES_COOLDOWN;

    // Boss settings
    public static final ForgeConfigSpec.DoubleValue MUZAN_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue AKAZA_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue KOKUSHIBO_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue BOSSES_BURN_IN_SUNLIGHT;

    // Progression settings
    public static final ForgeConfigSpec.DoubleValue XP_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue XP_PER_DEMON_KILL;
    public static final ForgeConfigSpec.IntValue XP_PER_FORM_USE;
    public static final ForgeConfigSpec.BooleanValue SLAYER_MARK_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SLAYER_MARK_HEALTH_THRESHOLD;

    // Visual settings
    public static final ForgeConfigSpec.BooleanValue SHOW_BREATHING_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue SHOW_DAMAGE_NUMBERS;
    public static final ForgeConfigSpec.BooleanValue SHOW_FORM_ANNOUNCEMENTS;

    // Multiplayer settings
    public static final ForgeConfigSpec.BooleanValue SYNC_EFFECTS_TO_OTHER_PLAYERS;
    public static final ForgeConfigSpec.IntValue PARTICLE_SYNC_RANGE;

    static {
        BUILDER.push("Combat");

        BREATHING_DAMAGE_MULTIPLIER = BUILDER
                .comment("Multiplier for all breathing form damage (default: 1.0)")
                .defineInRange("breathingDamageMultiplier", 1.0, 0.1, 10.0);

        DEMON_DAMAGE_MULTIPLIER = BUILDER
                .comment("Multiplier for demon attack damage (default: 1.0)")
                .defineInRange("demonDamageMultiplier", 1.0, 0.1, 10.0);

        FORM_COOLDOWN_TICKS = BUILDER
                .comment("Base cooldown for breathing forms in ticks (default: 40)")
                .defineInRange("formCooldownTicks", 40, 10, 200);

        TOTAL_CONCENTRATION_HALVES_COOLDOWN = BUILDER
                .comment("Whether Total Concentration effect halves cooldowns (default: true)")
                .define("totalConcentrationHalvesCooldown", true);

        BUILDER.pop();
        BUILDER.push("Bosses");

        MUZAN_HEALTH_MULTIPLIER = BUILDER
                .comment("Health multiplier for Muzan (default: 1.0, base 1000 HP)")
                .defineInRange("muzanHealthMultiplier", 1.0, 0.5, 5.0);

        AKAZA_HEALTH_MULTIPLIER = BUILDER
                .comment("Health multiplier for Akaza (default: 1.0, base 600 HP)")
                .defineInRange("akazaHealthMultiplier", 1.0, 0.5, 5.0);

        KOKUSHIBO_HEALTH_MULTIPLIER = BUILDER
                .comment("Health multiplier for Kokushibo (default: 1.0, base 900 HP)")
                .defineInRange("kokushiboHealthMultiplier", 1.0, 0.5, 5.0);

        BOSSES_BURN_IN_SUNLIGHT = BUILDER
                .comment("Whether demon bosses take damage from sunlight (default: true)")
                .define("bossesBurnInSunlight", true);

        BUILDER.pop();
        BUILDER.push("Progression");

        XP_MULTIPLIER = BUILDER
                .comment("Multiplier for all XP gains (default: 1.0)")
                .defineInRange("xpMultiplier", 1.0, 0.1, 10.0);

        XP_PER_DEMON_KILL = BUILDER
                .comment("XP gained per demon kill (default: 10)")
                .defineInRange("xpPerDemonKill", 10, 1, 100);

        XP_PER_FORM_USE = BUILDER
                .comment("XP gained per breathing form use (default: 2)")
                .defineInRange("xpPerFormUse", 2, 1, 20);

        SLAYER_MARK_ENABLED = BUILDER
                .comment("Whether the Slayer Mark system is enabled (default: true)")
                .define("slayerMarkEnabled", true);

        SLAYER_MARK_HEALTH_THRESHOLD = BUILDER
                .comment("Health percentage to trigger Slayer Mark (default: 0.25)")
                .defineInRange("slayerMarkHealthThreshold", 0.25, 0.1, 0.5);

        BUILDER.pop();
        BUILDER.push("Visual");

        SHOW_BREATHING_PARTICLES = BUILDER
                .comment("Show breathing technique particles (default: true)")
                .define("showBreathingParticles", true);

        SHOW_DAMAGE_NUMBERS = BUILDER
                .comment("Show damage numbers on hit (default: true)")
                .define("showDamageNumbers", true);

        SHOW_FORM_ANNOUNCEMENTS = BUILDER
                .comment("Show form name announcements (default: true)")
                .define("showFormAnnouncements", true);

        BUILDER.pop();
        BUILDER.push("Multiplayer");

        SYNC_EFFECTS_TO_OTHER_PLAYERS = BUILDER
                .comment("Sync visual effects to other players (default: true)")
                .define("syncEffectsToOtherPlayers", true);

        PARTICLE_SYNC_RANGE = BUILDER
                .comment("Range in blocks to sync particle effects (default: 64)")
                .defineInRange("particleSyncRange", 64, 16, 256);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
