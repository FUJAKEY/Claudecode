package com.infinitygauntlet.items;

import com.infinitygauntlet.events.InfinityHandler;
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
    
    // Sub-abilities for each stone
    private static final String[][] STONE_ABILITIES = {
        {"Teleport", "Warp Zone"},           // Space Stone
        {"Time Freeze", "∞ INFINITY ∞"},     // Time Stone - GOJO SATORU
        {"Transform", "Illusion"},            // Reality Stone
        {"Power Wave", "Destruction"},        // Power Stone
        {"Mind Control", "Telepathy"},        // Mind Stone
        {"Life Steal", "Resurrection"}        // Soul Stone
    };
    
    // Cooldowns in ticks for each stone
    private static final int[] STONE_COOLDOWNS = {
        40, 100, 60, 80, 120, 60
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
        
        // Ctrl (Sneak) + RMB: Switch sub-ability for current stone
        if (player.isShiftKeyDown() && player.isCrouching()) {
            int activeStone = nbt.getInt("active_stone");
            if (nbt.getBoolean(STONE_KEYS[activeStone])) {
                int subAbility = nbt.getInt("sub_ability_" + activeStone);
                subAbility = (subAbility + 1) % 2; // Toggle between 0 and 1
                nbt.putInt("sub_ability_" + activeStone, subAbility);
                
                // Special handling for Infinity toggle
                if (activeStone == 1 && subAbility == 1) {
                    // Activating Infinity
                    nbt.putBoolean("infinity_active", true);
                    if (!world.isClientSide) {
                        player.displayClientMessage(
                            new StringTextComponent("∞ INFINITY ACTIVATED ∞")
                                .withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD), true);
                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0F, 0.5F);
                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.END_PORTAL_SPAWN, SoundCategory.PLAYERS, 0.5F, 2.0F);
                    }
                } else if (activeStone == 1) {
                    // Deactivating Infinity
                    nbt.putBoolean("infinity_active", false);
                    if (!world.isClientSide) {
                        player.displayClientMessage(
                            new StringTextComponent("Infinity deactivated")
                                .withStyle(TextFormatting.GRAY), true);
                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    }
                }
                
                if (!world.isClientSide && activeStone != 1) {
                    player.displayClientMessage(
                        new StringTextComponent("◇ " + STONE_NAMES[activeStone] + " → " + STONE_ABILITIES[activeStone][subAbility])
                            .withStyle(STONE_COLORS[activeStone]), true);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5F, 1.5F);
                }
                return ActionResult.success(stack);
            }
        }
        
        if (player.isShiftKeyDown()) {
            // Shift+RMB: Switch active stone
            int activeStone = nbt.getInt("active_stone");
            int nextStone = activeStone;
            for (int i = 0; i < 6; i++) {
                nextStone = (nextStone + 1) % 6;
                if (nbt.getBoolean(STONE_KEYS[nextStone])) {
                    break;
                }
            }
            
            nbt.putInt("active_stone", nextStone);
            
            if (!world.isClientSide) {
                int subAbility = nbt.getInt("sub_ability_" + nextStone);
                player.displayClientMessage(
                    new StringTextComponent("◆ " + STONE_NAMES[nextStone] + " [" + STONE_ABILITIES[nextStone][subAbility] + "]")
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
            for (int i = 0; i < 6; i++) {
                if (nbt.getBoolean(STONE_KEYS[i])) {
                    activeStone = i;
                    nbt.putInt("active_stone", i);
                    break;
                }
            }
        }
        
        // If Infinity is active (Time Stone sub-ability), toggle it off on use
        if (activeStone == 1 && nbt.getBoolean("infinity_active")) {
            // Infinity is passive - just show message
            if (!world.isClientSide) {
                player.displayClientMessage(
                    new StringTextComponent("∞ Infinity is ACTIVE - Nothing can touch you!")
                        .withStyle(TextFormatting.AQUA), true);
            }
            return ActionResult.success(stack);
        }
        
        // Check cooldown
        String cooldownKey = "cooldown_" + STONE_KEYS[activeStone];
        long lastUse = nbt.getLong(cooldownKey);
        long currentTime = world.getGameTime();
        int cooldown = STONE_COOLDOWNS[activeStone];
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
            // Check for SNAP (all 6 stones + double click)
            if (stoneCount == 6) {
                long lastSnap = nbt.getLong("last_snap");
                if (currentTime - lastSnap >= 6000) {
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
            
            float powerMultiplier = 1.0F + (stoneCount * 0.2F);
            int subAbility = nbt.getInt("sub_ability_" + activeStone);
            
            // Use active stone ability with sub-ability
            useStoneAbility(world, player, activeStone, subAbility, powerMultiplier);
            nbt.putLong(cooldownKey, currentTime);
            
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
    
    private void useStoneAbility(World world, PlayerEntity player, int stoneIndex, int subAbility, float powerMultiplier) {
        switch (stoneIndex) {
            case 0: 
                if (subAbility == 0) useSpaceStone(world, player, powerMultiplier);
                else useSpaceStoneWarp(world, player, powerMultiplier);
                break;
            case 1: 
                if (subAbility == 0) useTimeStone(world, player, powerMultiplier);
                // Sub-ability 1 is Infinity - handled passively
                break;
            case 2: 
                if (subAbility == 0) useRealityStone(world, player, powerMultiplier);
                else useRealityStoneIllusion(world, player, powerMultiplier);
                break;
            case 3: 
                if (subAbility == 0) usePowerStone(world, player, powerMultiplier);
                else usePowerStoneDestruction(world, player, powerMultiplier);
                break;
            case 4: 
                if (subAbility == 0) useMindStone(world, player, powerMultiplier);
                else useMindStoneTelepath(world, player, powerMultiplier);
                break;
            case 5: 
                if (subAbility == 0) useSoulStone(world, player, powerMultiplier);
                else useSoulStoneResurrect(world, player, powerMultiplier);
                break;
        }
    }
    
    // ==================== SPACE STONE ====================
    
    private void useSpaceStone(World world, PlayerEntity player, float power) {
        double distance = 50 * power;
        Vector3d lookVec = player.getLookAngle();
        Vector3d eyePos = player.getEyePosition(1.0F);
        Vector3d targetPos = eyePos.add(lookVec.scale(distance));
        
        BlockRayTraceResult result = world.clip(new RayTraceContext(
            eyePos, targetPos, RayTraceContext.BlockMode.COLLIDER, 
            RayTraceContext.FluidMode.NONE, player));
        
        BlockPos teleportPos = result.getBlockPos().relative(result.getDirection());
        double oldX = player.getX(), oldY = player.getY(), oldZ = player.getZ();
        
        player.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5);
        
        world.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.5F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.5F);
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(ParticleTypes.REVERSE_PORTAL, oldX, oldY + 1, oldZ, 100, 0.5, 1, 0.5, 0.1);
            sw.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 100, 0.5, 1, 0.5, 0.5);
        }
        
        player.displayClientMessage(new StringTextComponent("⬡ Teleported!").withStyle(TextFormatting.BLUE), true);
    }
    
    private void useSpaceStoneWarp(World world, PlayerEntity player, float power) {
        // Warp Zone - teleport all nearby entities to random locations
        AxisAlignedBB area = player.getBoundingBox().inflate(10 * power);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        Random rand = new Random();
        for (LivingEntity entity : entities) {
            double newX = entity.getX() + (rand.nextDouble() - 0.5) * 20;
            double newZ = entity.getZ() + (rand.nextDouble() - 0.5) * 20;
            entity.teleportTo(newX, entity.getY(), newZ);
            
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1, entity.getZ(), 30, 0.5, 1, 0.5, 0.3);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.5F, 0.3F);
        player.displayClientMessage(new StringTextComponent("⬡ Warped " + entities.size() + " entities!").withStyle(TextFormatting.BLUE), true);
    }
    
    // ==================== TIME STONE ====================
    
    private void useTimeStone(World world, PlayerEntity player, float power) {
        int radius = (int)(15 * power);
        int duration = (int)(200 * power);
        
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        int affected = 0;
        for (LivingEntity entity : entities) {
            entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, duration, 10));
            entity.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, duration, 5));
            entity.addEffect(new EffectInstance(Effects.WEAKNESS, duration, 4));
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
            affected++;
        }
        
        player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, duration / 2, 2));
        
        if (world instanceof ServerWorld) {
            for (int i = 0; i < 360; i += 15) {
                double angle = Math.toRadians(i);
                double x = player.getX() + Math.cos(angle) * 3;
                double z = player.getZ() + Math.sin(angle) * 3;
                ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD, x, player.getY() + 1, z, 5, 0.1, 0.3, 0.1, 0.01);
            }
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 2.0F);
        player.displayClientMessage(new StringTextComponent("⏱ Time frozen! " + affected + " affected").withStyle(TextFormatting.GREEN), true);
    }
    
    // ==================== REALITY STONE ====================
    
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
                        transformed++;
                    }
                }
            }
        }
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1, player.getZ(), 200, radius, 3, radius, 0.1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 0.5F);
        player.displayClientMessage(new StringTextComponent("✦ " + transformed + " blocks transformed").withStyle(TextFormatting.RED), true);
    }
    
    private void useRealityStoneIllusion(World world, PlayerEntity player, float power) {
        // Make player invisible and create confusion
        player.addEffect(new EffectInstance(Effects.INVISIBILITY, (int)(200 * power), 0));
        
        AxisAlignedBB area = player.getBoundingBox().inflate(15);
        List<MobEntity> mobs = world.getEntitiesOfClass(MobEntity.class, area);
        
        for (MobEntity mob : mobs) {
            mob.setTarget(null);
            mob.addEffect(new EffectInstance(Effects.BLINDNESS, (int)(100 * power), 0));
        }
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(), 100, 1, 1, 1, 0.1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(new StringTextComponent("✦ Illusion active! You are invisible").withStyle(TextFormatting.RED), true);
    }
    
    // ==================== POWER STONE ====================
    
    private void usePowerStone(World world, PlayerEntity player, float power) {
        float damage = 20.0F * power;
        int radius = (int)(10 * power);
        
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        int hit = 0;
        for (LivingEntity entity : entities) {
            Vector3d knockback = entity.position().subtract(player.position()).normalize().scale(3 * power);
            entity.setDeltaMovement(knockback.x, 0.7, knockback.z);
            entity.hurtMarked = true;
            entity.hurt(DamageSource.playerAttack(player).setMagic(), damage);
            entity.setSecondsOnFire((int)(5 * power));
            hit++;
        }
        
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            sw.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1, player.getZ(), 3, 0, 0, 0, 0);
            sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1, player.getZ(), 100, radius/2, 2, radius/2, 0.2);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0F, 0.5F);
        player.displayClientMessage(new StringTextComponent("☠ " + hit + " hit for " + (int)damage + " damage!").withStyle(TextFormatting.DARK_PURPLE), true);
    }
    
    private void usePowerStoneDestruction(World world, PlayerEntity player, float power) {
        // Destroy ALL blocks in radius (except bedrock)
        int radius = (int)(5 * power);
        BlockPos playerPos = player.blockPosition();
        int destroyed = 0;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (world.getBlockState(pos).getDestroySpeed(world, pos) >= 0 && 
                        world.getBlockState(pos).getBlock() != Blocks.BEDROCK) {
                        world.destroyBlock(pos, true);
                        destroyed++;
                    }
                }
            }
        }
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 10, radius, radius, radius, 0);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 2.0F, 0.5F);
        player.displayClientMessage(new StringTextComponent("☠ DESTRUCTION! " + destroyed + " blocks obliterated").withStyle(TextFormatting.DARK_PURPLE), true);
    }
    
    // ==================== MIND STONE ====================
    
    private void useMindStone(World world, PlayerEntity player, float power) {
        int radius = (int)(20 * power);
        int duration = (int)(400 * power);
        
        AxisAlignedBB area = player.getBoundingBox().inflate(radius);
        List<MobEntity> entities = world.getEntitiesOfClass(MobEntity.class, area);
        
        int controlled = 0;
        for (MobEntity mob : entities) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            mob.addEffect(new EffectInstance(Effects.GLOWING, duration, 0));
            mob.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), 1.2);
            controlled++;
        }
        
        player.addEffect(new EffectInstance(Effects.NIGHT_VISION, duration, 0));
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 2, player.getZ(), 100, 5, 2, 5, 0.5);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0F, 1.5F);
        player.displayClientMessage(new StringTextComponent("◉ " + controlled + " minds controlled!").withStyle(TextFormatting.YELLOW), true);
    }
    
    private void useMindStoneTelepath(World world, PlayerEntity player, float power) {
        // See through walls + reveal all entities
        player.addEffect(new EffectInstance(Effects.NIGHT_VISION, (int)(600 * power), 0));
        
        AxisAlignedBB area = player.getBoundingBox().inflate(50);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
        
        for (LivingEntity entity : entities) {
            entity.addEffect(new EffectInstance(Effects.GLOWING, (int)(300 * power), 0));
        }
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 2, player.getZ(), 200, 25, 10, 25, 0.5);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_AMBIENT, SoundCategory.PLAYERS, 1.0F, 2.0F);
        player.displayClientMessage(new StringTextComponent("◉ Telepathy active! " + entities.size() + " entities revealed").withStyle(TextFormatting.YELLOW), true);
    }
    
    // ==================== SOUL STONE ====================
    
    private void useSoulStone(World world, PlayerEntity player, float power) {
        int duration = (int)(400 * power);
        int healAmount = (int)(10 * power);
        
        player.heal(healAmount);
        player.addEffect(new EffectInstance(Effects.REGENERATION, duration, 3));
        player.addEffect(new EffectInstance(Effects.ABSORPTION, duration, 4));
        player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, duration, 2));
        player.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, duration, 0));
        
        AxisAlignedBB area = player.getBoundingBox().inflate(8 * power);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && !(e instanceof PlayerEntity));
        
        int drained = 0;
        for (LivingEntity entity : entities) {
            float drain = Math.min(entity.getHealth(), 4.0F * power);
            entity.hurt(DamageSource.MAGIC, drain);
            player.heal(drain / 2);
            drained++;
        }
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1, player.getZ(), 50, 1, 1, 1, 0.1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundCategory.PLAYERS, 0.5F, 1.5F);
        player.displayClientMessage(new StringTextComponent("❤ Healed " + healAmount + "! Drained " + drained + " souls").withStyle(TextFormatting.GOLD), true);
    }
    
    private void useSoulStoneResurrect(World world, PlayerEntity player, float power) {
        // Full heal + remove all negative effects + temporary god mode
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        
        // Remove negative effects
        player.removeEffect(Effects.POISON);
        player.removeEffect(Effects.WITHER);
        player.removeEffect(Effects.HUNGER);
        player.removeEffect(Effects.WEAKNESS);
        player.removeEffect(Effects.MOVEMENT_SLOWDOWN);
        player.removeEffect(Effects.BLINDNESS);
        
        // Powerful buffs
        int duration = (int)(200 * power);
        player.addEffect(new EffectInstance(Effects.REGENERATION, duration, 4));
        player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, duration, 3));
        player.addEffect(new EffectInstance(Effects.ABSORPTION, duration, 5));
        player.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, duration, 0));
        
        if (world instanceof ServerWorld) {
            ((ServerWorld) world).sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 100, 1, 2, 1, 0.5);
            ((ServerWorld) world).sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1, player.getZ(), 50, 1, 1, 1, 0.1);
        }
        
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(new StringTextComponent("❤ RESURRECTION! Full restoration").withStyle(TextFormatting.GOLD).withStyle(TextFormatting.BOLD), true);
    }
    
    // ==================== SNAP ====================
    
    private void performSnap(World world, PlayerEntity player) {
        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;
            
            List<LivingEntity> allEntities = sw.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(150), e -> e != player && !(e instanceof PlayerEntity));
            
            Random random = new Random();
            int killed = 0;
            
            for (LivingEntity entity : allEntities) {
                if (random.nextBoolean()) {
                    for (int i = 0; i < 20; i++) {
                        sw.sendParticles(ParticleTypes.ASH,
                            entity.getX() + random.nextDouble() - 0.5,
                            entity.getY() + random.nextDouble() * entity.getBbHeight(),
                            entity.getZ() + random.nextDouble() - 0.5,
                            5, 0.2, 0.2, 0.2, 0.05);
                    }
                    entity.remove();
                    killed++;
                }
            }
            
            sw.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 10, player.getZ(), 10, 5, 5, 5, 0);
            sw.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 2, player.getZ(), 500, 30, 10, 30, 0.5);
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.END_PORTAL_SPAWN, SoundCategory.PLAYERS, 3.0F, 0.3F);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 3.0F, 0.5F);
            
            player.displayClientMessage(new StringTextComponent("★ SNAP ★ " + killed + " erased!")
                .withStyle(TextFormatting.LIGHT_PURPLE).withStyle(TextFormatting.BOLD), true);
            
            player.hurt(DamageSource.MAGIC, 20.0F);
        }
    }
    
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (entity instanceof PlayerEntity && isSelected) {
            PlayerEntity player = (PlayerEntity) entity;
            CompoundNBT nbt = stack.getOrCreateTag();
            int stoneCount = countStones(nbt);
            
            // Apply passive effects
            if (stoneCount > 0 && world.getGameTime() % 20 == 0) {
                if (nbt.getBoolean("space_stone")) {
                    player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 25, 0, false, false));
                }
                if (nbt.getBoolean("power_stone")) {
                    player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 25, 0, false, false));
                }
                if (nbt.getBoolean("soul_stone")) {
                    player.addEffect(new EffectInstance(Effects.REGENERATION, 25, 0, false, false));
                }
                if (nbt.getBoolean("reality_stone")) {
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
                    player.getX(), player.getY() + 0.8, player.getZ(), stoneCount, 0.3, 0.2, 0.3, 0.01);
            }
            
            // Infinity visual effect
            if (nbt.getBoolean("infinity_active") && world instanceof ServerWorld && world.getGameTime() % 3 == 0) {
                ((ServerWorld) world).sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1, player.getZ(), 10, 2, 1, 2, 0.01);
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
            
            if (hasStone) {
                int subAbility = nbt.getInt("sub_ability_" + i);
                tooltip.add(new StringTextComponent("  " + status + STONE_NAMES[i] + " [" + STONE_ABILITIES[i][subAbility] + "]").withStyle(color));
            } else {
                tooltip.add(new StringTextComponent("  " + status + STONE_NAMES[i]).withStyle(color));
            }
        }
        
        if (nbt.getBoolean("infinity_active")) {
            tooltip.add(new StringTextComponent(""));
            tooltip.add(new StringTextComponent("∞ INFINITY ACTIVE ∞").withStyle(TextFormatting.AQUA).withStyle(TextFormatting.BOLD));
            tooltip.add(new StringTextComponent("Nothing can touch you!").withStyle(TextFormatting.GRAY));
        }
        
        tooltip.add(new StringTextComponent(""));
        
        if (stoneCount == 6) {
            tooltip.add(new StringTextComponent("★ ALL STONES COLLECTED ★")
                .withStyle(TextFormatting.LIGHT_PURPLE).withStyle(TextFormatting.BOLD));
            tooltip.add(new StringTextComponent("Double RMB to SNAP!")
                .withStyle(TextFormatting.RED).withStyle(TextFormatting.ITALIC));
        } else if (stoneCount > 0) {
            tooltip.add(new StringTextComponent("Power: " + (100 + stoneCount * 20) + "%").withStyle(TextFormatting.AQUA));
        }
        
        int activeStone = nbt.getInt("active_stone");
        if (stoneCount > 0 && nbt.getBoolean(STONE_KEYS[activeStone])) {
            int subAbility = nbt.getInt("sub_ability_" + activeStone);
            tooltip.add(new StringTextComponent(""));
            tooltip.add(new StringTextComponent("▶ " + STONE_NAMES[activeStone] + " → " + STONE_ABILITIES[activeStone][subAbility])
                .withStyle(STONE_COLORS[activeStone]).withStyle(TextFormatting.BOLD));
        }
        
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Shift+RMB: Switch stone").withStyle(TextFormatting.DARK_GRAY));
        tooltip.add(new StringTextComponent("Crouch+Shift+RMB: Switch ability").withStyle(TextFormatting.DARK_GRAY));
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
