package com.demonslayer.init;

import com.demonslayer.DemonSlayerMod;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = 
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DemonSlayerMod.MOD_ID);

    // Breathing effects
    public static final RegistryObject<BasicParticleType> WATER_BREATHING = 
            PARTICLES.register("water_breathing", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> FLAME_BREATHING = 
            PARTICLES.register("flame_breathing", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> THUNDER_BREATHING = 
            PARTICLES.register("thunder_breathing", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> WIND_BREATHING = 
            PARTICLES.register("wind_breathing", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> MIST_BREATHING = 
            PARTICLES.register("mist_breathing", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> LOVE_BREATHING = 
            PARTICLES.register("love_breathing", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> SUN_BREATHING = 
            PARTICLES.register("sun_breathing", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> BEAST_BREATHING = 
            PARTICLES.register("beast_breathing", () -> new BasicParticleType(false));

    // Demon effects
    public static final RegistryObject<BasicParticleType> DEMON_BLOOD = 
            PARTICLES.register("demon_blood", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> MUZAN_AURA = 
            PARTICLES.register("muzan_aura", () -> new BasicParticleType(false));

    // Slayer effects
    public static final RegistryObject<BasicParticleType> SLAYER_MARK = 
            PARTICLES.register("slayer_mark", () -> new BasicParticleType(false));
    
    public static final RegistryObject<BasicParticleType> HASHIRA_AURA = 
            PARTICLES.register("hashira_aura", () -> new BasicParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
