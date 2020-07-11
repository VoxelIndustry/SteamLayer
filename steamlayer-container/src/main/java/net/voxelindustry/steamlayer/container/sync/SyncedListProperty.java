package net.voxelindustry.steamlayer.container.sync;

import lombok.Getter;
import net.minecraft.network.PacketByteBuf;
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
        stored = new ArrayList<>();
        this.range = range;

        this.syncRate = syncRate;
        lastSync = 0;
    }

    public SyncedListProperty(Supplier<List<T>> supplier, SyncedWrapper<T> wrapper, Range<Integer> range)
    {
        this(supplier, wrapper, range, 0);
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

        List<T> supplied = supplier.get();

        if ((stored == null ^ supplied == null))
            return true;
        if (stored == null)
            return false;

        if (range != null)
        {
            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
            {
                if (!wrapper.areEquals(stored.get(index - range.getMinimum()), supplied.get(index)))
                    return true;
            }
        }
        else
        {
            if (stored.size() != supplied.size())
                return true;

            for (int index = 0; index < supplied.size(); index++)
            {
                if (!wrapper.areEquals(stored.get(index), supplied.get(index)))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void updateInternal()
    {
        List<T> supplied = supplier.get();

        if (range != null)
        {
            stored.clear();

            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
                stored.add(wrapper.copy(supplied.get(index)));
        }
        else
        {
            stored.clear();
            supplied.forEach(element -> stored.add(wrapper.copy(element)));
        }
    }

    @Override
    public void update()
    {
        List<T> supplied = supplier.get();

        if (range != null)
        {
            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
            {
                if (supplied.size() <= index)
                    supplied.add(wrapper.copy(stored.get(index - range.getMinimum())));
                else
                    supplied.set(index, wrapper.copy(stored.get(index - range.getMinimum())));
            }
        }
        else
        {
            supplied.clear();

            for (int index = 0; index < stored.size(); index++)
            {
                if (supplied.size() <= index)
                    supplied.add(wrapper.copy(stored.get(index)));
                else
                    supplied.set(index, wrapper.copy(stored.get(index)));
            }
        }
    }

    @Override
    public void write(PacketByteBuf buffer)
    {
        buffer.writeInt(stored.size());

        for (T element : stored)
            wrapper.write(buffer, element);
    }

    @Override
    public void read(PacketByteBuf buffer)
    {
        stored.clear();

        int count = buffer.readInt();
        for (int index = 0; index < count; index++)
            stored.add(wrapper.read(buffer));
    }
}
