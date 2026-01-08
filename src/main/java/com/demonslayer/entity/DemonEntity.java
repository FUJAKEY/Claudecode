package com.demonslayer.entity;

import com.demonslayer.init.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

public class DemonEntity extends MonsterEntity {
    
    private int attackCooldown = 0;
    private int specialAttackCooldown = 0;
    private boolean isEnraged = false;
    
    public DemonEntity(EntityType<? extends MonsterEntity> type, World world) {
        super(type, world);
    }
    
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(4, new LookRandomlyGoal(this));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!level.isClientSide) {
            // Sunlight damage
            if (level.isDay() && level.canSeeSky(blockPosition())) {
                setSecondsOnFire(2);
                hurt(DamageSource.ON_FIRE, 2.0F);
                
                if (level instanceof ServerWorld) {
                    ((ServerWorld) level).sendParticles(ParticleTypes.SMOKE,
                        getX(), getY() + 1, getZ(), 5, 0.3, 0.5, 0.3, 0.02);
                }
            } else {
                // Night regeneration
                if (getHealth() < getMaxHealth() && tickCount % 40 == 0) {
                    heal(2.0F);
                }
            }
            
            // Enrage at low health
            if (getHealth() < getMaxHealth() * 0.3 && !isEnraged) {
                isEnraged = true;
                addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 9999, 1));
                addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 9999, 1));
                
                if (level instanceof ServerWorld) {
                    ((ServerWorld) level).sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        getX(), getY() + 1.5, getZ(), 10, 0.5, 0.5, 0.5, 0.1);
                }
                
                level.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundCategory.HOSTILE, 2.0F, 0.5F);
            }
            
            // Special attacks
            if (attackCooldown > 0) attackCooldown--;
            if (specialAttackCooldown > 0) specialAttackCooldown--;
            
            LivingEntity target = getTarget();
            if (target != null && distanceToSqr(target) < 64 && specialAttackCooldown == 0) {
                performSpecialAttack(target);
                specialAttackCooldown = 100 + random.nextInt(60);
            }
        }
        
        // Particles
        if (level.isClientSide && tickCount % 10 == 0) {
            level.addParticle(ParticleTypes.CRIMSON_SPORE,
                getX() + (random.nextDouble() - 0.5),
                getY() + 1 + random.nextDouble(),
                getZ() + (random.nextDouble() - 0.5),
                0, 0.05, 0);
        }
    }
    
    private void performSpecialAttack(LivingEntity target) {
        Random rand = random;
        int attack = rand.nextInt(3);
        
        switch (attack) {
            case 0: // Blood Demon Art: Slash
                performSlashAttack(target);
                break;
            case 1: // Teleport behind
                performTeleportAttack(target);
                break;
            case 2: // AOE fear
                performFearAttack();
                break;
        }
    }
    
    private void performSlashAttack(LivingEntity target) {
        Vector3d dir = target.position().subtract(position()).normalize();
        
        target.hurt(DamageSource.mobAttack(this), 15.0F);
        target.setDeltaMovement(dir.scale(1.5).add(0, 0.5, 0));
        target.hurtMarked = true;
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            for (double d = 0; d < 5; d += 0.5) {
                Vector3d pos = position().add(dir.scale(d)).add(0, 1.5, 0);
                sw.sendParticles(ParticleTypes.CRIMSON_SPORE, pos.x, pos.y, pos.z, 5, 0.1, 0.1, 0.1, 0.05);
            }
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.5F);
    }
    
    private void performTeleportAttack(LivingEntity target) {
        // Teleport behind target
        Vector3d behind = target.position().subtract(target.getLookAngle().scale(2));
        teleportTo(behind.x, target.getY(), behind.z);
        
        target.hurt(DamageSource.mobAttack(this), 10.0F);
        
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.CRIMSON_SPORE,
                getX(), getY() + 1, getZ(), 30, 0.5, 1, 0.5, 0.1);
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 0.5F);
    }
    
    private void performFearAttack() {
        AxisAlignedBB area = getBoundingBox().inflate(8);
        List<PlayerEntity> players = level.getEntitiesOfClass(PlayerEntity.class, area);
        
        for (PlayerEntity player : players) {
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 60, 1));
            player.addEffect(new EffectInstance(Effects.WEAKNESS, 60, 0));
            player.addEffect(new EffectInstance(Effects.BLINDNESS, 60, 0));
        }
        
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.SQUID_INK,
                getX(), getY() + 1, getZ(), 100, 4, 2, 4, 0.1);
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.AMBIENT_CAVE, SoundCategory.HOSTILE, 2.0F, 0.3F);
    }
    
    @Override
    public boolean doHurtTarget(net.minecraft.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        
        if (hit && target instanceof LivingEntity) {
            // Life steal
            heal(3.0F);
            
            if (level instanceof ServerWorld) {
                ((ServerWorld) level).sendParticles(ParticleTypes.CRIMSON_SPORE,
                    target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.5, 0.3, 0.1);
            }
        }
        
        return hit;
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Extra damage from sunlight and fire
        if (source.isFire() || source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) {
            amount *= 2.0F;
        }
        
        // Nichirin sword does extra damage
        if (source.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) source.getEntity();
            ItemStack weapon = player.getMainHandItem();
            if (weapon.getItem().getDescriptionId().contains("nichirin")) {
                amount *= 1.5F;
                
                if (level instanceof ServerWorld) {
                    ((ServerWorld) level).sendParticles(ParticleTypes.CRIT,
                        getX(), getY() + 1, getZ(), 15, 0.5, 0.5, 0.5, 0.2);
                }
            }
        }
        
        return super.hurt(source, amount);
    }
    
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        
        // Drop demon blood
        int bloodCount = 1 + random.nextInt(3 + looting);
        this.spawnAtLocation(new ItemStack(ModItems.DEMON_BLOOD.get(), bloodCount));
        
        // Rare: Wisteria flower
        if (random.nextFloat() < 0.1F + looting * 0.05F) {
            this.spawnAtLocation(new ItemStack(ModItems.WISTERIA_FLOWER.get()));
        }
        
        // Very rare: Scarlet Crimson Ore
        if (random.nextFloat() < 0.05F + looting * 0.02F) {
            this.spawnAtLocation(new ItemStack(ModItems.SCARLET_CRIMSON_ORE.get()));
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("Enraged", isEnraged);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        isEnraged = nbt.getBoolean("Enraged");
    }
}
