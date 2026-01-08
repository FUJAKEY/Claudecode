package com.infinitygauntlet.abilities;

import com.infinitygauntlet.init.ModItems;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Combo Abilities - Use 2 stones together for powerful combined attacks
 */
public class ComboAbilities {
    
    // Combo identifiers
    public static final String COMBO_SPACE_POWER = "space_power";      // Teleport Bomb
    public static final String COMBO_TIME_SOUL = "time_soul";          // Resurrection Wave
    public static final String COMBO_REALITY_MIND = "reality_mind";    // Mass Illusion
    public static final String COMBO_POWER_REALITY = "power_reality";  // World Breaker
    public static final String COMBO_SPACE_TIME = "space_time";        // Time Warp
    public static final String COMBO_SOUL_MIND = "soul_mind";          // Spirit Army
    
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();
    
    /**
     * Try to use a combo ability if player has 2 required stones selected
     */
    public static boolean tryCombo(World world, PlayerEntity player, ItemStack gauntlet, 
                                    int primaryStone, int secondaryStone) {
        String comboKey = getComboKey(primaryStone, secondaryStone);
        if (comboKey == null) return false;
        
        CompoundNBT nbt = gauntlet.getOrCreateTag();
        
        // Check cooldown
        long lastUse = COOLDOWNS.getOrDefault(player.getUUID().toString() + comboKey, 0L);
        if (System.currentTimeMillis() - lastUse < 10000) { // 10 second cooldown
            int remaining = (int)((10000 - (System.currentTimeMillis() - lastUse)) / 1000) + 1;
            player.displayClientMessage(
                new StringTextComponent("⚡ Combo cooldown: " + remaining + "s")
                    .withStyle(TextFormatting.GRAY), true);
            return false;
        }
        
        // Execute combo
        boolean success = executeCombo(world, player, comboKey);
        
        if (success) {
            COOLDOWNS.put(player.getUUID().toString() + comboKey, System.currentTimeMillis());
        }
        
        return success;
    }
    
    private static String getComboKey(int stone1, int stone2) {
        // Sort to get consistent key
        int min = Math.min(stone1, stone2);
        int max = Math.max(stone1, stone2);
        
        // Stone indices: 0=space, 1=time, 2=reality, 3=power, 4=mind, 5=soul
        if (min == 0 && max == 3) return COMBO_SPACE_POWER;    // Space + Power
        if (min == 1 && max == 5) return COMBO_TIME_SOUL;      // Time + Soul
        if (min == 2 && max == 4) return COMBO_REALITY_MIND;   // Reality + Mind
        if (min == 2 && max == 3) return COMBO_POWER_REALITY;  // Power + Reality
        if (min == 0 && max == 1) return COMBO_SPACE_TIME;     // Space + Time
        if (min == 4 && max == 5) return COMBO_SOUL_MIND;      // Soul + Mind
        
        return null;
    }
    
    private static boolean executeCombo(World world, PlayerEntity player, String comboKey) {
        if (world.isClientSide) return false;
        
        switch (comboKey) {
            case COMBO_SPACE_POWER: return comboTeleportBomb(world, player);
            case COMBO_TIME_SOUL: return comboResurrectionWave(world, player);
            case COMBO_REALITY_MIND: return comboMassIllusion(world, player);
            case COMBO_POWER_REALITY: return comboWorldBreaker(world, player);
            case COMBO_SPACE_TIME: return comboTimeWarp(world, player);
            case COMBO_SOUL_MIND: return comboSpiritArmy(world, player);
            default: return false;
        }
    }
    
    /**
     * Space + Power: Teleport Bomb
     * Teleport to target and create massive explosion
     */
    private static boolean comboTeleportBomb(World world, PlayerEntity player) {
        Vector3d lookVec = player.getLookAngle();
        Vector3d targetPos = player.position().add(lookVec.scale(30));
        
        // Teleport
        player.teleportTo(targetPos.x, targetPos.y + 1, targetPos.z);
        
        // Explosion damage without block damage
        AxisAlignedBB area = player.getBoundingBox().inflate(10);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity entity : entities) {
            entity.hurt(DamageSource.playerAttack(player).setMagic(), 40.0F);
            Vector3d knockback = entity.position().subtract(player.position()).normalize().scale(3);
            entity.setDeltaMovement(knockback.x, 1.0, knockback.z);
            entity.hurtMarked = true;
            entity.setSecondsOnFire(5);
        }
        
        // Effects
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1, player.getZ(), 5, 0, 0, 0, 0);
            sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1, player.getZ(), 200, 5, 3, 5, 0.5);
            sw.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 100, 3, 2, 3, 1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundCategory.PLAYERS, 3.0F, 0.5F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 2.0F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("⚡ TELEPORT BOMB! " + entities.size() + " targets hit!")
                .withStyle(TextFormatting.LIGHT_PURPLE).withStyle(TextFormatting.BOLD), true);
        
        return true;
    }
    
    /**
     * Time + Soul: Resurrection Wave
     * Fully heal and revive player + create protective time bubble
     */
    private static boolean comboResurrectionWave(World world, PlayerEntity player) {
        // Full restoration
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        
        // Clear all negative effects
        player.removeAllEffects();
        
        // Massive buffs
        int duration = 600; // 30 seconds
        player.addEffect(new EffectInstance(Effects.REGENERATION, duration, 4));
        player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, duration, 3));
        player.addEffect(new EffectInstance(Effects.ABSORPTION, duration, 5));
        player.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, duration, 0));
        player.addEffect(new EffectInstance(Effects.WATER_BREATHING, duration, 0));
        player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, duration, 2));
        player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, duration, 2));
        
        // Freeze all nearby enemies
        AxisAlignedBB area = player.getBoundingBox().inflate(15);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, 
            e -> e != player && !(e instanceof PlayerEntity));
        
        for (LivingEntity entity : entities) {
            entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, duration, 10));
            entity.addEffect(new EffectInstance(Effects.WEAKNESS, duration, 4));
        }
        
        // Effects
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 200, 3, 3, 3, 0.5);
            sw.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 100, 5, 3, 5, 0.1);
            sw.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1, player.getZ(), 100, 5, 3, 5, 0.3);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TOTEM_USE, SoundCategory.PLAYERS, 2.0F, 1.0F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundCategory.PLAYERS, 2.0F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("⚡ RESURRECTION WAVE! Full restoration!")
                .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
        
        return true;
    }
    
    /**
     * Reality + Mind: Mass Illusion
     * Turn enemies against each other
     */
    private static boolean comboMassIllusion(World world, PlayerEntity player) {
        AxisAlignedBB area = player.getBoundingBox().inflate(25);
        List<MobEntity> mobs = world.getEntitiesOfClass(MobEntity.class, area);
        
        // Make mobs attack each other
        for (int i = 0; i < mobs.size(); i++) {
            MobEntity mob = mobs.get(i);
            mob.setTarget(null);
            
            // Find another mob to target
            if (mobs.size() > 1) {
                MobEntity target = mobs.get((i + 1) % mobs.size());
                if (target != mob) {
                    mob.setTarget(target);
                    mob.setLastHurtByMob(target);
                }
            }
            
            mob.addEffect(new EffectInstance(Effects.CONFUSION, 400, 0));
            mob.addEffect(new EffectInstance(Effects.GLOWING, 400, 0));
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANTED_HIT,
                    mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(), 20, 0.5, 0.5, 0.5, 0.5);
            }
        }
        
        // Player becomes invisible
        player.addEffect(new EffectInstance(Effects.INVISIBILITY, 400, 0));
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.CRIMSON_SPORE,
                player.getX(), player.getY() + 1, player.getZ(), 200, 10, 5, 10, 0.1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 2.0F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("⚡ MASS ILLUSION! " + mobs.size() + " mobs confused!")
                .withStyle(TextFormatting.RED).withStyle(TextFormatting.BOLD), true);
        
        return true;
    }
    
    /**
     * Power + Reality: World Breaker
     * Destroy and transform large area
     */
    private static boolean comboWorldBreaker(World world, PlayerEntity player) {
        int radius = 10;
        BlockPos center = player.blockPosition();
        int affected = 0;
        
        Random rand = new Random();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + z*z > radius*radius) continue;
                    
                    BlockPos pos = center.offset(x, y, z);
                    if (world.getBlockState(pos).getDestroySpeed(world, pos) >= 0 &&
                        world.getBlockState(pos).getBlock() != Blocks.BEDROCK) {
                        
                        if (rand.nextFloat() < 0.3) {
                            // Transform
                            switch (rand.nextInt(4)) {
                                case 0: world.setBlock(pos, Blocks.DIAMOND_ORE.defaultBlockState(), 3); break;
                                case 1: world.setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), 3); break;
                                case 2: world.setBlock(pos, Blocks.EMERALD_ORE.defaultBlockState(), 3); break;
                                case 3: world.setBlock(pos, Blocks.ANCIENT_DEBRIS.defaultBlockState(), 3); break;
                            }
                        } else if (rand.nextFloat() < 0.5) {
                            // Destroy
                            world.destroyBlock(pos, true);
                        }
                        affected++;
                    }
                }
            }
        }
        
        // Damage nearby enemies
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        for (LivingEntity entity : entities) {
            entity.hurt(DamageSource.playerAttack(player).setMagic(), 50.0F);
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (int i = 0; i < 20; i++) {
                sw.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX() + rand.nextGaussian() * 5,
                    player.getY() + 1,
                    player.getZ() + rand.nextGaussian() * 5,
                    1, 0, 0, 0, 0);
            }
            sw.sendParticles(ParticleTypes.CRIMSON_SPORE,
                player.getX(), player.getY() + 1, player.getZ(), 500, radius, 5, radius, 0.5);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 3.0F, 0.3F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundCategory.PLAYERS, 3.0F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("⚡ WORLD BREAKER! " + affected + " blocks affected!")
                .withStyle(TextFormatting.DARK_RED).withStyle(TextFormatting.BOLD), true);
        
        return true;
    }
    
    /**
     * Space + Time: Time Warp
     * Freeze time and teleport around at will (speed boost + time stop)
     */
    private static boolean comboTimeWarp(World world, PlayerEntity player) {
        int duration = 300; // 15 seconds
        
        // Player gets extreme speed and flying-like movement
        player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, duration, 5));
        player.addEffect(new EffectInstance(Effects.JUMP, duration, 3));
        player.addEffect(new EffectInstance(Effects.SLOW_FALLING, duration, 0));
        player.addEffect(new EffectInstance(Effects.DOLPHINS_GRACE, duration, 0));
        
        // Freeze all entities
        AxisAlignedBB area = player.getBoundingBox().inflate(30);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity entity : entities) {
            entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, duration, 255)); // Complete freeze
            entity.addEffect(new EffectInstance(Effects.JUMP, duration, 128));
            entity.setDeltaMovement(0, 0, 0);
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1, player.getZ(), 300, 15, 5, 15, 0.05);
            sw.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1, player.getZ(), 200, 10, 3, 10, 1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMBIENT_CAVE, SoundCategory.PLAYERS, 2.0F, 0.1F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_POWER_SELECT, SoundCategory.PLAYERS, 2.0F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("⚡ TIME WARP! " + entities.size() + " entities frozen! Move freely!")
                .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
        
        return true;
    }
    
    /**
     * Soul + Mind: Spirit Army
     * Summon ghost warriors to fight for you
     */
    private static boolean comboSpiritArmy(World world, PlayerEntity player) {
        if (!(world instanceof ServerWorld)) return false;
        
        ServerWorld sw = (ServerWorld) world;
        int summonCount = 5;
        Random rand = new Random();
        
        for (int i = 0; i < summonCount; i++) {
            // Create wolf allies (acting as "spirits")
            WolfEntity wolf = new WolfEntity(net.minecraft.entity.EntityType.WOLF, world);
            
            double x = player.getX() + (rand.nextDouble() - 0.5) * 6;
            double z = player.getZ() + (rand.nextDouble() - 0.5) * 6;
            wolf.setPos(x, player.getY(), z);
            
            // Make them tamed to player
            wolf.tame(player);
            wolf.setOwnerUUID(player.getUUID());
            
            // Buff them
            wolf.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 1200, 3));
            wolf.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 1200, 2));
            wolf.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 1200, 2));
            wolf.addEffect(new EffectInstance(Effects.GLOWING, 1200, 0));
            wolf.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 1200, 0));
            
            // Set health high
            wolf.setHealth(100.0F);
            
            world.addFreshEntity(wolf);
            
            sw.sendParticles(ParticleTypes.SOUL,
                wolf.getX(), wolf.getY() + 1, wolf.getZ(), 30, 0.5, 1, 0.5, 0.1);
        }
        
        sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
            player.getX(), player.getY() + 1, player.getZ(), 100, 3, 2, 3, 0.2);
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WOLF_HOWL, SoundCategory.PLAYERS, 2.0F, 0.5F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SOUL_ESCAPE, SoundCategory.PLAYERS, 2.0F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("⚡ SPIRIT ARMY! " + summonCount + " spirit wolves summoned!")
                .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
        
        return true;
    }
}
