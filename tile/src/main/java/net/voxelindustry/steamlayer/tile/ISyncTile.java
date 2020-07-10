package net.voxelindustry.steamlayer.tile;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;

public interface ISyncTile extends BlockEntityClientSerializable
{
    void askServerSync();

    void forceSync();

    void syncLock();

    void releaseSyncLock(boolean flushSync);
}
