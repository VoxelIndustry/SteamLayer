package net.voxelindustry.steamlayer.container.sync;

import io.netty.buffer.ByteBuf;

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

    public abstract void write(ByteBuf buffer, T value);

    public abstract T read(ByteBuf buffer);
}
