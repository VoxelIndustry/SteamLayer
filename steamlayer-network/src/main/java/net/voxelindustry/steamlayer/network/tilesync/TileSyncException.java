package net.voxelindustry.steamlayer.network.tilesync;

import net.minecraft.world.level.block.entity.BlockEntity;

public class TileSyncException extends RuntimeException
{
    public TileSyncException(Class<?> tileClass, BlockEntity blockTile, String error)
    {
        super("Could not sync partial TileEntity. tile=" + tileClass + " pos=" + blockTile.getBlockPos() + " world=" + blockTile.getLevel() + " error=" + error);
    }
}
