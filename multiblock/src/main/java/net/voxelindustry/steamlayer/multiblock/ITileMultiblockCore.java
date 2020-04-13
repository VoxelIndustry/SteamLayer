package net.voxelindustry.steamlayer.multiblock;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public interface ITileMultiblockCore extends ITileMultiblock
{
    @Override
    void breakCore();

    @Nullable
    <T> LazyOptional<T> getCapability(Capability<T> capability, BlockPos from, @Nullable Direction facing);

    @Override
    default ITileMultiblockCore getCore()
    {
        return this;
    }

    @Override
    default BlockPos getCoreOffset()
    {
        return BlockPos.ZERO;
    }

    @Override
    default boolean isCore()
    {
        return true;
    }

    @Override
    default boolean isCorePresent()
    {
        return true;
    }

    default boolean onRightClick(PlayerEntity player, RayTraceResult hit, BlockPos from)
    {
        return false;
    }
}
