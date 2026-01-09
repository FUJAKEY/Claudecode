package com.arcanemagic.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Storage handler for ManaCapability - handles NBT serialization
 */
public class ManaCapabilityStorage implements Capability.IStorage<ManaCapability> {

    @Nullable
    @Override
    public INBT writeNBT(Capability<ManaCapability> capability, ManaCapability instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<ManaCapability> capability, ManaCapability instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            instance.deserializeNBT((CompoundNBT) nbt);
        }
    }
}
