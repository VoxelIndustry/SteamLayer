package net.voxelindustry.steamlayer.container.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;

public interface SyncedValue
{
    boolean needRefresh();

    void updateInternal();

    void update();

    void write(RegistryFriendlyByteBuf buffer);

    void read(RegistryFriendlyByteBuf buffer);
}
