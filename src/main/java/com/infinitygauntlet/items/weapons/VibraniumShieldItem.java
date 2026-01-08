package com.infinitygauntlet.items.weapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class VibraniumShieldItem extends ShieldItem {
    
    public VibraniumShieldItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (player.isShiftKeyDown()) {
            // Shield bash - damage and knockback nearby enemies
            if (!world.isClientSide) {
                AxisAlignedBB area = player.getBoundingBox().inflate(4);
                List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, 
                    e -> e != player);
                
                Vector3d lookVec = player.getLookAngle();
                
                for (LivingEntity entity : entities) {
                    Vector3d toEntity = entity.position().subtract(player.position()).normalize();
                    double dot = lookVec.dot(toEntity);
                    
                    if (dot > 0.5) { // In front of player
                        entity.hurt(DamageSource.playerAttack(player), 8.0F);
                        entity.setDeltaMovement(toEntity.scale(2).add(0, 0.5, 0));
                        entity.hurtMarked = true;
                    }
                }
                
                // Deflect projectiles
                AxisAlignedBB projectileArea = player.getBoundingBox().inflate(5);
                List<AbstractArrowEntity> arrows = world.getEntitiesOfClass(AbstractArrowEntity.class, projectileArea);
                
                for (AbstractArrowEntity arrow : arrows) {
                    // Reverse direction
                    arrow.setDeltaMovement(arrow.getDeltaMovement().reverse().scale(1.5));
                    arrow.setOwner(player);
                }
                
                if (world instanceof ServerWorld) {
                    ((ServerWorld) world).sendParticles(ParticleTypes.CRIT,
                        player.getX() + lookVec.x * 2, 
                        player.getY() + 1, 
                        player.getZ() + lookVec.z * 2, 
                        20, 1, 0.5, 1, 0.2);
                }
                
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SHIELD_BLOCK, SoundCategory.PLAYERS, 1.5F, 0.8F);
            }
            
            player.getCooldowns().addCooldown(this, 40);
            return ActionResult.success(stack);
        }
        
        return super.use(world, player, hand);
    }
    
    @Override
    public boolean isShield(ItemStack stack, LivingEntity entity) {
        return true;
    }
    
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }
    
    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BLOCK;
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
