package net.voxelindustry.steamlayer.container.sync;

import net.minecraft.network.PacketByteBuf;

public interface SyncedValue
{
    boolean needRefresh();

    void updateInternal();

    void update();

    void write(PacketByteBuf buffer);

    void read(PacketByteBuf buffer);
}
