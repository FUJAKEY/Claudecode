package com.arcanemagic.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Capability provider that attaches mana storage to players
 */
public class ManaProvider implements ICapabilitySerializable<CompoundNBT> {

    private final ManaStorage manaStorage;
    private final LazyOptional<ManaCapability> optional;

    public ManaProvider() {
        this.manaStorage = new ManaStorage();
        this.optional = LazyOptional.of(() -> manaStorage);
    }

    public ManaProvider(PlayerEntity player) {
        this();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ManaCapability.getCapability()) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return manaStorage.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        manaStorage.deserializeNBT(nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
