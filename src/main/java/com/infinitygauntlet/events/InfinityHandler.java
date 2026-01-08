package com.infinitygauntlet.events;

import com.infinitygauntlet.InfinityGauntletMod;
import com.infinitygauntlet.init.ModItems;
import com.infinitygauntlet.items.InfinityGauntletItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * INFINITY - Gojo Satoru's technique
 * Creates an absolute barrier that nothing can penetrate.
 * Uses convergent infinite series to slow everything approaching to zero.
 */
@Mod.EventBusSubscriber(modid = InfinityGauntletMod.MOD_ID)
public class InfinityHandler {
    
    // Track players with Infinity active
    private static final Map<UUID, Long> INFINITY_ACTIVE = new HashMap<>();
    private static final double INFINITY_RADIUS = 4.0; // Barrier radius
    private static final double PUSH_STRENGTH = 1.5;
    
    /**
     * Check if player has Infinity active
     */
    public static boolean hasInfinityActive(PlayerEntity player) {
        if (player == null) return false;
        
        // Check main hand and off hand
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
                CompoundNBT nbt = stack.getOrCreateTag();
                if (nbt.getBoolean("time_stone") && nbt.getBoolean("infinity_active")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static void setInfinityActive(PlayerEntity player, boolean active) {
        if (active) {
            INFINITY_ACTIVE.put(player.getUUID(), System.currentTimeMillis());
        } else {
            INFINITY_ACTIVE.remove(player.getUUID());
        }
    }
    
    /**
     * BLOCK ALL DAMAGE - Highest priority, cancel everything
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (hasInfinityActive(player)) {
                event.setCanceled(true);
                
                // Visual effect on blocked damage
                if (!player.level.isClientSide && player.level instanceof ServerWorld) {
                    ServerWorld world = (ServerWorld) player.level;
                    world.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 1, player.getZ(),
                        5, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
    }
    
    /**
     * BLOCK ALL HURT - Secondary protection layer
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (hasInfinityActive(player)) {
                event.setCanceled(true);
            }
        }
    }
    
    /**
     * BLOCK ALL DAMAGE APPLICATION - Final protection layer
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (hasInfinityActive(player)) {
                event.setCanceled(true);
            }
        }
    }
    
    /**
     * BLOCK ALL PROJECTILES - Arrows, fireballs, mod projectiles, everything
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Entity projectile = event.getEntity();
        World world = projectile.level;
        
        // Check all players with Infinity
        List<PlayerEntity> players = world.getEntitiesOfClass(PlayerEntity.class,
            projectile.getBoundingBox().inflate(INFINITY_RADIUS));
        
        for (PlayerEntity player : players) {
            if (hasInfinityActive(player)) {
                double distance = projectile.distanceTo(player);
                if (distance < INFINITY_RADIUS) {
                    // Cancel impact
                    event.setCanceled(true);
                    
                    // Deflect projectile
                    Vector3d deflect = projectile.position()
                        .subtract(player.position())
                        .normalize()
                        .scale(2.0);
                    projectile.setDeltaMovement(deflect);
                    
                    // Effect
                    if (world instanceof ServerWorld) {
                        ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANT,
                            projectile.getX(), projectile.getY(), projectile.getZ(),
                            10, 0.2, 0.2, 0.2, 0.5);
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * BLOCK EXPLOSIONS - TNT, creepers, mod explosions
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        World world = event.getWorld();
        Vector3d explosionPos = explosion.getPosition();
        
        // Remove affected entities that have Infinity
        event.getAffectedEntities().removeIf(entity -> {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                if (hasInfinityActive(player)) {
                    return true; // Remove from affected list
                }
            }
            return false;
        });
        
        // Also protect blocks near players with Infinity
        event.getAffectedBlocks().removeIf(pos -> {
            List<PlayerEntity> nearbyPlayers = world.getEntitiesOfClass(PlayerEntity.class,
                new AxisAlignedBB(pos).inflate(INFINITY_RADIUS));
            for (PlayerEntity player : nearbyPlayers) {
                if (hasInfinityActive(player)) {
                    return true;
                }
            }
            return false;
        });
    }
    
    /**
     * PUSH EVERYTHING AWAY - Nothing can approach
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        PlayerEntity player = event.player;
        if (player.level.isClientSide) return;
        
        if (!hasInfinityActive(player)) return;
        
        World world = player.level;
        
        // Push away ALL entities
        AxisAlignedBB area = player.getBoundingBox().inflate(INFINITY_RADIUS);
        List<Entity> entities = world.getEntities(player, area, e -> e != player);
        
        for (Entity entity : entities) {
            double distance = entity.distanceTo(player);
            
            if (distance < INFINITY_RADIUS && distance > 0.1) {
                // Calculate push force - stronger when closer (inverse)
                double force = PUSH_STRENGTH * (1.0 - (distance / INFINITY_RADIUS));
                force = Math.max(force, 0.3);
                
                // Direction away from player
                Vector3d direction = entity.position()
                    .subtract(player.position())
                    .normalize();
                
                // Apply velocity
                Vector3d push = direction.scale(force);
                entity.setDeltaMovement(entity.getDeltaMovement().add(push.x, push.y * 0.3, push.z));
                entity.hurtMarked = true;
                
                // Stop hostile mobs from attacking
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    if (living instanceof net.minecraft.entity.MobEntity) {
                        ((net.minecraft.entity.MobEntity) living).setTarget(null);
                    }
                }
            }
        }
        
        // Visual effect - barrier
        if (world instanceof ServerWorld && world.getGameTime() % 5 == 0) {
            ServerWorld sw = (ServerWorld) world;
            
            // Sphere of particles
            for (int i = 0; i < 360; i += 30) {
                for (int j = -60; j <= 60; j += 30) {
                    double yaw = Math.toRadians(i);
                    double pitch = Math.toRadians(j);
                    double radius = INFINITY_RADIUS - 0.2;
                    
                    double x = player.getX() + Math.cos(yaw) * Math.cos(pitch) * radius;
                    double y = player.getY() + 1 + Math.sin(pitch) * radius;
                    double z = player.getZ() + Math.sin(yaw) * Math.cos(pitch) * radius;
                    
                    sw.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0);
                }
            }
            
            // Inner glow
            sw.sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1, player.getZ(),
                20, INFINITY_RADIUS/2, INFINITY_RADIUS/2, INFINITY_RADIUS/2, 0.01);
        }
        
        // Sound effect
        if (world.getGameTime() % 20 == 0) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_AMBIENT, SoundCategory.PLAYERS, 0.3F, 2.0F);
        }
        
        // Hunger cost for using Infinity
        if (world.getGameTime() % 40 == 0) {
            player.getFoodData().addExhaustion(1.0F);
        }
    }
}
