package com.infinitygauntlet.events;

import com.infinitygauntlet.InfinityGauntletMod;
import com.infinitygauntlet.entity.ThanosEntity;
import com.infinitygauntlet.init.ModEntities;
import com.infinitygauntlet.init.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Handles Thanos spawn logic - he hunts players who collect all infinity stones
 */
@Mod.EventBusSubscriber(modid = InfinityGauntletMod.MOD_ID)
public class ThanosSpawnHandler {
    
    private static final Map<UUID, Long> LAST_SPAWN_ATTEMPT = new HashMap<>();
    private static final int CHECK_INTERVAL = 1200; // Every minute
    private static final int MIN_SPAWN_DISTANCE = 10;
    private static final int MAX_SPAWN_DISTANCE = 30;
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level.isClientSide) return;
        if (!(event.player.level instanceof ServerWorld)) return;
        
        PlayerEntity player = event.player;
        ServerWorld world = (ServerWorld) player.level;
        
        // Only check every minute
        if (world.getGameTime() % CHECK_INTERVAL != 0) return;
        
        // Check if player has all 6 stones in gauntlet
        ItemStack gauntlet = null;
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == ModItems.INFINITY_GAUNTLET.get()) {
                gauntlet = stack;
                break;
            }
        }
        
        if (gauntlet == null) return;
        
        CompoundNBT nbt = gauntlet.getOrCreateTag();
        String[] stoneKeys = {"space_stone", "time_stone", "reality_stone", 
                              "power_stone", "mind_stone", "soul_stone"};
        
        int stoneCount = 0;
        for (String key : stoneKeys) {
            if (nbt.getBoolean(key)) stoneCount++;
        }
        
        // Need at least 4 stones for Thanos to spawn
        if (stoneCount < 4) return;
        
        // Check cooldown
        long lastAttempt = LAST_SPAWN_ATTEMPT.getOrDefault(player.getUUID(), 0L);
        if (world.getGameTime() - lastAttempt < CHECK_INTERVAL * 5) return;
        
        LAST_SPAWN_ATTEMPT.put(player.getUUID(), world.getGameTime());
        
        // Spawn chance increases with more stones
        Random rand = new Random();
        int chance = 150 - (stoneCount * 20); // 4 stones = 1/70, 6 stones = 1/30
        if (rand.nextInt(chance) != 0) return;
        
        // Spawn Thanos!
        spawnThanos(world, player);
    }
    
    private static void spawnThanos(ServerWorld world, PlayerEntity player) {
        Random rand = new Random();
        
        // Find spawn position
        double angle = rand.nextDouble() * Math.PI * 2;
        double distance = MIN_SPAWN_DISTANCE + rand.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
        
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        
        // Find ground level
        BlockPos spawnPos = new BlockPos(x, player.getY() + 10, z);
        while (!world.getBlockState(spawnPos.below()).getMaterial().isSolid() && spawnPos.getY() > 10) {
            spawnPos = spawnPos.below();
        }
        
        // Create Thanos
        ThanosEntity thanos = ModEntities.THANOS.get().create(world);
        if (thanos != null) {
            thanos.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            thanos.setTarget(player);
            world.addFreshEntity(thanos);
            
            // Announce to player
            player.displayClientMessage(
                new StringTextComponent("").append(
                    new StringTextComponent("☠ ").withStyle(TextFormatting.DARK_PURPLE)
                ).append(
                    new StringTextComponent("THANOS").withStyle(TextFormatting.DARK_PURPLE).withStyle(TextFormatting.BOLD)
                ).append(
                    new StringTextComponent(" is hunting you!").withStyle(TextFormatting.RED)
                ), false);
            
            player.displayClientMessage(
                new StringTextComponent("\"You should have gone for the head...\"")
                    .withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC), false);
            
            InfinityGauntletMod.LOGGER.info("Thanos spawned for player " + player.getName().getString());
        }
    }
}
