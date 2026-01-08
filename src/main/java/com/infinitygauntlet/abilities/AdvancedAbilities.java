package com.infinitygauntlet.abilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Advanced stone abilities for v5.0
 */
public class AdvancedAbilities {
    
    // Portal system storage
    private static final Map<UUID, BlockPos> PORTAL_POINT_A = new HashMap<>();
    private static final Map<UUID, BlockPos> PORTAL_POINT_B = new HashMap<>();
    private static final Map<UUID, Long> PORTAL_COOLDOWN = new HashMap<>();
    
    // Mob transformation map
    private static final Map<EntityType<?>, EntityType<?>> MOB_TRANSFORMS = new HashMap<>();
    
    static {
        // Hostile -> Passive transformations
        MOB_TRANSFORMS.put(EntityType.ZOMBIE, EntityType.VILLAGER);
        MOB_TRANSFORMS.put(EntityType.SKELETON, EntityType.WOLF);
        MOB_TRANSFORMS.put(EntityType.CREEPER, EntityType.PIG);
        MOB_TRANSFORMS.put(EntityType.SPIDER, EntityType.CAT);
        MOB_TRANSFORMS.put(EntityType.ENDERMAN, EntityType.IRON_GOLEM);
        MOB_TRANSFORMS.put(EntityType.BLAZE, EntityType.STRIDER);
        MOB_TRANSFORMS.put(EntityType.WITCH, EntityType.VILLAGER);
        MOB_TRANSFORMS.put(EntityType.PILLAGER, EntityType.VILLAGER);
        MOB_TRANSFORMS.put(EntityType.VINDICATOR, EntityType.IRON_GOLEM);
        MOB_TRANSFORMS.put(EntityType.DROWNED, EntityType.DOLPHIN);
        MOB_TRANSFORMS.put(EntityType.HUSK, EntityType.VILLAGER);
        MOB_TRANSFORMS.put(EntityType.PHANTOM, EntityType.PARROT);
        MOB_TRANSFORMS.put(EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN);
        MOB_TRANSFORMS.put(EntityType.HOGLIN, EntityType.PIG);
        MOB_TRANSFORMS.put(EntityType.PIGLIN_BRUTE, EntityType.PIGLIN);
        MOB_TRANSFORMS.put(EntityType.RAVAGER, EntityType.COW);
        MOB_TRANSFORMS.put(EntityType.GHAST, EntityType.SNOW_GOLEM);
        MOB_TRANSFORMS.put(EntityType.MAGMA_CUBE, EntityType.SLIME);
        MOB_TRANSFORMS.put(EntityType.SILVERFISH, EntityType.BEE);
        MOB_TRANSFORMS.put(EntityType.CAVE_SPIDER, EntityType.CAT);
        
        // Passive -> Special transformations
        MOB_TRANSFORMS.put(EntityType.PIG, EntityType.HORSE);
        MOB_TRANSFORMS.put(EntityType.COW, EntityType.MOOSHROOM);
        MOB_TRANSFORMS.put(EntityType.CHICKEN, EntityType.PARROT);
        MOB_TRANSFORMS.put(EntityType.SHEEP, EntityType.LLAMA);
        MOB_TRANSFORMS.put(EntityType.WOLF, EntityType.FOX);
        MOB_TRANSFORMS.put(EntityType.CAT, EntityType.OCELOT);
    }
    
    /**
     * Power Stone: Laser Beam
     * Continuous damage beam in the direction player is looking
     */
    public static void useLaserBeam(World world, PlayerEntity player, float power) {
        if (world.isClientSide) return;
        
        ServerWorld sw = (ServerWorld) world;
        Vector3d eyePos = player.getEyePosition(1.0F);
        Vector3d lookVec = player.getLookAngle();
        
        double beamLength = 30.0 * power;
        float damage = 5.0F * power;
        
        // Trace beam
        for (double d = 0; d < beamLength; d += 0.5) {
            Vector3d pos = eyePos.add(lookVec.scale(d));
            
            // Particles along beam
            sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0.01);
            
            // Check for entities at this point
            AxisAlignedBB hitBox = new AxisAlignedBB(pos.x - 0.3, pos.y - 0.3, pos.z - 0.3,
                pos.x + 0.3, pos.y + 0.3, pos.z + 0.3);
            
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, hitBox,
                e -> e != player);
            
            for (LivingEntity entity : entities) {
                entity.hurt(DamageSource.playerAttack(player).setMagic(), damage);
                entity.setSecondsOnFire(3);
                
                sw.sendParticles(ParticleTypes.FLAME,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    10, 0.3, 0.3, 0.3, 0.1);
            }
            
            // Check for block collision
            BlockPos blockPos = new BlockPos(pos);
            if (!world.getBlockState(blockPos).isAir()) {
                sw.sendParticles(ParticleTypes.LAVA,
                    pos.x, pos.y, pos.z, 10, 0.2, 0.2, 0.2, 0.05);
                break;
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("☠ POWER BEAM!")
                .withStyle(TextFormatting.DARK_PURPLE), true);
    }
    
    /**
     * Space Stone: Portal System
     * Set point A first, then point B to create bidirectional portal
     */
    public static void usePortalSystem(World world, PlayerEntity player, float power) {
        if (world.isClientSide) return;
        
        UUID playerId = player.getUUID();
        BlockPos currentPos = player.blockPosition();
        
        // Check cooldown
        long lastUse = PORTAL_COOLDOWN.getOrDefault(playerId, 0L);
        if (System.currentTimeMillis() - lastUse < 500) return;
        PORTAL_COOLDOWN.put(playerId, System.currentTimeMillis());
        
        if (!PORTAL_POINT_A.containsKey(playerId)) {
            // Set point A
            PORTAL_POINT_A.put(playerId, currentPos);
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.PORTAL,
                    currentPos.getX() + 0.5, currentPos.getY() + 1, currentPos.getZ() + 0.5,
                    50, 0.5, 1, 0.5, 0.5);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.5F);
            
            player.displayClientMessage(
                new StringTextComponent("⬡ Portal Point A set! Use again to set Point B")
                    .withStyle(TextFormatting.BLUE), true);
            
        } else if (!PORTAL_POINT_B.containsKey(playerId)) {
            // Set point B
            PORTAL_POINT_B.put(playerId, currentPos);
            
            BlockPos pointA = PORTAL_POINT_A.get(playerId);
            
            if (world instanceof ServerWorld) {
                ServerWorld sw = (ServerWorld) world;
                // Particles at both points
                sw.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    pointA.getX() + 0.5, pointA.getY() + 1, pointA.getZ() + 0.5,
                    100, 0.5, 1, 0.5, 0.5);
                sw.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    currentPos.getX() + 0.5, currentPos.getY() + 1, currentPos.getZ() + 0.5,
                    100, 0.5, 1, 0.5, 0.5);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.END_PORTAL_SPAWN, SoundCategory.PLAYERS, 0.5F, 2.0F);
            
            player.displayClientMessage(
                new StringTextComponent("⬡ Portal created! Use again to teleport")
                    .withStyle(TextFormatting.BLUE), true);
            
        } else {
            // Teleport between points
            BlockPos pointA = PORTAL_POINT_A.get(playerId);
            BlockPos pointB = PORTAL_POINT_B.get(playerId);
            
            double distToA = player.distanceToSqr(pointA.getX(), pointA.getY(), pointA.getZ());
            double distToB = player.distanceToSqr(pointB.getX(), pointB.getY(), pointB.getZ());
            
            BlockPos destination = distToA < distToB ? pointB : pointA;
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    50, 0.5, 1, 0.5, 0.5);
            }
            
            player.teleportTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.REVERSE_PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    50, 0.5, 1, 0.5, 0.5);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            
            player.displayClientMessage(
                new StringTextComponent("⬡ Teleported through portal!")
                    .withStyle(TextFormatting.BLUE), true);
        }
    }
    
    /**
     * Clear portals for player
     */
    public static void clearPortals(PlayerEntity player) {
        UUID playerId = player.getUUID();
        PORTAL_POINT_A.remove(playerId);
        PORTAL_POINT_B.remove(playerId);
        
        player.displayClientMessage(
            new StringTextComponent("⬡ Portals cleared")
                .withStyle(TextFormatting.GRAY), true);
    }
    
    /**
     * Reality Stone: Mob Transformation
     * Transform mobs in front of player
     */
    public static void useMobTransform(World world, PlayerEntity player, float power) {
        if (world.isClientSide) return;
        
        ServerWorld sw = (ServerWorld) world;
        int radius = (int)(8 * power);
        int transformed = 0;
        
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        List<MobEntity> mobs = world.getEntitiesOfClass(MobEntity.class, area);
        
        for (MobEntity mob : mobs) {
            EntityType<?> newType = MOB_TRANSFORMS.get(mob.getType());
            
            if (newType != null) {
                // Create new entity
                Entity newEntity = newType.create(world);
                if (newEntity != null) {
                    newEntity.setPos(mob.getX(), mob.getY(), mob.getZ());
                    
                    // Copy some data
                    if (newEntity instanceof LivingEntity) {
                        ((LivingEntity) newEntity).setHealth(((LivingEntity) newEntity).getMaxHealth());
                    }
                    
                    // Visual effect
                    sw.sendParticles(ParticleTypes.CRIMSON_SPORE,
                        mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                        30, 0.5, 0.5, 0.5, 0.1);
                    sw.sendParticles(ParticleTypes.ENCHANT,
                        mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                        20, 0.5, 0.5, 0.5, 0.5);
                    
                    // Remove old, spawn new
                    mob.remove();
                    world.addFreshEntity(newEntity);
                    transformed++;
                }
            }
        }
        
        sw.sendParticles(ParticleTypes.CRIMSON_SPORE,
            player.getX(), player.getY() + 1, player.getZ(),
            100, radius/2, 2, radius/2, 0.1);
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 1.5F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("✦ Transformed " + transformed + " mobs!")
                .withStyle(TextFormatting.RED), true);
    }
    
    /**
     * Soul Stone: Summon Dead
     * Summon ghosts of recently killed mobs as allies
     */
    public static void useSummonDead(World world, PlayerEntity player, float power) {
        if (world.isClientSide) return;
        
        ServerWorld sw = (ServerWorld) world;
        int summonCount = (int)(3 + power * 2);
        
        // Summon zombie allies (representing "dead souls")
        for (int i = 0; i < summonCount; i++) {
            ZombieEntity zombie = new ZombieEntity(world);
            
            double angle = (Math.PI * 2 / summonCount) * i;
            double x = player.getX() + Math.cos(angle) * 3;
            double z = player.getZ() + Math.sin(angle) * 3;
            
            zombie.setPos(x, player.getY(), z);
            
            // Make them not attack player (set persistent data)
            zombie.setPersistenceRequired();
            zombie.setBaby(false);
            
            // Give them effects
            zombie.addEffect(new net.minecraft.potion.EffectInstance(
                net.minecraft.potion.Effects.DAMAGE_BOOST, 1200, 2));
            zombie.addEffect(new net.minecraft.potion.EffectInstance(
                net.minecraft.potion.Effects.MOVEMENT_SPEED, 1200, 1));
            zombie.addEffect(new net.minecraft.potion.EffectInstance(
                net.minecraft.potion.Effects.GLOWING, 1200, 0));
            zombie.addEffect(new net.minecraft.potion.EffectInstance(
                net.minecraft.potion.Effects.FIRE_RESISTANCE, 1200, 0));
            
            // They attack other mobs, not player
            AxisAlignedBB area = zombie.getBoundingBox().inflate(20);
            List<MobEntity> targets = world.getEntitiesOfClass(MobEntity.class, area,
                e -> !(e instanceof ZombieEntity));
            if (!targets.isEmpty()) {
                zombie.setTarget(targets.get(0));
            }
            
            world.addFreshEntity(zombie);
            
            sw.sendParticles(ParticleTypes.SOUL,
                zombie.getX(), zombie.getY() + 1, zombie.getZ(),
                20, 0.3, 0.5, 0.3, 0.1);
        }
        
        sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
            player.getX(), player.getY() + 1, player.getZ(),
            100, 3, 2, 3, 0.2);
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SOUL_ESCAPE, SoundCategory.PLAYERS, 1.5F, 0.5F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundCategory.PLAYERS, 1.0F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("❤ Summoned " + summonCount + " undead warriors!")
                .withStyle(TextFormatting.GOLD), true);
    }
}
