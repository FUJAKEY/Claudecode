package com.arcanemagic.init;

import com.arcanemagic.ArcaneMagicMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Registry for all mod sounds
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(
            ForgeRegistries.SOUND_EVENTS, ArcaneMagicMod.MOD_ID);

    // THE WORLD sound effect
    public static final RegistryObject<SoundEvent> THE_WORLD = SOUNDS.register("the_world",
            () -> new SoundEvent(new ResourceLocation(ArcaneMagicMod.MOD_ID, "the_world")));
}
