package net.voxelindustry.steamlayer.multiblock;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public interface ITileMultiblockCore extends ITileMultiblock
{
    @Override
    void breakCore();

    @Override
    default ITileMultiblockCore getCore()
    {
        return this;
    }

    @Override
    default BlockPos getCoreOffset()
    {
        return BlockPos.ORIGIN;
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

    default ActionResult onRightClick(PlayerEntity player, BlockHitResult hit, BlockPos from)
    {
        return ActionResult.PASS;
    }
}
