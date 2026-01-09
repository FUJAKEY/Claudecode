package com.arcanemagic.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Mana capability interface - defines the contract for mana storage
 */
public interface ManaCapability {

    @CapabilityInject(ManaCapability.class)
    Capability<ManaCapability> MANA_CAPABILITY = null;

    /**
     * Get current mana amount
     */
    int getMana();

    /**
     * Set current mana (capped to max)
     */
    void setMana(int mana);

    /**
     * Get maximum mana capacity
     */
    int getMaxMana();

    /**
     * Set maximum mana capacity
     */
    void setMaxMana(int maxMana);

    /**
     * Consume mana for spell casting
     * 
     * @return true if enough mana was available
     */
    boolean consumeMana(int amount);

    /**
     * Regenerate mana (called every tick from altar or naturally)
     */
    void regenerateMana(int amount);

    /**
     * Get mana regeneration rate (per second)
     */
    int getManaRegenRate();

    /**
     * Set mana regeneration rate
     */
    void setManaRegenRate(int rate);

    /**
     * Serialize to NBT for saving
     */
    CompoundNBT serializeNBT();

    /**
     * Deserialize from NBT for loading
     */
    void deserializeNBT(CompoundNBT nbt);

    /**
     * Copy data from another capability (for respawn)
     */
    void copyFrom(ManaCapability other);

    /**
     * Register the capability with Forge
     */
    static void register() {
        CapabilityManager.INSTANCE.register(
                ManaCapability.class,
                new ManaCapabilityStorage(),
                ManaStorage::new);
    }
}
