package net.voxelindustry.steamlayer.tile.modular;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public interface ICapabilityModule
{
    boolean hasCapability(Capability<?> capability, BlockPos from, @Nullable Direction facing);

    @Nullable
    <T> T getCapability(Capability<T> capability, BlockPos from, @Nullable Direction facing);
}
