package net.voxelindustry.steamlayer.container.sync;

import net.minecraft.network.FriendlyByteBuf;

public interface SyncedValue
{
    boolean needRefresh();

    void updateInternal();

    void update();

    void write(FriendlyByteBuf buffer);

    void read(FriendlyByteBuf buffer);
}
