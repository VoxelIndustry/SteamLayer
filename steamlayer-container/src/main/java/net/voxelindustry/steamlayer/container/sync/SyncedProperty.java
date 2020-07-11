package net.voxelindustry.steamlayer.container.sync;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SyncedProperty<T> implements SyncedValue
{
    @Getter
    private final Supplier<T> supplier;
    @Getter
    private final Consumer<T> consumer;
    @Getter
    @Setter
    protected     T           stored;

    @Getter
    private int syncRate;
    private int lastSync;

    private SyncedWrapper<T> wrapper;

    public SyncedProperty(Supplier<T> supplier, Consumer<T> consumer, SyncedWrapper<T> wrapper, int syncRate)
    {
        this.supplier = supplier;
        this.consumer = consumer;
        this.wrapper = wrapper;
        stored = null;

        this.syncRate = syncRate;
        lastSync = 0;
    }

    public SyncedProperty(Supplier<T> supplier, Consumer<T> consumer, SyncedWrapper<T> wrapper)
    {
        this(supplier, consumer, wrapper, 0);
    }

    @Override
    public boolean needRefresh()
    {
        if (lastSync != syncRate)
        {
            lastSync++;
            return false;
        }
        lastSync = 0;

        T supplied = supplier.get();

        if ((stored == null ^ supplied == null))
            return true;
        if (stored == null)
            return false;

        return !wrapper.areEquals(stored, supplied);
    }

    @Override
    public void updateInternal()
    {
        stored = wrapper.copy(supplier.get());
    }

    @Override
    public void update()
    {
        consumer.accept(stored);
    }

    @Override
    public void write(PacketByteBuf buffer)
    {
        wrapper.write(buffer, stored);
    }

    @Override
    public void read(PacketByteBuf buffer)
    {
        stored = wrapper.read(buffer);
    }
}
