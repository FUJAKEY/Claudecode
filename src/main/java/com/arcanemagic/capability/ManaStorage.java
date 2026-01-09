package com.arcanemagic.capability;

import net.minecraft.nbt.CompoundNBT;

/**
 * Implementation of ManaCapability - stores actual mana data
 */
public class ManaStorage implements ManaCapability {

    private int mana;
    private int maxMana;
    private int regenRate;

    // Default values
    public static final int DEFAULT_MAX_MANA = 100;
    public static final int DEFAULT_REGEN_RATE = 1; // per second

    public ManaStorage() {
        this.mana = DEFAULT_MAX_MANA;
        this.maxMana = DEFAULT_MAX_MANA;
        this.regenRate = DEFAULT_REGEN_RATE;
    }

    public ManaStorage(int maxMana) {
        this.mana = maxMana;
        this.maxMana = maxMana;
        this.regenRate = DEFAULT_REGEN_RATE;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int mana) {
        this.mana = Math.max(0, Math.min(mana, maxMana));
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public void setMaxMana(int maxMana) {
        this.maxMana = Math.max(1, maxMana);
        if (this.mana > this.maxMana) {
            this.mana = this.maxMana;
        }
    }

    @Override
    public boolean consumeMana(int amount) {
        if (mana >= amount) {
            mana -= amount;
            return true;
        }
        return false;
    }

    @Override
    public void regenerateMana(int amount) {
        setMana(mana + amount);
    }

    @Override
    public int getManaRegenRate() {
        return regenRate;
    }

    @Override
    public void setManaRegenRate(int rate) {
        this.regenRate = Math.max(0, rate);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("mana", mana);
        nbt.putInt("maxMana", maxMana);
        nbt.putInt("regenRate", regenRate);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.maxMana = nbt.getInt("maxMana");
        this.mana = nbt.getInt("mana");
        this.regenRate = nbt.getInt("regenRate");

        // Ensure valid state
        if (maxMana <= 0)
            maxMana = DEFAULT_MAX_MANA;
        if (regenRate <= 0)
            regenRate = DEFAULT_REGEN_RATE;
        if (mana > maxMana)
            mana = maxMana;
        if (mana < 0)
            mana = 0;
    }

    @Override
    public void copyFrom(ManaCapability other) {
        this.mana = other.getMana();
        this.maxMana = other.getMaxMana();
        this.regenRate = other.getManaRegenRate();
    }
}
