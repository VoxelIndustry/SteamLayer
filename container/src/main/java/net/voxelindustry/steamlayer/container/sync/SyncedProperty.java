package net.voxelindustry.steamlayer.container.sync;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

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
        this.stored = null;

        this.syncRate = syncRate;
        this.lastSync = 0;
    }

    public SyncedProperty(Supplier<T> supplier, Consumer<T> consumer, SyncedWrapper<T> wrapper)
    {
        this(supplier, consumer, wrapper, 0);
    }

    @Override
    public boolean needRefresh()
    {
        if (this.lastSync != this.syncRate)
        {
            this.lastSync++;
            return false;
        }
        this.lastSync = 0;

        T supplied = this.supplier.get();

        if ((this.stored == null ^ supplied == null))
            return true;
        if (this.stored == null)
            return false;

        return !this.wrapper.areEquals(stored, supplied);
    }

    @Override
    public void updateInternal()
    {
        this.stored = this.wrapper.copy(this.supplier.get());
    }

    @Override
    public void update()
    {
        this.consumer.accept(this.stored);
    }

    @Override
    public void write(ByteBuf buffer)
    {
        this.wrapper.write(buffer, this.stored);
    }

    @Override
    public void read(ByteBuf buffer)
    {
        this.stored = this.wrapper.read(buffer);
    }
}
