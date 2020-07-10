package net.voxelindustry.steamlayer.container.sync;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.PacketByteBuf;
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
        stored = null;
        this.elementClass = elementClass;
        this.range = range;

        this.syncRate = syncRate;
        lastSync = 0;
    }

    public SyncedArrayProperty(Supplier<T[]> supplier, SyncedWrapper<T> wrapper, Class<T> elementClass,
                               Range<Integer> range)
    {
        this(supplier, wrapper, elementClass, range, 0);
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

        T[] supplied = supplier.get();

        if ((stored == null ^ supplied == null))
            return true;
        if (stored == null)
            return false;

        if (range != null)
        {
            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
            {
                if (!wrapper.areEquals(stored[index - range.getMinimum()], supplied[index]))
                    return true;
            }
        }
        else
        {
            for (int index = 0; index < supplied.length; index++)
            {
                if (!wrapper.areEquals(stored[index], supplied[index]))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void updateInternal()
    {
        T[] supplied = supplier.get();

        if (range != null)
        {
            stored = Arrays.copyOfRange(supplied, range.getMinimum(), range.getMaximum());

            for (int index = 0; index < stored.length; index++)
                stored[index] = wrapper.copy(supplied[index + range.getMinimum()]);
        }
        else
        {
            stored = Arrays.copyOf(supplied, supplied.length);

            for (int index = 0; index < stored.length; index++)
                stored[index] = wrapper.copy(supplied[index]);
        }
    }

    @Override
    public void update()
    {
        T[] supplied = supplier.get();

        if (range != null)
        {
            for (int index = 0; index < stored.length; index++)
                supplied[index + range.getMinimum()] = wrapper.copy(stored[index]);
        }
        else
        {
            for (int index = 0; index < stored.length; index++)
                supplied[index] = wrapper.copy(stored[index]);
        }
    }

    @Override
    public void write(PacketByteBuf buffer)
    {
        buffer.writeInt(stored.length);

        for (T element : stored)
            wrapper.write(buffer, element);
    }

    @Override
    public void read(PacketByteBuf buffer)
    {
        int count = buffer.readInt();
        if (stored == null)
            stored = (T[]) Array.newInstance(elementClass, count);

        for (int index = 0; index < stored.length; index++)
            stored[index] = wrapper.read(buffer);
    }
}
