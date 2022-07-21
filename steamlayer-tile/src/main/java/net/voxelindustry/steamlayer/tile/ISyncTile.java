package net.voxelindustry.steamlayer.tile;

public interface ISyncTile
{
    void askServerSync();

    void sync();

    void forceSync();

    void syncLock();

    void releaseSyncLock(boolean flushSync);
}
