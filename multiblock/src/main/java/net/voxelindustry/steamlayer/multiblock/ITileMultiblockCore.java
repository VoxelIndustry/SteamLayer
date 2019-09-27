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
    void breakCore();

    @Nullable
    <T> LazyOptional<T> getCapability(Capability<T> capability, BlockPos from, @Nullable Direction facing);

    default ITileMultiblockCore getCore()
    {
        return this;
    }

    default BlockPos getCoreOffset()
    {
        return BlockPos.ZERO;
    }

    default boolean isCore()
    {
        return true;
    }

    default boolean isCorePresent()
    {
        return true;
    }

    default boolean onRightClick(PlayerEntity player, RayTraceResult hit, BlockPos from)
    {
        return false;
    }
}
