package com.demonslayer.entity;

import com.demonslayer.init.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/**
 * Akaza - Upper Moon 3
 * Martial arts master demon
 */
public class AkazaEntity extends MonsterEntity {
    
    private final ServerBossInfo bossInfo = new ServerBossInfo(
        new StringTextComponent("猗窩座 Akaza - Upper Moon 3").withStyle(TextFormatting.DARK_RED),
        BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_6);
    
    private int comboCounter = 0;
    private int attackCooldown = 0;
    
    public AkazaEntity(EntityType<? extends MonsterEntity> type, World world) {
        super(type, world);
        this.xpReward = 300;
    }
    
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 12.0F));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!level.isClientSide) {
            bossInfo.setPercent(getHealth() / getMaxHealth());
            
            // Sunlight weakness
            if (level.isDay() && level.canSeeSky(blockPosition())) {
                hurt(DamageSource.ON_FIRE, 15.0F);
                setSecondsOnFire(3);
            }
            
            if (attackCooldown > 0) attackCooldown--;
            
            LivingEntity target = getTarget();
            if (target != null && attackCooldown == 0 && distanceToSqr(target) < 100) {
                performMartialArts(target);
                attackCooldown = 30 + random.nextInt(20);
            }
        }
        
        // Particles
        if (level.isClientSide && tickCount % 3 == 0) {
            level.addParticle(ParticleTypes.CRIMSON_SPORE,
                getX() + (random.nextDouble() - 0.5), getY() + 1.5, getZ() + (random.nextDouble() - 0.5),
                0, 0.05, 0);
        }
    }
    
    private void performMartialArts(LivingEntity target) {
        int attack = random.nextInt(4);
        
        switch (attack) {
            case 0: destructiveDeath(target); break;
            case 1: airType(target); break;
            case 2: comboSmash(target); break;
            case 3: annihilationType(target); break;
        }
    }
    
    // 破壊殺 - Destructive Death
    private void destructiveDeath(LivingEntity target) {
        Vector3d dir = target.position().subtract(position()).normalize();
        
        // Rush attack
        teleportTo(target.getX() - dir.x * 2, target.getY(), target.getZ() - dir.z * 2);
        target.hurt(DamageSource.mobAttack(this), 25.0F);
        
        // Shockwave
        AxisAlignedBB area = target.getBoundingBox().inflate(4);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this)) {
            entity.hurt(DamageSource.mobAttack(this), 10.0F);
            Vector3d knockback = entity.position().subtract(target.position()).normalize().scale(2);
            entity.setDeltaMovement(knockback.x, 0.5, knockback.z);
            entity.hurtMarked = true;
        }
        
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.EXPLOSION,
                target.getX(), target.getY() + 1, target.getZ(), 5, 1, 1, 1, 0);
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 1.2F);
        
        announceAttack("破壊殺・羅針！");
    }
    
    // 空式 - Air Type
    private void airType(LivingEntity target) {
        // Long range shockwave
        Vector3d dir = target.position().subtract(position()).normalize();
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            for (double d = 0; d < 12; d += 0.5) {
                Vector3d pos = position().add(dir.scale(d)).add(0, 1, 0);
                sw.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 5, 0.2, 0.2, 0.2, 0.1);
                
                AxisAlignedBB hitBox = new AxisAlignedBB(pos.x - 1, pos.y - 1, pos.z - 1,
                    pos.x + 1, pos.y + 1, pos.z + 1);
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != this)) {
                    entity.hurt(DamageSource.mobAttack(this).setMagic(), 15.0F);
                }
            }
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 2.0F, 0.5F);
        
        announceAttack("空式！");
    }
    
    // Rapid combo
    private void comboSmash(LivingEntity target) {
        for (int i = 0; i < 5; i++) {
            target.hurt(DamageSource.mobAttack(this), 8.0F);
        }
        
        comboCounter++;
        if (comboCounter >= 3) {
            // Finish with heavy attack
            target.hurt(DamageSource.mobAttack(this), 30.0F);
            target.setDeltaMovement(0, 1.5, 0);
            target.hurtMarked = true;
            comboCounter = 0;
        }
        
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + 1, target.getZ(), 30, 0.5, 0.5, 0.5, 0.3);
        }
        
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 1.5F, 1.5F);
        
        announceAttack("連撃！");
    }
    
    // 滅式 - Annihilation Type
    private void annihilationType(LivingEntity target) {
        AxisAlignedBB area = getBoundingBox().inflate(6);
        
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this)) {
            entity.hurt(DamageSource.mobAttack(this).bypassArmor(), 35.0F);
            entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 2));
        }
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1, getZ(), 100, 3, 2, 3, 0.2);
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 2.0F, 0.5F);
        
        announceAttack("滅式！");
    }
    
    private void announceAttack(String name) {
        for (PlayerEntity player : level.getEntitiesOfClass(PlayerEntity.class, getBoundingBox().inflate(30))) {
            player.displayClientMessage(
                new StringTextComponent("Akaza: \"" + name + "\"")
                    .withStyle(TextFormatting.RED).withStyle(TextFormatting.BOLD), true);
        }
    }
    
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(new ItemStack(ModItems.DEMON_BLOOD.get(), 8 + random.nextInt(5)));
        this.spawnAtLocation(new ItemStack(ModItems.SCARLET_CRIMSON_INGOT.get(), 3 + random.nextInt(3)));
    }
    
    @Override
    public void startSeenByPlayer(ServerPlayerEntity player) {
        super.startSeenByPlayer(player);
        bossInfo.addPlayer(player);
    }
    
    @Override
    public void stopSeenByPlayer(ServerPlayerEntity player) {
        super.stopSeenByPlayer(player);
        bossInfo.removePlayer(player);
    }
}
