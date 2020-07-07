package net.voxelindustry.steamlayer.tile;

public interface ISyncTile
{
    void askServerSync();

    void forceSync();

    void sync();

    void syncLock();

    void releaseSyncLock(boolean flushSync);
}
