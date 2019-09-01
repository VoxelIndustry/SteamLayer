package net.voxelindustry.steamlayer.container.sync;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
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
        this.stored = new ArrayList<>();
        this.range = range;

        this.syncRate = syncRate;
        this.lastSync = 0;
    }

    public SyncedEnumListProperty(Supplier<List<E>> supplier, Class<E> enumClass, Range<Integer> range)
    {
        this(supplier, enumClass, range, 0);
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

        List<E> supplied = this.supplier.get();

        if ((this.stored == null ^ supplied == null))
            return true;
        if (this.stored == null)
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
        List<E> supplied = this.supplier.get();

        if (this.range != null)
        {
            this.stored.clear();

            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
                this.stored.add(supplied.get(index));
        }
        else
        {
            this.stored.clear();
            stored.addAll(supplied);
        }
    }

    @Override
    public void update()
    {
        List<E> supplied = this.supplier.get();

        if (this.range != null)
        {
            for (int index = range.getMinimum(); index < range.getMaximum(); index++)
            {
                if (supplied.size() <= index)
                    supplied.add(this.stored.get(index - range.getMinimum()));
                else
                    supplied.set(index, this.stored.get(index - range.getMinimum()));
            }
        }
        else
        {
            supplied.clear();

            for (int index = 0; index < this.stored.size(); index++)
            {
                if (supplied.size() <= index)
                    supplied.add(this.stored.get(index));
                else
                    supplied.set(index, this.stored.get(index));
            }
        }
    }

    @Override
    public void write(ByteBuf buffer)
    {
        buffer.writeInt(this.stored.size());

        for (E element : this.stored)
            buffer.writeInt(element.ordinal());
    }

    @Override
    public void read(ByteBuf buffer)
    {
        this.stored.clear();

        int count = buffer.readInt();
        for (int index = 0; index < count; index++)
            this.stored.add(SharedSecrets.getJavaLangAccess().getEnumConstantsShared(enumClass)[buffer.readInt()]);
    }
}