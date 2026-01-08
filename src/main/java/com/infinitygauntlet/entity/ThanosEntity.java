package com.infinitygauntlet.entity;

import com.infinitygauntlet.init.ModItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ThanosEntity extends MonsterEntity {
    
    private static final DataParameter<Integer> ATTACK_PHASE = EntityDataManager.defineId(ThanosEntity.class, DataSerializers.INT);
    private static final DataParameter<Boolean> HAS_GAUNTLET = EntityDataManager.defineId(ThanosEntity.class, DataSerializers.BOOLEAN);
    
    private final ServerBossInfo bossInfo = new ServerBossInfo(
        new StringTextComponent("THANOS").withStyle(TextFormatting.DARK_PURPLE).withStyle(TextFormatting.BOLD),
        BossInfo.Color.PURPLE,
        BossInfo.Overlay.PROGRESS
    );
    
    private int attackCooldown = 0;
    private int specialAttackCooldown = 0;
    private int currentAttack = 0;
    
    public ThanosEntity(EntityType<? extends MonsterEntity> type, World world) {
        super(type, world);
        this.xpReward = 500;
    }
    
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 500.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35D)
            .add(Attributes.ATTACK_DAMAGE, 15.0D)
            .add(Attributes.ARMOR, 10.0D)
            .add(Attributes.ARMOR_TOUGHNESS, 5.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
            .add(Attributes.FOLLOW_RANGE, 64.0D);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new ThanosAttackGoal(this));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.0D, 32.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomWalkingGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 16.0F));
        this.goalSelector.addGoal(5, new LookRandomlyGoal(this));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACK_PHASE, 0);
        this.entityData.define(HAS_GAUNTLET, true);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!this.level.isClientSide) {
            // Boss bar update
            this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
            
            // Attack cooldowns
            if (attackCooldown > 0) attackCooldown--;
            if (specialAttackCooldown > 0) specialAttackCooldown--;
            
            // Rage mode at low health
            if (this.getHealth() < this.getMaxHealth() * 0.3) {
                if (!this.hasEffect(Effects.DAMAGE_BOOST)) {
                    this.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 100, 2));
                    this.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 100, 1));
                }
            }
            
            // Particles
            if (this.level.getGameTime() % 5 == 0) {
                ServerWorld sw = (ServerWorld) this.level;
                sw.sendParticles(ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1.5, this.getZ(),
                    5, 0.5, 1, 0.5, 0.1);
            }
            
            // Special attacks
            if (specialAttackCooldown <= 0 && this.getTarget() != null) {
                performSpecialAttack();
                specialAttackCooldown = 100 + random.nextInt(100);
            }
        }
    }
    
    private void performSpecialAttack() {
        if (this.level.isClientSide) return;
        
        ServerWorld world = (ServerWorld) this.level;
        currentAttack = (currentAttack + 1) % 4;
        
        switch (currentAttack) {
            case 0: // Power Stone Blast
                powerStoneBlast(world);
                break;
            case 1: // Space Stone Pull
                spaceStonePull(world);
                break;
            case 2: // Reality Stone
                realityStoneAttack(world);
                break;
            case 3: // Ground Pound
                groundPound(world);
                break;
        }
    }
    
    private void powerStoneBlast(ServerWorld world) {
        LivingEntity target = this.getTarget();
        if (target == null) return;
        
        // Beam of particles towards target
        Vector3d direction = target.position().subtract(this.position()).normalize();
        for (int i = 0; i < 20; i++) {
            Vector3d pos = this.position().add(direction.scale(i));
            world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                pos.x, pos.y + 1.5, pos.z, 10, 0.2, 0.2, 0.2, 0.05);
        }
        
        // Damage in line
        AxisAlignedBB area = this.getBoundingBox().inflate(20, 5, 20);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, 
            e -> e != this && this.distanceTo(e) < 20);
        
        for (LivingEntity entity : entities) {
            Vector3d toEntity = entity.position().subtract(this.position()).normalize();
            double dot = direction.dot(toEntity);
            if (dot > 0.7) { // In the beam direction
                entity.hurt(DamageSource.mobAttack(this), 20.0F);
                entity.setSecondsOnFire(5);
            }
        }
        
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0F, 0.5F);
    }
    
    private void spaceStonePull(ServerWorld world) {
        // Pull all nearby players towards Thanos
        AxisAlignedBB area = this.getBoundingBox().inflate(15);
        List<PlayerEntity> players = world.getEntitiesOfClass(PlayerEntity.class, area);
        
        for (PlayerEntity player : players) {
            Vector3d pull = this.position().subtract(player.position()).normalize().scale(1.5);
            player.setDeltaMovement(player.getDeltaMovement().add(pull));
            player.hurtMarked = true;
            
            world.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 1, 0.5, 0.5);
        }
        
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 2.0F, 0.3F);
    }
    
    private void realityStoneAttack(ServerWorld world) {
        // Apply random negative effects
        AxisAlignedBB area = this.getBoundingBox().inflate(10);
        List<PlayerEntity> players = world.getEntitiesOfClass(PlayerEntity.class, area);
        
        Random rand = new Random();
        for (PlayerEntity player : players) {
            switch (rand.nextInt(4)) {
                case 0: player.addEffect(new EffectInstance(Effects.BLINDNESS, 100, 0)); break;
                case 1: player.addEffect(new EffectInstance(Effects.CONFUSION, 100, 0)); break;
                case 2: player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, 2)); break;
                case 3: player.addEffect(new EffectInstance(Effects.WEAKNESS, 100, 1)); break;
            }
            
            world.sendParticles(ParticleTypes.CRIMSON_SPORE,
                player.getX(), player.getY() + 1, player.getZ(), 50, 1, 1, 1, 0.1);
        }
        
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ILLUSIONER_CAST_SPELL, SoundCategory.HOSTILE, 2.0F, 0.5F);
    }
    
    private void groundPound(ServerWorld world) {
        // Jump and slam (simplified - just damage nearby)
        this.setDeltaMovement(0, 0.8, 0);
        
        // Schedule ground impact
        AxisAlignedBB area = this.getBoundingBox().inflate(8);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != this);
        
        for (LivingEntity entity : entities) {
            entity.hurt(DamageSource.mobAttack(this), 15.0F);
            Vector3d knockback = entity.position().subtract(this.position()).normalize().scale(2);
            entity.setDeltaMovement(knockback.x, 0.5, knockback.z);
            entity.hurtMarked = true;
        }
        
        world.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
            this.getX(), this.getY(), this.getZ(), 3, 0, 0, 0, 0);
        
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 0.5F);
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Reduce some damage types
        if (source.isFire() || source.isExplosion()) {
            amount *= 0.5F;
        }
        if (source.isMagic()) {
            amount *= 0.7F;
        }
        
        // Teleport away from damage sometimes
        if (this.getHealth() < this.getMaxHealth() * 0.5 && random.nextFloat() < 0.2) {
            double newX = this.getX() + (random.nextDouble() - 0.5) * 10;
            double newZ = this.getZ() + (random.nextDouble() - 0.5) * 10;
            this.teleportTo(newX, this.getY(), newZ);
            
            if (this.level instanceof ServerWorld) {
                ((ServerWorld) this.level).sendParticles(ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1, this.getZ(), 50, 0.5, 1, 0.5, 0.5);
            }
        }
        
        return super.hurt(source, amount);
    }
    
    @Override
    public void die(DamageSource source) {
        super.die(source);
        
        if (!this.level.isClientSide) {
            // Drop gauntlet core and vibranium
            this.spawnAtLocation(new ItemStack(ModItems.GAUNTLET_CORE.get(), 1));
            this.spawnAtLocation(new ItemStack(ModItems.VIBRANIUM_INGOT.get(), 3 + random.nextInt(5)));
            
            // Random stone drop
            if (random.nextFloat() < 0.1) {
                switch (random.nextInt(6)) {
                    case 0: this.spawnAtLocation(new ItemStack(ModItems.SPACE_STONE.get())); break;
                    case 1: this.spawnAtLocation(new ItemStack(ModItems.TIME_STONE.get())); break;
                    case 2: this.spawnAtLocation(new ItemStack(ModItems.REALITY_STONE.get())); break;
                    case 3: this.spawnAtLocation(new ItemStack(ModItems.POWER_STONE.get())); break;
                    case 4: this.spawnAtLocation(new ItemStack(ModItems.MIND_STONE.get())); break;
                    case 5: this.spawnAtLocation(new ItemStack(ModItems.SOUL_STONE.get())); break;
                }
            }
            
            // Epic death effect
            if (this.level instanceof ServerWorld) {
                ServerWorld sw = (ServerWorld) this.level;
                sw.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1, this.getZ(), 200, 2, 2, 2, 0.5);
                sw.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY() + 1, this.getZ(), 100, 3, 3, 3, 0.2);
            }
        }
    }
    
    @Override
    public void startSeenByPlayer(ServerPlayerEntity player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }
    
    @Override
    public void stopSeenByPlayer(ServerPlayerEntity player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }
    
    @Override
    public boolean canChangeDimensions() {
        return false;
    }
    
    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
    
    // Custom attack goal
    static class ThanosAttackGoal extends MeleeAttackGoal {
        private final ThanosEntity thanos;
        
        public ThanosAttackGoal(ThanosEntity thanos) {
            super(thanos, 1.2D, true);
            this.thanos = thanos;
        }
        
        @Override
        protected void checkAndPerformAttack(LivingEntity target, double distance) {
            if (thanos.attackCooldown <= 0 && distance < this.getAttackReachSqr(target)) {
                thanos.attackCooldown = 20;
                this.mob.doHurtTarget(target);
                
                // Knockback
                Vector3d knockback = target.position().subtract(thanos.position()).normalize().scale(1.5);
                target.setDeltaMovement(knockback.x, 0.4, knockback.z);
                target.hurtMarked = true;
            }
        }
    }
}
