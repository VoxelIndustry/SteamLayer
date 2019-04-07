package net.voxelindustry.steamlayer.container.sync;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncedListProperty<T> implements SyncedValue
{
    @Getter
    private final Supplier<List<T>> supplier;

    @Getter
    protected final List<T> stored;

    private Range<Integer> range;

    @Getter
    private int syncRate;
    private int lastSync;

    private SyncedWrapper<T> wrapper;

    public SyncedListProperty(Supplier<List<T>> supplier, SyncedWrapper<T> wrapper, Range<Integer> range, int syncRate)
    {
        this.supplier = supplier;
        this.wrapper = wrapper;
        this.stored = new ArrayList<>();
        this.range = range;

        this.syncRate = syncRate;
        this.lastSync = 0;
    }

    public SyncedListProperty(Supplier<List<T>> supplier, SyncedWrapper<T> wrapper, Range<Integer> range)
    {
        this(supplier, wrapper, range, 0);
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

        List<T> supplied = this.supplier.get();

        if ((this.stored == null ^ supplied == null))
            return true;
        if (this.stored == null)
            return false;

        if (range != null)
        {
            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
            {
                if (!this.wrapper.areEquals(stored.get(index - range.getMinimum()), supplied.get(index)))
                    return true;
            }
        }
        else
        {
            if (stored.size() != supplied.size())
                return true;

            for (int index = 0; index < supplied.size(); index++)
            {
                if (!this.wrapper.areEquals(stored.get(index), supplied.get(index)))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void updateInternal()
    {
        List<T> supplied = this.supplier.get();

        if (this.range != null)
        {
            this.stored.clear();

            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
                this.stored.add(this.wrapper.copy(supplied.get(index)));
        }
        else
        {
            this.stored.clear();
            supplied.forEach(element -> stored.add(wrapper.copy(element)));
        }
    }

    @Override
    public void update()
    {
        List<T> supplied = this.supplier.get();

        if (this.range != null)
        {
            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
            {
                if (supplied.size() <= index)
                    supplied.add(this.wrapper.copy(this.stored.get(index - range.getMinimum())));
                else
                    supplied.set(index, this.wrapper.copy(this.stored.get(index - range.getMinimum())));
            }
        }
        else
        {
            for (int index = 0; index < this.stored.size(); index++)
            {
                if (supplied.size() <= index)
                    supplied.add(this.wrapper.copy(this.stored.get(index)));
                else
                    supplied.set(index, this.wrapper.copy(this.stored.get(index)));
            }
        }
    }

    @Override
    public void write(ByteBuf buffer)
    {
        buffer.writeInt(this.stored.size());

        for (T element : this.stored)
            this.wrapper.write(buffer, element);
    }

    @Override
    public void read(ByteBuf buffer)
    {
        this.stored.clear();

        int count = buffer.readInt();
        for (int index = 0; index < count; index++)
            this.stored.add(this.wrapper.read(buffer));
    }
}
