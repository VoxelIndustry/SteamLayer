package net.voxelindustry.steamlayer.container.sync;

@FunctionalInterface
public interface ISyncCallback
{
    void onSync(SyncedValue property);
}
