package net.voxelindustry.steamlayer.multiblock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface RightClickAction
{
    RightClickAction EMPTY = (w, pos, state, player, hand, side, hitX, hitY, hitZ) -> false;

    boolean apply(World w, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side,
                  float hitX, float hitY, float hitZ);
}
