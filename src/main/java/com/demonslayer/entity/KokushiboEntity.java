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
 * Kokushibo - Upper Moon 1
 * Moon Breathing master, strongest Upper Moon
 */
public class KokushiboEntity extends MonsterEntity {
    
    private final ServerBossInfo bossInfo = new ServerBossInfo(
        new StringTextComponent("黒死牟 Kokushibo - Upper Moon 1").withStyle(TextFormatting.DARK_PURPLE),
        BossInfo.Color.PURPLE, BossInfo.Overlay.NOTCHED_10);
    
    private int phase = 1;
    private int attackCooldown = 0;
    
    public KokushiboEntity(EntityType<? extends MonsterEntity> type, World world) {
        super(type, world);
        this.xpReward = 400;
    }
    
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 900.0D)
                .add(Attributes.ATTACK_DAMAGE, 22.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.42D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 16.0F));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!level.isClientSide) {
            bossInfo.setPercent(getHealth() / getMaxHealth());
            
            // Phase transitions
            if (getHealth() < getMaxHealth() * 0.5 && phase < 2) {
                phase = 2;
                addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 9999, 1));
                addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 9999, 1));
            }
            
            // Sunlight
            if (level.isDay() && level.canSeeSky(blockPosition())) {
                hurt(DamageSource.ON_FIRE, 10.0F);
            }
            
            if (attackCooldown > 0) attackCooldown--;
            
            LivingEntity target = getTarget();
            if (target != null && attackCooldown == 0) {
                performMoonBreathing(target);
                attackCooldown = 25 + random.nextInt(15);
            }
        }
        
        // Particles
        if (level.isClientSide && tickCount % 2 == 0) {
            level.addParticle(ParticleTypes.END_ROD,
                getX() + (random.nextDouble() - 0.5) * 2, getY() + 1 + random.nextDouble(), 
                getZ() + (random.nextDouble() - 0.5) * 2, 0, -0.05, 0);
        }
    }
    
    private void performMoonBreathing(LivingEntity target) {
        int attack = random.nextInt(phase == 2 ? 6 : 4);
        
        switch (attack) {
            case 0: darkMoon(target); break;
            case 1: crescentMoonBlades(target); break;
            case 2: moonDragonRingtail(target); break;
            case 3: perpetualNight(target); break;
            case 4: moongazerPrism(target); break;
            case 5: sixfoldCrescentMoon(target); break;
        }
    }
    
    // 闇月 - Dark Moon
    private void darkMoon(LivingEntity target) {
        Vector3d dir = target.position().subtract(position()).normalize();
        
        // Fast slash
        teleportTo(target.getX() + dir.x * 2, target.getY(), target.getZ() + dir.z * 2);
        target.hurt(DamageSource.mobAttack(this).bypassArmor(), 28.0F);
        
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.SWEEP_ATTACK,
                target.getX(), target.getY() + 1, target.getZ(), 10, 1, 1, 1, 0);
            ((ServerWorld) level).sendParticles(ParticleTypes.END_ROD,
                target.getX(), target.getY() + 1, target.getZ(), 30, 1, 1, 1, 0.2);
        }
        
        announceForm("壱ノ型 闇月・宵の宮");
    }
    
    // Crescent Moon Blades
    private void crescentMoonBlades(LivingEntity target) {
        // Multiple crescent projectiles
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            for (int i = 0; i < 5; i++) {
                double angle = (Math.PI * 2 / 5) * i + tickCount * 0.1;
                double offsetX = Math.cos(angle) * 3;
                double offsetZ = Math.sin(angle) * 3;
                
                Vector3d pos = target.position().add(offsetX, 1, offsetZ);
                sw.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 10, 0.3, 0.3, 0.3, 0.1);
            }
        }
        
        AxisAlignedBB area = target.getBoundingBox().inflate(4);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this)) {
            entity.hurt(DamageSource.mobAttack(this), 20.0F);
        }
        
        announceForm("弐ノ型 珠華ノ弄月");
    }
    
    // Moon Dragon Ringtail
    private void moonDragonRingtail(LivingEntity target) {
        // Circular slash attack
        AxisAlignedBB area = getBoundingBox().inflate(8);
        
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this)) {
            entity.hurt(DamageSource.mobAttack(this), 25.0F);
            Vector3d toCenter = position().subtract(entity.position()).normalize();
            entity.setDeltaMovement(toCenter.x * 1.5, 0.8, toCenter.z * 1.5);
            entity.hurtMarked = true;
        }
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            for (int i = 0; i < 360; i += 10) {
                double angle = Math.toRadians(i);
                double x = getX() + Math.cos(angle) * 5;
                double z = getZ() + Math.sin(angle) * 5;
                sw.sendParticles(ParticleTypes.END_ROD, x, getY() + 1, z, 5, 0.1, 0.5, 0.1, 0.05);
            }
        }
        
        announceForm("伍ノ型 月魄災禍");
    }
    
    // Perpetual Night
    private void perpetualNight(LivingEntity target) {
        // Apply blindness and damage
        AxisAlignedBB area = getBoundingBox().inflate(12);
        
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this)) {
            entity.addEffect(new EffectInstance(Effects.BLINDNESS, 100, 0));
            entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 60, 1));
            entity.hurt(DamageSource.mobAttack(this).setMagic(), 15.0F);
        }
        
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.SQUID_INK,
                getX(), getY() + 1, getZ(), 200, 6, 3, 6, 0.1);
        }
        
        announceForm("陸ノ型 常夜孤月・無間");
    }
    
    // Phase 2 only: Moongazer's Prism
    private void moongazerPrism(LivingEntity target) {
        // Massive damage cone
        Vector3d dir = target.position().subtract(position()).normalize();
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            for (double d = 0; d < 15; d += 0.5) {
                double spread = d * 0.3;
                for (int i = -2; i <= 2; i++) {
                    Vector3d pos = position().add(dir.scale(d)).add(dir.cross(new Vector3d(0, 1, 0)).scale(i * spread / 2)).add(0, 1.5, 0);
                    sw.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 3, 0.1, 0.1, 0.1, 0.02);
                }
            }
        }
        
        // Hit all in cone
        AxisAlignedBB area = getBoundingBox().expandTowards(dir.scale(15)).inflate(3);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this)) {
            entity.hurt(DamageSource.mobAttack(this).bypassArmor(), 40.0F);
        }
        
        announceForm("拾肆ノ型 兇変・天満繊月");
    }
    
    // Phase 2 only: Sixfold Crescent Moon
    private void sixfoldCrescentMoon(LivingEntity target) {
        // Six rapid slashes
        for (int i = 0; i < 6; i++) {
            target.hurt(DamageSource.mobAttack(this), 12.0F);
        }
        target.setDeltaMovement(0, 2, 0);
        target.hurtMarked = true;
        
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.END_ROD,
                target.getX(), target.getY() + 1, target.getZ(), 100, 1, 2, 1, 0.5);
        }
        
        announceForm("拾陸ノ型 月虹・片割れ月");
    }
    
    private void announceForm(String name) {
        for (PlayerEntity player : level.getEntitiesOfClass(PlayerEntity.class, getBoundingBox().inflate(40))) {
            player.displayClientMessage(
                new StringTextComponent("【月の呼吸】" + name)
                    .withStyle(TextFormatting.DARK_PURPLE).withStyle(TextFormatting.BOLD), true);
        }
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 2.0F, 0.3F);
    }
    
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(new ItemStack(ModItems.DEMON_BLOOD.get(), 15 + random.nextInt(10)));
        this.spawnAtLocation(new ItemStack(ModItems.SCARLET_CRIMSON_INGOT.get(), 5 + random.nextInt(5)));
        // Rare drop: Black Nichirin
        if (random.nextFloat() < 0.3F) {
            this.spawnAtLocation(new ItemStack(ModItems.NICHIRIN_SWORD_BLACK.get()));
        }
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
