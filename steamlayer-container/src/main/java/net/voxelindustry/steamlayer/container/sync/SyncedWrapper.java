package net.voxelindustry.steamlayer.container.sync;

import net.minecraft.network.PacketByteBuf;

import java.util.Objects;

public abstract class SyncedWrapper<T>
{
    public boolean areEquals(T first, T second)
    {
        return Objects.equals(first, second);
    }

    public T copy(T original)
    {
        return original;
    }

    public abstract void write(PacketByteBuf buffer, T value);

    public abstract T read(PacketByteBuf buffer);
}
