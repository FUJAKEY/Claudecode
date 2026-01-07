package com.infinitygauntlet.items;

import com.infinitygauntlet.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InfinityGauntletItem extends Item {
    
    private static final String[] STONE_KEYS = {
        "space_stone", "time_stone", "reality_stone", 
        "power_stone", "mind_stone", "soul_stone"
    };
    
    private static final String[] STONE_NAMES = {
        "Space Stone", "Time Stone", "Reality Stone",
        "Power Stone", "Mind Stone", "Soul Stone"
    };
    
    private static final TextFormatting[] STONE_COLORS = {
        TextFormatting.BLUE, TextFormatting.GREEN, TextFormatting.RED,
        TextFormatting.DARK_PURPLE, TextFormatting.YELLOW, TextFormatting.GOLD
    };
    
    // Cooldowns in ticks for each stone
    private static final int[] STONE_COOLDOWNS = {
        40,  // Space - 2 seconds
        100, // Time - 5 seconds
        60,  // Reality - 3 seconds
        80,  // Power - 4 seconds
        120, // Mind - 6 seconds
        60   // Soul - 3 seconds
    };

    // Block transformations for Reality Stone
    private static final Map<Block, Block> REALITY_TRANSFORMS = new HashMap<Block, Block>() {{
        put(Blocks.STONE, Blocks.DIAMOND_ORE);
        put(Blocks.COBBLESTONE, Blocks.EMERALD_ORE);
        put(Blocks.DIRT, Blocks.GOLD_ORE);
        put(Blocks.SAND, Blocks.IRON_ORE);
        put(Blocks.GRAVEL, Blocks.LAPIS_ORE);
        put(Blocks.NETHERRACK, Blocks.ANCIENT_DEBRIS);
        put(Blocks.END_STONE, Blocks.DIAMOND_BLOCK);
        put(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);
        put(Blocks.WATER, Blocks.ICE);
        put(Blocks.LAVA, Blocks.MAGMA_BLOCK);
        put(Blocks.GRASS_BLOCK, Blocks.MYCELIUM);
        put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG);
        put(Blocks.COAL_ORE, Blocks.DIAMOND_ORE);
    }};

    public InfinityGauntletItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundNBT nbt = stack.getOrCreateTag();
        
        if (player.isShiftKeyDown()) {
            // Shift+RMB: Switch active stone
            int activeStone = nbt.getInt("active_stone");
            
            // Find next stone that is inserted
            int nextStone = activeStone;
            for (int i = 0; i < 6; i++) {
                nextStone = (nextStone + 1) % 6;
                if (nbt.getBoolean(STONE_KEYS[nextStone])) {
                    break;
                }
            }
            
            nbt.putInt("active_stone", nextStone);
            
            if (!world.isClientSide) {
                player.displayClientMessage(
                    new StringTextComponent("◆ " + STONE_NAMES[nextStone] + " ◆")
                        .withStyle(STONE_COLORS[nextStone]).withStyle(TextFormatting.BOLD), true);
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.5F, 1.5F);
            }
            return ActionResult.success(stack);
        }
        
        // Count stones
        int stoneCount = countStones(nbt);
        
        if (stoneCount == 0) {
            if (!world.isClientSide) {
                player.displayClientMessage(
                    new StringTextComponent("✗ No stones! Use stones on the gauntlet to insert them.")
                        .withStyle(TextFormatting.RED), true);
            }
            return ActionResult.fail(stack);
        }
        
        int activeStone = nbt.getInt("active_stone");
        
        // Check if active stone is inserted
        if (!nbt.getBoolean(STONE_KEYS[activeStone])) {
            // Find first available stone
            for (int i = 0; i < 6; i++) {
                if (nbt.getBoolean(STONE_KEYS[i])) {
                    activeStone = i;
                    nbt.putInt("active_stone", i);
                    break;
                }
            }
        }
        
        // Check cooldown
        String cooldownKey = "cooldown_" + STONE_KEYS[activeStone];
        long lastUse = nbt.getLong(cooldownKey);
        long currentTime = world.getGameTime();
        int cooldown = STONE_COOLDOWNS[activeStone];
        
        // Cooldown reduction based on stone count (more stones = faster cooldown)
        cooldown = cooldown - (stoneCount * 5);
        if (cooldown < 20) cooldown = 20;
        
        if (currentTime - lastUse < cooldown) {
            if (!world.isClientSide) {
                int remaining = (int)((cooldown - (currentTime - lastUse)) / 20) + 1;
                player.displayClientMessage(
                    new StringTextComponent("⏱ Cooldown: " + remaining + "s")
                        .withStyle(TextFormatting.GRAY), true);
            }
            return ActionResult.fail(stack);
        }

        if (!world.isClientSide) {
            // Check for SNAP (all 6 stones)
            if (stoneCount == 6) {
                long lastSnap = nbt.getLong("last_snap");
                if (currentTime - lastSnap >= 6000) { // 5 minute cooldown for snap
                    // Special key combo for snap: all stones + sneaking was already tried
                    // So we just use regular ability, but player can hold for snap
                    // Actually let's make snap require all stones + double right click
                    int clickCount = nbt.getInt("click_count");
                    long lastClick = nbt.getLong("last_click");
                    
                    if (currentTime - lastClick < 10) {
                        clickCount++;
                        if (clickCount >= 2) {
                            performSnap(world, player);
                            nbt.putLong("last_snap", currentTime);
                            nbt.putInt("click_count", 0);
                            return ActionResult.success(stack);
                        }
                    } else {
                        clickCount = 1;
                    }
                    nbt.putInt("click_count", clickCount);
                    nbt.putLong("last_click", currentTime);
                }
            }
            
            // Calculate power multiplier based on stone count
            float powerMultiplier = 1.0F + (stoneCount * 0.2F);
            
            // Use active stone ability
            useStoneAbility(world, player, activeStone, powerMultiplier);
            nbt.putLong(cooldownKey, currentTime);
            
            // XP cost
            if (player.totalExperience > 0 && !player.isCreative()) {
                player.giveExperiencePoints(-5 * stoneCount);
            }
        }
        
        player.getCooldowns().addCooldown(this, 10);
        return ActionResult.success(stack);
    }
    
    private int countStones(CompoundNBT nbt) {
        int count = 0;
        for (String key : STONE_KEYS) {
            if (nbt.getBoolean(key)) count++;
        }
        return count;
    }
    
    private void useStoneAbility(World world, PlayerEntity player, int stoneIndex, float powerMultiplier) {
        switch (stoneIndex) {
            case 0: useSpaceStone(world, player, powerMultiplier); break;
            case 1: useTimeStone(world, player, powerMultiplier); break;
            case 2: useRealityStone(world, player, powerMultiplier); break;
            case 3: usePowerStone(world, player, powerMultiplier); break;
            case 4: useMindStone(world, player, powerMultiplier); break;
            case 5: useSoulStone(world, player, powerMultiplier); break;
        }
    }
    
    private void useSpaceStone(World world, PlayerEntity player, float power) {
        double distance = 50 * power;
        Vector3d lookVec = player.getLookAngle();
        Vector3d eyePos = player.getEyePosition(1.0F);
        Vector3d targetPos = eyePos.add(lookVec.scale(distance));
        
        BlockRayTraceResult result = world.clip(new RayTraceContext(
            eyePos, targetPos, RayTraceContext.BlockMode.COLLIDER, 
            RayTraceContext.FluidMode.NONE, player));
        
        BlockPos teleportPos = result.getBlockPos().relative(result.getDirection());
        
        // Store old position for particles
        double oldX = player.getX();
        double oldY = player.getY();
        double oldZ = player.getZ();
        
        player.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5);
        
        // Sound effects
        world.playSound(null, oldX, oldY, oldZ,
            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.5F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.5F);
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            // Portal particles at old location
            sw.sendParticles(ParticleTypes.REVERSE_PORTAL,
                oldX, oldY + 1, oldZ, 100, 0.5, 1, 0.5, 0.1);
            // Portal particles at new location
            sw.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1, player.getZ(), 100, 0.5, 1, 0.5, 0.5);
            // Line of particles between locations
            Vector3d diff = new Vector3d(player.getX() - oldX, player.getY() - oldY, player.getZ() - oldZ);
            double len = diff.length();
            for (int i = 0; i < len; i += 2) {
                double t = i / len;
                sw.sendParticles(ParticleTypes.DRAGON_BREATH,
                    oldX + diff.x * t, oldY + 1 + diff.y * t, oldZ + diff.z * t,
                    3, 0.1, 0.1, 0.1, 0.01);
            }
        }
        
        player.displayClientMessage(
            new StringTextComponent("⬡ Teleported " + (int)result.distanceTo(player) + " blocks!")
                .withStyle(TextFormatting.BLUE), true);
    }
    
    private void useTimeStone(World world, PlayerEntity player, float power) {
        int radius = (int)(15 * power);
        int duration = (int)(200 * power);
        
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player);
        
        int affected = 0;
        for (LivingEntity entity : entities) {
            // Freeze effect: extreme slowness + no jump + weakness
            entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, duration, 10));
            entity.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, duration, 5));
            entity.addEffect(new EffectInstance(Effects.WEAKNESS, duration, 4));
            entity.addEffect(new EffectInstance(Effects.JUMP, duration, 128)); // Can't jump
            
            // Stop their movement
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD,
                    entity.getX(), entity.getY() + 1, entity.getZ(), 20, 0.3, 0.5, 0.3, 0.02);
            }
            affected++;
        }
        
        // Player gets speed boost (time moves faster for you)
        player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, duration / 2, 2));
        player.addEffect(new EffectInstance(Effects.DIG_SPEED, duration / 2, 2));
        
        if (world instanceof ServerWorld) {
            // Time vortex effect
            for (int i = 0; i < 360; i += 15) {
                double angle = Math.toRadians(i);
                double x = player.getX() + Math.cos(angle) * 3;
                double z = player.getZ() + Math.sin(angle) * 3;
                ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD,
                    x, player.getY() + 1, z, 5, 0.1, 0.3, 0.1, 0.01);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 2.0F);
        
        player.displayClientMessage(
            new StringTextComponent("⏱ Time frozen! " + affected + " entities affected for " + (duration/20) + "s")
                .withStyle(TextFormatting.GREEN), true);
    }
    
    private void useRealityStone(World world, PlayerEntity player, float power) {
        int radius = (int)(5 * power);
        BlockPos playerPos = player.blockPosition();
        int transformed = 0;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    Block currentBlock = world.getBlockState(pos).getBlock();
                    
                    if (REALITY_TRANSFORMS.containsKey(currentBlock)) {
                        Block newBlock = REALITY_TRANSFORMS.get(currentBlock);
                        world.setBlock(pos, newBlock.defaultBlockState(), 3);
                        
                        if (world instanceof ServerWorld && transformed < 50) {
                            ((ServerWorld) world).sendParticles(ParticleTypes.CRIMSON_SPORE,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
                                5, 0.3, 0.3, 0.3, 0.05);
                        }
                        transformed++;
                    }
                }
            }
        }
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.CRIMSON_SPORE,
                player.getX(), player.getY() + 1, player.getZ(), 200, radius, 3, radius, 0.1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 0.5F);
        
        player.displayClientMessage(
            new StringTextComponent("✦ Reality warped! " + transformed + " blocks transformed")
                .withStyle(TextFormatting.RED), true);
    }
    
    private void usePowerStone(World world, PlayerEntity player, float power) {
        float damage = 20.0F * power;
        int radius = (int)(10 * power);
        
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player);
        
        int hit = 0;
        for (LivingEntity entity : entities) {
            // Knockback away from player
            Vector3d knockback = entity.position().subtract(player.position()).normalize().scale(3 * power);
            entity.setDeltaMovement(knockback.x, 0.7, knockback.z);
            entity.hurtMarked = true;
            
            // Damage
            entity.hurt(DamageSource.playerAttack(player).setMagic(), damage);
            
            // Fire for extra damage
            entity.setSecondsOnFire((int)(5 * power));
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.EXPLOSION,
                    entity.getX(), entity.getY() + 1, entity.getZ(), 3, 0.3, 0.3, 0.3, 0);
            }
            hit++;
        }
        
        // Destroy weak blocks nearby
        BlockPos playerPos = player.blockPosition();
        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (world.getBlockState(pos).getDestroySpeed(world, pos) < 1.0F &&
                        world.getBlockState(pos).getDestroySpeed(world, pos) > 0) {
                        world.destroyBlock(pos, true);
                    }
                }
            }
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                player.getX(), player.getY() + 1, player.getZ(), 3, 0, 0, 0, 0);
            sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1, player.getZ(), 100, radius/2, 2, radius/2, 0.2);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0F, 0.5F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        
        player.displayClientMessage(
            new StringTextComponent("☠ Power unleashed! " + hit + " entities hit for " + (int)damage + " damage!")
                .withStyle(TextFormatting.DARK_PURPLE), true);
    }
    
    private void useMindStone(World world, PlayerEntity player, float power) {
        int radius = (int)(20 * power);
        int duration = (int)(400 * power);
        
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        List<MobEntity> entities = world.getEntitiesOfClass(MobEntity.class, area);
        
        int controlled = 0;
        for (MobEntity mob : entities) {
            // Clear target
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            
            // Apply effects
            mob.addEffect(new EffectInstance(Effects.GLOWING, duration, 0));
            mob.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, duration, 2));
            
            // Make peaceful towards player
            mob.setNoAi(false);
            
            // Navigate to player
            mob.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), 1.2);
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANT,
                    mob.getX(), mob.getY() + mob.getBbHeight() + 0.5, mob.getZ(), 
                    15, 0.3, 0.3, 0.3, 0.5);
            }
            controlled++;
        }
        
        // Give player enhanced awareness
        player.addEffect(new EffectInstance(Effects.NIGHT_VISION, duration, 0));
        
        if (world instanceof ServerWorld) {
            // Mind wave effect
            for (int ring = 1; ring <= 3; ring++) {
                for (int i = 0; i < 360; i += 10) {
                    double angle = Math.toRadians(i);
                    double x = player.getX() + Math.cos(angle) * ring * 3;
                    double z = player.getZ() + Math.sin(angle) * ring * 3;
                    ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANT,
                        x, player.getY() + 1, z, 3, 0.1, 0.2, 0.1, 0.3);
                }
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0F, 1.5F);
        
        player.displayClientMessage(
            new StringTextComponent("◉ " + controlled + " minds controlled! They will follow you for " + (duration/20) + "s")
                .withStyle(TextFormatting.YELLOW), true);
    }
    
    private void useSoulStone(World world, PlayerEntity player, float power) {
        int duration = (int)(400 * power);
        int healAmount = (int)(10 * power);
        
        // Heal player significantly
        player.heal(healAmount);
        
        // Powerful buffs
        player.addEffect(new EffectInstance(Effects.REGENERATION, duration, 3));
        player.addEffect(new EffectInstance(Effects.ABSORPTION, duration, 4));
        player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, duration, 2));
        player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, duration / 2, 1));
        player.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, duration, 0));
        
        // Life steal from nearby enemies
        AxisAlignedBB area = player.getBoundingBox().inflate(8 * power);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && !(e instanceof PlayerEntity));
        
        int drained = 0;
        for (LivingEntity entity : entities) {
            float drain = Math.min(entity.getHealth(), 4.0F * power);
            entity.hurt(DamageSource.MAGIC, drain);
            player.heal(drain / 2);
            
            if (world instanceof ServerWorld) {
                // Soul drain effect - particles from entity to player
                Vector3d diff = player.position().subtract(entity.position());
                for (int i = 0; i < 5; i++) {
                    double t = i / 5.0;
                    ((ServerWorld) world).sendParticles(ParticleTypes.SOUL,
                        entity.getX() + diff.x * t, 
                        entity.getY() + 1 + diff.y * t, 
                        entity.getZ() + diff.z * t,
                        2, 0.1, 0.1, 0.1, 0.02);
                }
            }
            drained++;
        }
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1, player.getZ(), 50, 1, 1, 1, 0.1);
            ((ServerWorld) world).sendParticles(ParticleTypes.SOUL,
                player.getX(), player.getY() + 1.5, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SOUL_ESCAPE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TOTEM_USE, SoundCategory.PLAYERS, 0.5F, 1.5F);
        
        player.displayClientMessage(
            new StringTextComponent("❤ Healed " + healAmount + " HP! Drained " + drained + " souls!")
                .withStyle(TextFormatting.GOLD), true);
    }
    
    private void performSnap(World world, PlayerEntity player) {
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            
            // Get all living entities in huge radius
            List<LivingEntity> allEntities = sw.getEntitiesOfClass(
                LivingEntity.class, 
                player.getBoundingBox().inflate(150),
                e -> e != player && !(e instanceof PlayerEntity)
            );
            
            // Kill 50% randomly with dramatic effect
            Random random = new Random();
            int killed = 0;
            
            for (LivingEntity entity : allEntities) {
                if (random.nextBoolean()) {
                    // Dust effect before removal
                    for (int i = 0; i < 20; i++) {
                        sw.sendParticles(ParticleTypes.ASH,
                            entity.getX() + random.nextDouble() - 0.5,
                            entity.getY() + random.nextDouble() * entity.getBbHeight(),
                            entity.getZ() + random.nextDouble() - 0.5,
                            5, 0.2, 0.2, 0.2, 0.05);
                    }
                    sw.sendParticles(ParticleTypes.SMOKE,
                        entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 
                        30, 0.5, 0.5, 0.5, 0.1);
                    
                    entity.remove();
                    killed++;
                }
            }
            
            // EPIC visual effects
            // Flash
            sw.sendParticles(ParticleTypes.FLASH,
                player.getX(), player.getY() + 10, player.getZ(), 10, 5, 5, 5, 0);
            
            // Multiple expanding rings
            for (int ring = 0; ring < 50; ring += 5) {
                for (int i = 0; i < 360; i += 5) {
                    double angle = Math.toRadians(i);
                    double x = player.getX() + Math.cos(angle) * ring;
                    double z = player.getZ() + Math.sin(angle) * ring;
                    sw.sendParticles(ParticleTypes.END_ROD, x, player.getY() + 1, z, 1, 0, 0, 0, 0);
                }
            }
            
            // Soul particles
            sw.sendParticles(ParticleTypes.SOUL,
                player.getX(), player.getY() + 2, player.getZ(), 500, 30, 10, 30, 0.5);
            
            // Dragon breath
            sw.sendParticles(ParticleTypes.DRAGON_BREATH,
                player.getX(), player.getY() + 1, player.getZ(), 200, 20, 5, 20, 0.2);
            
            // Sounds
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.END_PORTAL_SPAWN, SoundCategory.PLAYERS, 3.0F, 0.3F);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_DEATH, SoundCategory.PLAYERS, 2.0F, 0.5F);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 3.0F, 0.5F);
            
            // Message
            player.displayClientMessage(
                new StringTextComponent("")
                    .append(new StringTextComponent("★ ").withStyle(TextFormatting.LIGHT_PURPLE))
                    .append(new StringTextComponent("S N A P").withStyle(TextFormatting.WHITE).withStyle(TextFormatting.BOLD))
                    .append(new StringTextComponent(" ★").withStyle(TextFormatting.LIGHT_PURPLE))
                    .append(new StringTextComponent(" " + killed + " entities erased!").withStyle(TextFormatting.GRAY)),
                true);
            
            // Damage player for using snap (balanced)
            player.hurt(DamageSource.MAGIC, 20.0F);
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, 2));
            player.addEffect(new EffectInstance(Effects.WEAKNESS, 200, 1));
        }
    }
    
    // Passive effects when holding gauntlet
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (entity instanceof PlayerEntity && isSelected) {
            PlayerEntity player = (PlayerEntity) entity;
            CompoundNBT nbt = stack.getOrCreateTag();
            int stoneCount = countStones(nbt);
            
            if (stoneCount > 0 && world.getGameTime() % 20 == 0) {
                // Passive effects based on stones
                if (nbt.getBoolean("space_stone")) {
                    player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 25, 0, false, false));
                }
                if (nbt.getBoolean("power_stone")) {
                    player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 25, 0, false, false));
                }
                if (nbt.getBoolean("soul_stone")) {
                    player.addEffect(new EffectInstance(Effects.REGENERATION, 25, 0, false, false));
                }
                if (nbt.getBoolean("reality_stone") && !world.isClientSide) {
                    player.addEffect(new EffectInstance(Effects.LUCK, 25, 0, false, false));
                }
                if (nbt.getBoolean("mind_stone")) {
                    player.addEffect(new EffectInstance(Effects.HERO_OF_THE_VILLAGE, 25, 0, false, false));
                }
                if (nbt.getBoolean("time_stone")) {
                    player.addEffect(new EffectInstance(Effects.DIG_SPEED, 25, 0, false, false));
                }
            }
            
            // Particles when holding
            if (stoneCount > 0 && world instanceof ServerWorld && world.getGameTime() % 5 == 0) {
                ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 0.8, player.getZ(), 
                    stoneCount, 0.3, 0.2, 0.3, 0.01);
            }
        }
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        CompoundNBT nbt = stack.getOrCreateTag();
        int stoneCount = countStones(nbt);
        
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("◆ Infinity Stones:").withStyle(TextFormatting.GRAY));
        
        for (int i = 0; i < STONE_KEYS.length; i++) {
            boolean hasStone = nbt.getBoolean(STONE_KEYS[i]);
            String status = hasStone ? "✓ " : "✗ ";
            TextFormatting color = hasStone ? STONE_COLORS[i] : TextFormatting.DARK_GRAY;
            tooltip.add(new StringTextComponent("  " + status + STONE_NAMES[i]).withStyle(color));
        }
        
        tooltip.add(new StringTextComponent(""));
        
        if (stoneCount == 6) {
            tooltip.add(new StringTextComponent("★ ALL STONES COLLECTED ★")
                .withStyle(TextFormatting.LIGHT_PURPLE).withStyle(TextFormatting.BOLD));
            tooltip.add(new StringTextComponent("Double right-click to SNAP!")
                .withStyle(TextFormatting.RED).withStyle(TextFormatting.ITALIC));
        } else if (stoneCount > 0) {
            tooltip.add(new StringTextComponent("Power: " + (100 + stoneCount * 20) + "%")
                .withStyle(TextFormatting.AQUA));
        }
        
        int activeStone = nbt.getInt("active_stone");
        if (stoneCount > 0 && nbt.getBoolean(STONE_KEYS[activeStone])) {
            tooltip.add(new StringTextComponent(""));
            tooltip.add(new StringTextComponent("▶ Active: " + STONE_NAMES[activeStone])
                .withStyle(STONE_COLORS[activeStone]).withStyle(TextFormatting.BOLD));
        }
        
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Shift+RMB: Switch stone").withStyle(TextFormatting.DARK_GRAY));
        tooltip.add(new StringTextComponent("RMB: Use ability").withStyle(TextFormatting.DARK_GRAY));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return countStones(stack.getOrCreateTag()) > 0;
    }
    
    public static boolean insertStone(ItemStack gauntlet, String stoneKey) {
        CompoundNBT nbt = gauntlet.getOrCreateTag();
        if (!nbt.getBoolean(stoneKey)) {
            nbt.putBoolean(stoneKey, true);
            return true;
        }
        return false;
    }
    
    public static boolean hasStone(ItemStack gauntlet, String stoneKey) {
        return gauntlet.getOrCreateTag().getBoolean(stoneKey);
    }
}
