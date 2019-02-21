package net.voxelindustry.steamlayer.container.sync;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SyncableProperty<T extends Object>
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

    public SyncableProperty(Supplier<T> supplier, Consumer<T> consumer, int syncRate)
    {
        this.supplier = supplier;
        this.consumer = consumer;
        this.stored = null;

        this.syncRate = syncRate;
        this.lastSync = 0;
    }

    public SyncableProperty(Supplier<T> supplier, Consumer<T> consumer)
    {
        this(supplier, consumer, 0);
    }

    public boolean areEquals(T other)
    {
        return this.stored.equals(other);
    }

    public boolean needRefresh()
    {
        if (this.lastSync != this.syncRate)
        {
            this.lastSync++;
            return false;
        }
        this.lastSync = 0;

        T supplied = this.supplier.get();

        if((this.stored == null ^ supplied == null))
            return true;
        if(this.stored == null)
            return false;

        return  !this.areEquals(supplied);
    }

    public void updateInternal()
    {
        this.stored = this.copy(this.supplier.get());
    }

    public void update()
    {
        this.consumer.accept(this.stored);
    }

    public abstract NBTTagCompound toNBT(NBTTagCompound tag);

    public abstract void fromNBT(NBTTagCompound tag);

    public T copy(T original)
    {
        return original;
    }
}
