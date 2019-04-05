package net.voxelindustry.steamlayer.container.sync;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Range;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Supplier;

public class SyncedArrayProperty<T> implements SyncedValue
{
    @Getter
    private final Supplier<T[]> supplier;

    @Getter
    @Setter
    protected T[]      stored;
    private   Class<T> elementClass;

    private Range<Integer> range;

    @Getter
    private int syncRate;
    private int lastSync;

    private SyncedWrapper<T> wrapper;

    public SyncedArrayProperty(Supplier<T[]> supplier, SyncedWrapper<T> wrapper, Class<T> elementClass,
                               Range<Integer> range, int syncRate)
    {
        this.supplier = supplier;
        this.wrapper = wrapper;
        this.stored = null;
        this.elementClass = elementClass;
        this.range = range;

        this.syncRate = syncRate;
        this.lastSync = 0;
    }

    public SyncedArrayProperty(Supplier<T[]> supplier, SyncedWrapper<T> wrapper, Class<T> elementClass,
                               Range<Integer> range)
    {
        this(supplier, wrapper, elementClass, range, 0);
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

        T[] supplied = this.supplier.get();

        if ((this.stored == null ^ supplied == null))
            return true;
        if (this.stored == null)
            return false;

        if (range != null)
        {
            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
            {
                if (!this.wrapper.areEquals(stored[index - range.getMinimum()], supplied[index]))
                    return true;
            }
        }
        else
        {
            for (int index = 0; index < supplied.length; index++)
            {
                if (!this.wrapper.areEquals(stored[index], supplied[index]))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void updateInternal()
    {
        T[] supplied = this.supplier.get();

        if (this.range != null)
        {
            this.stored = Arrays.copyOfRange(supplied, this.range.getMinimum(), this.range.getMaximum());

            for (int index = 0; index < this.stored.length; index++)
                this.stored[index] = this.wrapper.copy(supplied[index + range.getMinimum()]);
        }
        else
        {
            this.stored = Arrays.copyOf(supplied, supplied.length);

            for (int index = 0; index < this.stored.length; index++)
                this.stored[index] = this.wrapper.copy(supplied[index]);
        }
    }

    @Override
    public void update()
    {
        T[] supplied = this.supplier.get();

        if (this.range != null)
        {
            for (int index = 0; index < this.stored.length; index++)
                supplied[index + range.getMinimum()] = this.wrapper.copy(this.stored[index]);
        }
        else
        {
            for (int index = 0; index < this.stored.length; index++)
                supplied[index] = this.wrapper.copy(this.stored[index]);
        }
    }

    @Override
    public void write(ByteBuf buffer)
    {
        buffer.writeInt(this.stored.length);

        for (T element : this.stored)
            this.wrapper.write(buffer, element);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(ByteBuf buffer)
    {
        int count = buffer.readInt();
        if (this.stored == null)
            this.stored = (T[]) Array.newInstance(this.elementClass, count);

        for (int index = 0; index < this.stored.length; index++)
            this.stored[index] = this.wrapper.read(buffer);
    }
}
