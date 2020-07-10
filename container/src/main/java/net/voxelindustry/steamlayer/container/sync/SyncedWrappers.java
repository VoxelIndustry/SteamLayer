package net.voxelindustry.steamlayer.container.sync;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;
import net.voxelindustry.steamlayer.inventory.InventoryHandler;

import java.util.IdentityHashMap;
import java.util.Map;

public class SyncedWrappers
{
    private static SyncedWrappers INSTANCE;

    public static SyncedWrappers instance()
    {
        if (INSTANCE == null)
            INSTANCE = new SyncedWrappers();
        return INSTANCE;
    }

    private Map<Class<?>, SyncedWrapper<?>> wrappers;

    private SyncedWrappers()
    {
        wrappers = new IdentityHashMap<>();

        wrappers.put(Integer.class, new SyncedWrapper<Integer>()
        {
            @Override
            public void write(PacketByteBuf buffer, Integer value)
            {
                buffer.writeInt(value);
            }

            @Override
            public Integer read(PacketByteBuf buffer)
            {
                return buffer.readInt();
            }
        });

        wrappers.put(Float.class, new SyncedWrapper<Float>()
        {
            @Override
            public void write(PacketByteBuf buffer, Float value)
            {
                buffer.writeFloat(value);
            }

            @Override
            public Float read(PacketByteBuf buffer)
            {
                return buffer.readFloat();
            }
        });

        wrappers.put(Boolean.class, new SyncedWrapper<Boolean>()
        {
            @Override
            public void write(PacketByteBuf buffer, Boolean value)
            {
                buffer.writeBoolean(value);
            }

            @Override
            public Boolean read(PacketByteBuf buffer)
            {
                return buffer.readBoolean();
            }
        });

        wrappers.put(Long.class, new SyncedWrapper<Long>()
        {
            @Override
            public void write(PacketByteBuf buffer, Long value)
            {
                buffer.writeLong(value);
            }

            @Override
            public Long read(PacketByteBuf buffer)
            {
                return buffer.readLong();
            }
        });

        wrappers.put(String.class, new SyncedWrapper<String>()
        {
            @Override
            public void write(PacketByteBuf buffer, String value)
            {
                buffer.writeString(value);
            }

            @Override
            public String read(PacketByteBuf buffer)
            {
                return buffer.readString();
            }
        });

        wrappers.put(ItemStack.class, new SyncedWrapper<ItemStack>()
        {
            @Override
            public void write(PacketByteBuf buffer, ItemStack value)
            {
                buffer.writeItemStack(value);
            }

            @Override
            public ItemStack read(PacketByteBuf buffer)
            {
                return buffer.readItemStack();
            }

            @Override
            public boolean areEquals(ItemStack first, ItemStack second)
            {
                return ItemUtils.deepEquals(first, second);
            }

            @Override
            public ItemStack copy(ItemStack original)
            {
                return original.copy();
            }
        });

        wrappers.put(Inventory.class, new SyncedWrapper<Inventory>()
        {
            @Override
            public void write(PacketByteBuf buffer, Inventory value)
            {
                buffer.writeInt(value.size());

                for (int slot = 0; slot < value.size(); slot++)
                    buffer.writeItemStack(value.getStack(slot));
            }

            @Override
            public Inventory read(PacketByteBuf buffer)
            {
                InventoryHandler inventory = new InventoryHandler(buffer.readInt());

                for (int slot = 0; slot < inventory.size(); slot++)
                    inventory.setStack(slot, buffer.readItemStack());
                return inventory;
            }

            @Override
            public boolean areEquals(Inventory first, Inventory second)
            {
                if (second.size() != first.size())
                    return false;
                for (int slot = 0; slot < first.size(); slot++)
                {
                    if (!ItemUtils.deepEqualsWithAmount(first.getStack(slot), second.getStack(slot)))
                        return false;
                }
                return true;
            }

            @Override
            public Inventory copy(Inventory original)
            {
                InventoryHandler copy = new InventoryHandler(original.size());

                for (int slot = 0; slot < original.size(); slot++)
                    copy.setStack(slot, original.getStack(slot).copy());
                return copy;
            }
        });
    }

    public <T> SyncedWrapper<T> get(Class<T> typeClass)
    {
        return (SyncedWrapper<T>) wrappers.get(typeClass);
    }

    public <T> void registerWrapper(SyncedWrapper<T> wrapper, Class<T> typeClass)
    {
        wrappers.put(typeClass, wrapper);
    }
}
