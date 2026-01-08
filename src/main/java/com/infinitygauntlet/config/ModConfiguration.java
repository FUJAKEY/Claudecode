package com.infinitygauntlet.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ModConfiguration {
    
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    // Stone Cooldowns
    public static final ForgeConfigSpec.IntValue SPACE_STONE_COOLDOWN;
    public static final ForgeConfigSpec.IntValue TIME_STONE_COOLDOWN;
    public static final ForgeConfigSpec.IntValue REALITY_STONE_COOLDOWN;
    public static final ForgeConfigSpec.IntValue POWER_STONE_COOLDOWN;
    public static final ForgeConfigSpec.IntValue MIND_STONE_COOLDOWN;
    public static final ForgeConfigSpec.IntValue SOUL_STONE_COOLDOWN;
    
    // Ability Settings
    public static final ForgeConfigSpec.IntValue TELEPORT_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue POWER_STONE_DAMAGE;
    public static final ForgeConfigSpec.IntValue TIME_FREEZE_DURATION;
    public static final ForgeConfigSpec.IntValue INFINITY_BARRIER_RADIUS;
    
    // Snap Settings
    public static final ForgeConfigSpec.IntValue SNAP_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue SNAP_RADIUS;
    public static final ForgeConfigSpec.DoubleValue SNAP_KILL_PERCENT;
    public static final ForgeConfigSpec.DoubleValue SNAP_DAMAGE_TO_PLAYER;
    
    // Thanos Settings
    public static final ForgeConfigSpec.DoubleValue THANOS_HEALTH;
    public static final ForgeConfigSpec.DoubleValue THANOS_DAMAGE;
    public static final ForgeConfigSpec.IntValue THANOS_SPAWN_CHANCE;
    
    // Combo Abilities
    public static final ForgeConfigSpec.BooleanValue ENABLE_COMBO_ABILITIES;
    public static final ForgeConfigSpec.IntValue COMBO_COOLDOWN;
    
    // XP Cost
    public static final ForgeConfigSpec.BooleanValue ENABLE_XP_COST;
    public static final ForgeConfigSpec.IntValue XP_COST_PER_STONE;
    
    static {
        BUILDER.push("Stone Cooldowns (in ticks, 20 = 1 second)");
        SPACE_STONE_COOLDOWN = BUILDER.comment("Space Stone cooldown").defineInRange("spaceStoneCooldown", 40, 0, 6000);
        TIME_STONE_COOLDOWN = BUILDER.comment("Time Stone cooldown").defineInRange("timeStoneCooldown", 100, 0, 6000);
        REALITY_STONE_COOLDOWN = BUILDER.comment("Reality Stone cooldown").defineInRange("realityStoneCooldown", 60, 0, 6000);
        POWER_STONE_COOLDOWN = BUILDER.comment("Power Stone cooldown").defineInRange("powerStoneCooldown", 80, 0, 6000);
        MIND_STONE_COOLDOWN = BUILDER.comment("Mind Stone cooldown").defineInRange("mindStoneCooldown", 120, 0, 6000);
        SOUL_STONE_COOLDOWN = BUILDER.comment("Soul Stone cooldown").defineInRange("soulStoneCooldown", 60, 0, 6000);
        BUILDER.pop();
        
        BUILDER.push("Ability Settings");
        TELEPORT_DISTANCE = BUILDER.comment("Max teleport distance for Space Stone").defineInRange("teleportDistance", 50, 10, 500);
        POWER_STONE_DAMAGE = BUILDER.comment("Power Stone base damage").defineInRange("powerStoneDamage", 20.0, 1.0, 1000.0);
        TIME_FREEZE_DURATION = BUILDER.comment("Time freeze duration in ticks").defineInRange("timeFreezeDuration", 200, 20, 6000);
        INFINITY_BARRIER_RADIUS = BUILDER.comment("Infinity barrier radius").defineInRange("infinityBarrierRadius", 4, 1, 20);
        BUILDER.pop();
        
        BUILDER.push("Snap Settings");
        SNAP_COOLDOWN = BUILDER.comment("Snap cooldown in ticks (6000 = 5 minutes)").defineInRange("snapCooldown", 6000, 0, 72000);
        SNAP_RADIUS = BUILDER.comment("Snap effect radius").defineInRange("snapRadius", 150.0, 10.0, 1000.0);
        SNAP_KILL_PERCENT = BUILDER.comment("Percent of entities to kill (0.5 = 50%)").defineInRange("snapKillPercent", 0.5, 0.0, 1.0);
        SNAP_DAMAGE_TO_PLAYER = BUILDER.comment("Damage to player after snap").defineInRange("snapDamageToPlayer", 20.0, 0.0, 100.0);
        BUILDER.pop();
        
        BUILDER.push("Thanos Boss");
        THANOS_HEALTH = BUILDER.comment("Thanos max health").defineInRange("thanosHealth", 500.0, 50.0, 10000.0);
        THANOS_DAMAGE = BUILDER.comment("Thanos base attack damage").defineInRange("thanosDamage", 15.0, 1.0, 100.0);
        THANOS_SPAWN_CHANCE = BUILDER.comment("Thanos spawn chance when player has all stones (1 in X per minute)").defineInRange("thanosSpawnChance", 100, 1, 10000);
        BUILDER.pop();
        
        BUILDER.push("Combo Abilities");
        ENABLE_COMBO_ABILITIES = BUILDER.comment("Enable combination abilities").define("enableComboAbilities", true);
        COMBO_COOLDOWN = BUILDER.comment("Combo ability cooldown in ticks").defineInRange("comboCooldown", 200, 20, 6000);
        BUILDER.pop();
        
        BUILDER.push("Balance");
        ENABLE_XP_COST = BUILDER.comment("Enable XP cost for using abilities").define("enableXpCost", true);
        XP_COST_PER_STONE = BUILDER.comment("XP cost per stone count").defineInRange("xpCostPerStone", 5, 0, 100);
        BUILDER.pop();
        
        SPEC = BUILDER.build();
    }
    
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "infinitygauntlet-common.toml");
    }
}
