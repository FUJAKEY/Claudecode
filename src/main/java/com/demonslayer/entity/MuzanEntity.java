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
import net.minecraft.nbt.CompoundNBT;
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
import java.util.Random;

/**
 * Muzan Kibutsuji - The King of Demons
 */
public class MuzanEntity extends MonsterEntity {
    
    private final ServerBossInfo bossInfo = new ServerBossInfo(
        new StringTextComponent("鬼舞辻無惨 Muzan Kibutsuji").withStyle(TextFormatting.DARK_RED),
        BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_10);
    
    private int phase = 1;
    private int attackCooldown = 0;
    private int specialCooldown = 0;
    private boolean isTransforming = false;
    
    public MuzanEntity(EntityType<? extends MonsterEntity> type, World world) {
        super(type, world);
        this.xpReward = 500;
    }
    
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.ATTACK_DAMAGE, 25.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.5D, false));
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
            float healthPercent = getHealth() / getMaxHealth();
            if (healthPercent < 0.3 && phase < 3) {
                enterPhase3();
            } else if (healthPercent < 0.6 && phase < 2) {
                enterPhase2();
            }
            
            // Extreme sunlight weakness
            if (level.isDay() && level.canSeeSky(blockPosition())) {
                hurt(DamageSource.ON_FIRE, 20.0F);
                setSecondsOnFire(5);
            }
            
            // Night regeneration
            if (!level.isDay() && tickCount % 20 == 0) {
                heal(5.0F);
            }
            
            // Special attacks
            if (attackCooldown > 0) attackCooldown--;
            if (specialCooldown > 0) specialCooldown--;
            
            LivingEntity target = getTarget();
            if (target != null && specialCooldown == 0) {
                performSpecialAttack(target);
                specialCooldown = 60 + random.nextInt(40);
            }
        }
        
        // Particles
        if (level.isClientSide && tickCount % 5 == 0) {
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.CRIMSON_SPORE,
                    getX() + (random.nextDouble() - 0.5) * 2,
                    getY() + random.nextDouble() * 2,
                    getZ() + (random.nextDouble() - 0.5) * 2,
                    0, 0.05, 0);
            }
        }
    }
    
    private void enterPhase2() {
        phase = 2;
        heal(100.0F);
        
        addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 9999, 1));
        addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 9999, 1));
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            sw.sendParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + 1, getZ(), 200, 3, 2, 3, 0.5);
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.WITHER_SPAWN, SoundCategory.HOSTILE, 2.0F, 0.5F);
        
        // Announce
        for (PlayerEntity player : level.getEntitiesOfClass(PlayerEntity.class, getBoundingBox().inflate(50))) {
            player.displayClientMessage(
                new StringTextComponent("Muzan: \"You dare challenge me?!\"")
                    .withStyle(TextFormatting.DARK_RED).withStyle(TextFormatting.ITALIC), false);
        }
    }
    
    private void enterPhase3() {
        phase = 3;
        heal(200.0F);
        
        addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 9999, 2));
        addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 9999, 2));
        addEffect(new EffectInstance(Effects.REGENERATION, 9999, 1));
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1, getZ(), 300, 5, 3, 5, 0.5);
            sw.sendParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + 1, getZ(), 500, 10, 5, 10, 0.5);
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 3.0F, 0.3F);
        
        for (PlayerEntity player : level.getEntitiesOfClass(PlayerEntity.class, getBoundingBox().inflate(50))) {
            player.displayClientMessage(
                new StringTextComponent("Muzan: \"I AM THE KING OF DEMONS!\"")
                    .withStyle(TextFormatting.DARK_RED).withStyle(TextFormatting.BOLD), false);
        }
    }
    
    private void performSpecialAttack(LivingEntity target) {
        int attack = random.nextInt(5);
        
        switch (attack) {
            case 0: bloodDemonArt(target); break;
            case 1: tentacleAttack(target); break;
            case 2: shockwave(); break;
            case 3: teleportBehind(target); break;
            case 4: summonMinions(); break;
        }
    }
    
    private void bloodDemonArt(LivingEntity target) {
        Vector3d dir = target.position().subtract(position()).normalize();
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            for (double d = 0; d < 15; d += 0.5) {
                Vector3d pos = position().add(dir.scale(d)).add(0, 1.5, 0);
                sw.sendParticles(ParticleTypes.CRIMSON_SPORE, pos.x, pos.y, pos.z, 10, 0.2, 0.2, 0.2, 0.1);
                
                AxisAlignedBB hitBox = new AxisAlignedBB(pos.x - 0.5, pos.y - 0.5, pos.z - 0.5,
                    pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitBox,
                    e -> e != this);
                for (LivingEntity entity : targets) {
                    entity.hurt(DamageSource.mobAttack(this).bypassArmor(), 15.0F * phase);
                    entity.addEffect(new EffectInstance(Effects.WITHER, 100, 1));
                }
            }
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.BLAZE_SHOOT, SoundCategory.HOSTILE, 1.5F, 0.3F);
    }
    
    private void tentacleAttack(LivingEntity target) {
        AxisAlignedBB area = getBoundingBox().inflate(8);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this);
        
        for (LivingEntity entity : targets) {
            entity.hurt(DamageSource.mobAttack(this), 20.0F * phase);
            Vector3d pull = position().subtract(entity.position()).normalize().scale(2);
            entity.setDeltaMovement(pull);
            entity.hurtMarked = true;
        }
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2 / 8) * i;
                for (int d = 0; d < 8; d++) {
                    double x = getX() + Math.cos(angle) * d;
                    double z = getZ() + Math.sin(angle) * d;
                    sw.sendParticles(ParticleTypes.CRIMSON_SPORE, x, getY() + 1, z, 5, 0.1, 0.5, 0.1, 0.05);
                }
            }
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.SLIME_SQUISH, SoundCategory.HOSTILE, 2.0F, 0.5F);
    }
    
    private void shockwave() {
        AxisAlignedBB area = getBoundingBox().inflate(12);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != this);
        
        for (LivingEntity entity : targets) {
            entity.hurt(DamageSource.mobAttack(this).setMagic(), 25.0F);
            Vector3d knockback = entity.position().subtract(position()).normalize().scale(3);
            entity.setDeltaMovement(knockback.x, 1.0, knockback.z);
            entity.hurtMarked = true;
        }
        
        if (level instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) level;
            sw.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 5, 0, 0, 0, 0);
            sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1, getZ(), 200, 6, 2, 6, 0.3);
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0F, 0.5F);
    }
    
    private void teleportBehind(LivingEntity target) {
        Vector3d behind = target.position().subtract(target.getLookAngle().scale(3));
        teleportTo(behind.x, target.getY(), behind.z);
        
        target.hurt(DamageSource.mobAttack(this).bypassArmor(), 30.0F);
        
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.CRIMSON_SPORE,
                getX(), getY() + 1, getZ(), 50, 1, 2, 1, 0.2);
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.5F, 0.3F);
    }
    
    private void summonMinions() {
        if (!(level instanceof ServerWorld)) return;
        
        for (int i = 0; i < 3; i++) {
            DemonEntity demon = new DemonEntity(EntityType.ZOMBIE, level); // Placeholder
            double angle = (Math.PI * 2 / 3) * i;
            demon.setPos(getX() + Math.cos(angle) * 4, getY(), getZ() + Math.sin(angle) * 4);
            // Would need proper entity type registration
        }
        
        level.playSound(null, getX(), getY(), getZ(),
            SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundCategory.HOSTILE, 2.0F, 0.5F);
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Nichirin sword bonus
        if (source.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) source.getEntity();
            if (player.getMainHandItem().getItem().getDescriptionId().contains("nichirin")) {
                amount *= 2.0F; // Double damage from Nichirin
            }
        }
        
        // Reduce all other damage
        if (!source.isBypassArmor()) {
            amount *= 0.5F;
        }
        
        return super.hurt(source, amount);
    }
    
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        
        // Guaranteed drops
        this.spawnAtLocation(new ItemStack(ModItems.DEMON_BLOOD.get(), 10 + random.nextInt(10)));
        this.spawnAtLocation(new ItemStack(ModItems.SCARLET_CRIMSON_INGOT.get(), 5 + random.nextInt(5)));
        
        // Rare: Black Nichirin Sword
        if (random.nextFloat() < 0.2F + looting * 0.1F) {
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
    
    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Phase", phase);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        phase = nbt.getInt("Phase");
        if (phase < 1) phase = 1;
    }
    
    @Override
    public boolean canChangeDimensions() {
        return false;
    }
}
