package com.gpiay.cpm.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CPMCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
    @CapabilityInject(ICPMCapability.class)
    public static Capability<ICPMCapability> CAPABILITY;

    private final ICPMCapability cpmCapability;

    public CPMCapabilityProvider(LivingEntity entity) {
        cpmCapability = entity.level.isClientSide ? new ClientCPMCapability(entity) : new ServerCPMCapability(entity);
    }

    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(
                ICPMCapability.class,
                new Capability.IStorage<ICPMCapability>() {
                    @Nullable
                    @Override
                    public INBT writeNBT(Capability<ICPMCapability> capability, ICPMCapability instance, Direction side) {
                        return instance.serializeNBT();
                    }

                    @Override
                    public void readNBT(Capability<ICPMCapability> capability, ICPMCapability instance, Direction side, INBT nbt) {
                        instance.deserializeNBT((CompoundNBT) nbt);
                    }
                },
                () -> null
        );
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? LazyOptional.of(() -> cpmCapability).cast() : LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return cpmCapability.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        cpmCapability.deserializeNBT(nbt);
    }
}
