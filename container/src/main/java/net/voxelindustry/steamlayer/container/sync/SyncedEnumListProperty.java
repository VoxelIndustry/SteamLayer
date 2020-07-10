package net.voxelindustry.steamlayer.container.sync;

import lombok.Getter;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.Range;
import sun.misc.SharedSecrets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncedEnumListProperty<E extends Enum<E>> implements SyncedValue
{
    @Getter
    private final Supplier<List<E>> supplier;

    @Getter
    protected final List<E> stored;

    private Range<Integer> range;

    @Getter
    private int syncRate;
    private int lastSync;

    private Class<E> enumClass;

    public SyncedEnumListProperty(Supplier<List<E>> supplier, Class<E> enumClass, Range<Integer> range, int syncRate)
    {
        this.supplier = supplier;
        this.enumClass = enumClass;
        stored = new ArrayList<>();
        this.range = range;

        this.syncRate = syncRate;
        lastSync = 0;
    }

    public SyncedEnumListProperty(Supplier<List<E>> supplier, Class<E> enumClass, Range<Integer> range)
    {
        this(supplier, enumClass, range, 0);
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

        List<E> supplied = supplier.get();

        if ((stored == null ^ supplied == null))
            return true;
        if (stored == null)
            return false;

        if (range != null)
        {
            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
            {
                if (stored.get(index - range.getMinimum()) != supplied.get(index))
                    return true;
            }
        }
        else
        {
            if (stored.size() != supplied.size())
                return true;

            for (int index = 0; index < supplied.size(); index++)
            {
                if (stored.get(index) != supplied.get(index))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void updateInternal()
    {
        List<E> supplied = supplier.get();

        if (range != null)
        {
            stored.clear();

            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
                stored.add(supplied.get(index));
        }
        else
        {
            stored.clear();
            stored.addAll(supplied);
        }
    }

    @Override
    public void update()
    {
        List<E> supplied = supplier.get();

        if (range != null)
        {
            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
            {
                if (supplied.size() <= index)
                    supplied.add(stored.get(index - range.getMinimum()));
                else
                    supplied.set(index, stored.get(index - range.getMinimum()));
            }
        }
        else
        {
            supplied.clear();

            for (int index = 0; index < stored.size(); index++)
            {
                if (supplied.size() <= index)
                    supplied.add(stored.get(index));
                else
                    supplied.set(index, stored.get(index));
            }
        }
    }

    @Override
    public void write(PacketByteBuf buffer)
    {
        buffer.writeInt(stored.size());

        for (E element : stored)
            buffer.writeInt(element.ordinal());
    }

    @Override
    public void read(PacketByteBuf buffer)
    {
        stored.clear();

        int count = buffer.readInt();
        for (int index = 0; index < count; index++)
            stored.add(SharedSecrets.getJavaLangAccess().getEnumConstantsShared(enumClass)[buffer.readInt()]);
    }
}