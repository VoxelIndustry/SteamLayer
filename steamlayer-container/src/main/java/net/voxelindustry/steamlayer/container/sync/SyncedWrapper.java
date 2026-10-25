package net.voxelindustry.steamlayer.container.sync;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;

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

    public abstract void write(FriendlyByteBuf buffer, T value);

    public abstract T read(FriendlyByteBuf buffer);
}
