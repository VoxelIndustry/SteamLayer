package net.voxelindustry.steamlayer.container.sync;

import io.netty.buffer.ByteBuf;

public interface SyncedValue
{
    boolean needRefresh();

    void updateInternal();

    void update();

    void write(ByteBuf buffer);

    void read(ByteBuf buffer);
}
