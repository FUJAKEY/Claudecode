package com.demonslayer.breathing;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/**
 * Individual Breathing Forms with unique attacks
 */
public enum BreathingForm {
    // ========== WATER BREATHING ==========
    WATER_SURFACE_SLASH("First Form: Water Surface Slash", 40, 8.0F, 3.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performSlash(world, player, damage * power, range * power, ParticleTypes.SPLASH, "水面斬り！");
        }
    },
    
    WATER_WHEEL("Second Form: Water Wheel", 60, 12.0F, 4.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performSpin(world, player, damage * power, range * power, ParticleTypes.DRIPPING_WATER, "水車！");
        }
    },
    
    FLOWING_DANCE("Third Form: Flowing Dance", 50, 10.0F, 5.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performDash(world, player, damage * power, range * power, ParticleTypes.SPLASH, "流流舞い！");
        }
    },
    
    STRIKING_TIDE("Fourth Form: Striking Tide", 80, 15.0F, 6.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performMultiHit(world, player, damage * power, range * power, ParticleTypes.BUBBLE, "打ち潮！");
        }
    },
    
    WATERFALL_BASIN("Fifth Form: Waterfall Basin", 100, 20.0F, 3.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performDownSlash(world, player, damage * power, ParticleTypes.FALLING_WATER, "干天の慈雨！");
        }
    },
    
    // ========== FLAME BREATHING ==========
    UNKNOWING_FIRE("First Form: Unknowing Fire", 50, 10.0F, 4.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performDash(world, player, damage * power, range * power, ParticleTypes.FLAME, "不知火！");
        }
    },
    
    RISING_SCORCHING_SUN("Second Form: Rising Scorching Sun", 60, 12.0F, 5.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performUppercut(world, player, damage * power, ParticleTypes.FLAME, "昇り炎天！");
        }
    },
    
    BLOOMING_FLAME_UNDULATION("Third Form: Blazing Universe", 70, 14.0F, 6.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performSpin(world, player, damage * power, range * power, ParticleTypes.LAVA, "気炎万象！");
        }
    },
    
    FLAME_TIGER("Fourth Form: Flame Tiger", 90, 18.0F, 8.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performProjectile(world, player, damage * power, range * power, ParticleTypes.FLAME, "炎虎！");
        }
    },
    
    RENGOKU("Ninth Form: Rengoku", 150, 30.0F, 10.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performUltimate(world, player, damage * power, range * power, ParticleTypes.SOUL_FIRE_FLAME, "煉獄！");
        }
    },
    
    // ========== THUNDER BREATHING ==========
    THUNDERCLAP_FLASH("First Form: Thunderclap and Flash", 30, 15.0F, 10.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performFlash(world, player, damage * power, range * power, "霹靂一閃！");
        }
    },
    
    RICE_SPIRIT("Second Form: Rice Spirit", 50, 12.0F, 5.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performMultiHit(world, player, damage * power, range * power, ParticleTypes.ENCHANTED_HIT, "稲魂！");
        }
    },
    
    THUNDER_SWARM("Third Form: Thunder Swarm", 70, 10.0F, 6.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performAOE(world, player, damage * power, range * power, ParticleTypes.ENCHANTED_HIT, "聚蚊成雷！");
        }
    },
    
    DISTANT_THUNDER("Fourth Form: Distant Thunder", 80, 8.0F, 8.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performProjectile(world, player, damage * power, range * power, ParticleTypes.ENCHANTED_HIT, "遠雷！");
        }
    },
    
    HEAT_LIGHTNING("Fifth Form: Heat Lightning", 100, 20.0F, 4.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performUppercut(world, player, damage * power, ParticleTypes.ENCHANTED_HIT, "熱界雷！");
        }
    },
    
    // ========== WIND BREATHING ==========
    DUST_WHIRLWIND_CUTTER("First Form: Dust Whirlwind Cutter", 50, 10.0F, 5.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performSpin(world, player, damage * power, range * power, ParticleTypes.CLOUD, "塵旋風・削ぎ！");
        }
    },
    
    CLAWS_PURIFYING_WIND("Second Form: Claws Purifying Wind", 60, 12.0F, 4.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performMultiHit(world, player, damage * power, range * power, ParticleTypes.CLOUD, "爪々・科戸風！");
        }
    },
    
    CLEAN_STORM_WIND_TREE("Third Form: Clean Storm Wind Tree", 70, 14.0F, 6.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performAOE(world, player, damage * power, range * power, ParticleTypes.CLOUD, "晴嵐風樹！");
        }
    },
    
    RISING_DUST_STORM("Fourth Form: Rising Dust Storm", 80, 16.0F, 7.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performUppercut(world, player, damage * power, ParticleTypes.CLOUD, "昇上砂塵嵐！");
        }
    },
    
    COLD_MOUNTAIN_WIND("Fifth Form: Cold Mountain Wind", 100, 20.0F, 5.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performSlash(world, player, damage * power, range * power, ParticleTypes.ITEM_SNOWBALL, "木枯らし颪！");
        }
    },
    
    // ========== MIST BREATHING ==========
    OBSCURING_CLOUDS("First Form: Obscuring Clouds", 40, 8.0F, 6.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performMist(world, player, range * power, "朧！");
        }
    },
    
    EIGHT_LAYERED_MIST("Second Form: Eight-Layered Mist", 60, 12.0F, 5.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performMultiHit(world, player, damage * power, range * power, ParticleTypes.CLOUD, "八重霞！");
        }
    },
    
    SCATTERING_MIST_SPLASH("Third Form: Scattering Mist", 70, 14.0F, 7.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performAOE(world, player, damage * power, range * power, ParticleTypes.CLOUD, "霞散の飛沫！");
        }
    },
    
    SHIFTING_FLOW_SLASH("Fourth Form: Shifting Flow Slash", 80, 16.0F, 4.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performDash(world, player, damage * power, range * power, ParticleTypes.CLOUD, "移流斬り！");
        }
    },
    
    // ========== LOVE BREATHING ==========
    SHIVERS_FIRST_LOVE("First Form: Shivers of First Love", 50, 10.0F, 5.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performWhip(world, player, damage * power, range * power, ParticleTypes.HEART, "初恋のわななき！");
        }
    },
    
    LOVE_PANGS("Second Form: Love Pangs", 60, 12.0F, 6.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performMultiHit(world, player, damage * power, range * power, ParticleTypes.HEART, "懊悩巡る恋！");
        }
    },
    
    CATLOVE_SHOWER("Third Form: Catlove Shower", 70, 14.0F, 7.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performAOE(world, player, damage * power, range * power, ParticleTypes.HEART, "恋猫しぐれ！");
        }
    },
    
    SWAYING_LOVE_WILDCLAW("Fifth Form: Swaying Love Wildclaw", 90, 18.0F, 8.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performWhip(world, player, damage * power, range * power, ParticleTypes.HEART, "揺らめく恋情・乱れ爪！");
        }
    },
    
    // ========== SUN BREATHING ==========
    DANCE("First Form: Dance", 60, 15.0F, 5.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performSunDance(world, player, damage * power, range * power, "円舞！");
        }
    },
    
    CLEAR_BLUE_SKY("Second Form: Clear Blue Sky", 70, 16.0F, 6.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performSpin(world, player, damage * power, range * power, ParticleTypes.END_ROD, "碧羅の天！");
        }
    },
    
    RAGING_SUN("Third Form: Raging Sun", 80, 18.0F, 5.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performDualSlash(world, player, damage * power, ParticleTypes.END_ROD, "烈日紅鏡！");
        }
    },
    
    BURNING_BONES_SUMMER_SUN("Fourth Form: Burning Bones", 100, 22.0F, 4.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performDownSlash(world, player, damage * power, ParticleTypes.END_ROD, "灼骨炎陽！");
        }
    },
    
    THIRTEENTH_FORM("Thirteenth Form", 200, 50.0F, 10.0F) {
        @Override
        public void execute(World world, PlayerEntity player, float power, TextFormatting color) {
            performUltimate(world, player, damage * power, range * power, ParticleTypes.END_ROD, "拾参ノ型！");
            player.hurt(DamageSource.MAGIC, 10.0F); // Cost
        }
    };
    
    protected final String name;
    protected final int cooldown;
    protected final float damage;
    protected final float range;
    
    BreathingForm(String name, int cooldown, float damage, float range) {
        this.name = name;
        this.cooldown = cooldown;
        this.damage = damage;
        this.range = range;
    }
    
    public String getName() { return name; }
    public int getCooldown() { return cooldown; }
    public float getDamage() { return damage; }
    public float getRange() { return range; }
    
    public abstract void execute(World world, PlayerEntity player, float power, TextFormatting color);
    
    // ========== HELPER METHODS ==========
    
    protected void performSlash(World world, PlayerEntity player, float damage, float range, 
                                 net.minecraft.particles.IParticleData particle, String callout) {
        Vector3d look = player.getLookAngle();
        AxisAlignedBB area = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player), damage);
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (double d = 0; d < range; d += 0.5) {
                Vector3d pos = player.position().add(look.scale(d)).add(0, 1.5, 0);
                sw.sendParticles(particle, pos.x, pos.y, pos.z, 5, 0.2, 0.2, 0.2, 0.05);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.5F, 1.2F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performSpin(World world, PlayerEntity player, float damage, float range,
                                net.minecraft.particles.IParticleData particle, String callout) {
        AxisAlignedBB area = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player), damage);
            Vector3d knockback = target.position().subtract(player.position()).normalize().scale(1.5);
            target.setDeltaMovement(knockback.x, 0.5, knockback.z);
            target.hurtMarked = true;
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (int i = 0; i < 360; i += 15) {
                double angle = Math.toRadians(i);
                double x = player.getX() + Math.cos(angle) * range;
                double z = player.getZ() + Math.sin(angle) * range;
                sw.sendParticles(particle, x, player.getY() + 1, z, 5, 0.2, 0.5, 0.2, 0.05);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.5F, 0.8F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performDash(World world, PlayerEntity player, float damage, float range,
                                net.minecraft.particles.IParticleData particle, String callout) {
        Vector3d look = player.getLookAngle();
        
        // Teleport forward
        double oldX = player.getX(), oldY = player.getY(), oldZ = player.getZ();
        player.teleportTo(player.getX() + look.x * range, player.getY(), player.getZ() + look.z * range);
        
        // Damage entities in path
        AxisAlignedBB area = new AxisAlignedBB(oldX, oldY, oldZ, player.getX(), player.getY() + 2, player.getZ()).inflate(1);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player), damage);
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            Vector3d diff = player.position().subtract(new Vector3d(oldX, oldY, oldZ));
            for (double d = 0; d < diff.length(); d += 0.5) {
                double t = d / diff.length();
                sw.sendParticles(particle, oldX + diff.x * t, oldY + 1 + diff.y * t, oldZ + diff.z * t, 3, 0.1, 0.1, 0.1, 0.02);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.8F, 1.5F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performMultiHit(World world, PlayerEntity player, float damage, float range,
                                    net.minecraft.particles.IParticleData particle, String callout) {
        Vector3d look = player.getLookAngle();
        AxisAlignedBB area = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            // 3 rapid hits
            for (int i = 0; i < 3; i++) {
                target.hurt(DamageSource.playerAttack(player), damage / 3);
            }
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (int i = 0; i < 5; i++) {
                Vector3d pos = player.position().add(look.scale(i)).add(0, 1.5, 0);
                sw.sendParticles(particle, pos.x, pos.y, pos.z, 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.5F, 1.5F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performAOE(World world, PlayerEntity player, float damage, float range,
                               net.minecraft.particles.IParticleData particle, String callout) {
        AxisAlignedBB area = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player), damage);
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 60, 1));
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(particle, player.getX(), player.getY() + 1, player.getZ(), 100, range/2, 2, range/2, 0.1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0F, 1.5F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performUppercut(World world, PlayerEntity player, float damage,
                                    net.minecraft.particles.IParticleData particle, String callout) {
        AxisAlignedBB area = player.getBoundingBox().inflate(3, 5, 3);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player), damage);
            target.setDeltaMovement(0, 1.5, 0);
            target.hurtMarked = true;
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (int y = 0; y < 5; y++) {
                sw.sendParticles(particle, player.getX(), player.getY() + y, player.getZ(), 10, 0.5, 0.2, 0.5, 0.1);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1.5F, 0.8F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performDownSlash(World world, PlayerEntity player, float damage,
                                     net.minecraft.particles.IParticleData particle, String callout) {
        AxisAlignedBB area = player.getBoundingBox().inflate(2).move(0, -2, 0);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player), damage * 1.5F); // Bonus damage
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(particle, player.getX(), player.getY() + 3, player.getZ(), 50, 0.5, 2, 0.5, 0.2);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1.5F, 0.5F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performProjectile(World world, PlayerEntity player, float damage, float range,
                                      net.minecraft.particles.IParticleData particle, String callout) {
        Vector3d look = player.getLookAngle();
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (double d = 0; d < range; d += 0.5) {
                Vector3d pos = player.position().add(look.scale(d)).add(0, 1.5, 0);
                sw.sendParticles(particle, pos.x, pos.y, pos.z, 10, 0.3, 0.3, 0.3, 0.1);
                
                // Check for target
                AxisAlignedBB hitBox = new AxisAlignedBB(pos.x - 0.5, pos.y - 0.5, pos.z - 0.5,
                    pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
                List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player);
                for (LivingEntity target : targets) {
                    target.hurt(DamageSource.playerAttack(player), damage);
                    target.setSecondsOnFire(3);
                }
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.2F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performFlash(World world, PlayerEntity player, float damage, float range, String callout) {
        Vector3d look = player.getLookAngle();
        
        // Instant teleport behind target
        AxisAlignedBB searchArea = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, searchArea, e -> e != player);
        
        if (!targets.isEmpty()) {
            LivingEntity target = targets.get(0);
            Vector3d behindTarget = target.position().subtract(target.getLookAngle().scale(2));
            player.teleportTo(behindTarget.x, target.getY(), behindTarget.z);
            target.hurt(DamageSource.playerAttack(player), damage);
            
            // Speed buff
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 40, 3));
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1, player.getZ(), 30, 1, 1, 1, 1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.5F, 2.0F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.YELLOW).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performMist(World world, PlayerEntity player, float range, String callout) {
        // Apply invisibility and blindness to enemies
        player.addEffect(new EffectInstance(Effects.INVISIBILITY, 100, 0));
        
        AxisAlignedBB area = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        for (LivingEntity target : targets) {
            target.addEffect(new EffectInstance(Effects.BLINDNESS, 60, 0));
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 60, 1));
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1, player.getZ(), 200, range, 2, range, 0.01);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMBIENT_CAVE, SoundCategory.PLAYERS, 1.0F, 1.5F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.WHITE).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performWhip(World world, PlayerEntity player, float damage, float range,
                                net.minecraft.particles.IParticleData particle, String callout) {
        // Long range flexible attack
        Vector3d look = player.getLookAngle();
        for (double d = 0; d < range; d += 1) {
            Vector3d pos = player.position().add(look.scale(d)).add(0, 1.5, 0);
            AxisAlignedBB hitBox = new AxisAlignedBB(pos.x - 1, pos.y - 1, pos.z - 1,
                pos.x + 1, pos.y + 1, pos.z + 1);
            List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player);
            for (LivingEntity target : targets) {
                target.hurt(DamageSource.playerAttack(player), damage);
            }
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (double d = 0; d < range; d += 0.3) {
                double wave = Math.sin(d * 2) * 0.5;
                Vector3d pos = player.position().add(look.scale(d)).add(0, 1.5 + wave, 0);
                sw.sendParticles(particle, pos.x, pos.y, pos.z, 3, 0.1, 0.1, 0.1, 0.01);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.5F, 1.8F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.LIGHT_PURPLE).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performSunDance(World world, PlayerEntity player, float damage, float range, String callout) {
        AxisAlignedBB area = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player), damage);
            target.setSecondsOnFire(5);
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            for (int i = 0; i < 360; i += 10) {
                double angle = Math.toRadians(i);
                double x = player.getX() + Math.cos(angle) * range;
                double z = player.getZ() + Math.sin(angle) * range;
                sw.sendParticles(ParticleTypes.END_ROD, x, player.getY() + 1, z, 5, 0.1, 0.5, 0.1, 0.02);
            }
            sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1, player.getZ(), 50, 2, 1, 2, 0.1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLAZE_SHOOT, SoundCategory.PLAYERS, 1.5F, 0.5F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performDualSlash(World world, PlayerEntity player, float damage,
                                     net.minecraft.particles.IParticleData particle, String callout) {
        Vector3d look = player.getLookAngle();
        Vector3d right = look.cross(new Vector3d(0, 1, 0)).normalize();
        
        // Two slashes
        for (int side = -1; side <= 1; side += 2) {
            Vector3d dir = look.add(right.scale(side * 0.5)).normalize();
            AxisAlignedBB area = player.getBoundingBox().expandTowards(dir.scale(5)).inflate(1);
            List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
            for (LivingEntity target : targets) {
                target.hurt(DamageSource.playerAttack(player), damage / 2);
            }
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(particle, player.getX(), player.getY() + 1, player.getZ(), 50, 3, 1, 3, 0.2);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.5F, 1.0F);
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
    }
    
    protected void performUltimate(World world, PlayerEntity player, float damage, float range,
                                    net.minecraft.particles.IParticleData particle, String callout) {
        // Massive AOE
        AxisAlignedBB area = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity target : targets) {
            target.hurt(DamageSource.playerAttack(player), damage);
            target.setSecondsOnFire(10);
            Vector3d knockback = target.position().subtract(player.position()).normalize().scale(3);
            target.setDeltaMovement(knockback.x, 1, knockback.z);
            target.hurtMarked = true;
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1, player.getZ(), 5, 0, 0, 0, 0);
            sw.sendParticles(particle, player.getX(), player.getY() + 1, player.getZ(), 500, range, 5, range, 0.5);
            sw.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 5, player.getZ(), 3, 0, 0, 0, 0);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0F, 0.3F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 2.0F, 0.5F);
        
        player.displayClientMessage(new StringTextComponent("【" + name + "】 " + callout)
            .withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
    }
}
