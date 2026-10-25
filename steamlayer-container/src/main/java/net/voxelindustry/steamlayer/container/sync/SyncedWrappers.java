package net.voxelindustry.steamlayer.container.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
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
            public void write(RegistryFriendlyByteBuf buffer, Integer value)
            {
                buffer.writeInt(value);
            }

            @Override
            public Integer read(RegistryFriendlyByteBuf buffer)
            {
                return buffer.readInt();
            }
        });

        wrappers.put(Float.class, new SyncedWrapper<Float>()
        {
            @Override
            public void write(RegistryFriendlyByteBuf buffer, Float value)
            {
                buffer.writeFloat(value);
            }

            @Override
            public Float read(RegistryFriendlyByteBuf buffer)
            {
                return buffer.readFloat();
            }
        });

        wrappers.put(Boolean.class, new SyncedWrapper<Boolean>()
        {
            @Override
            public void write(RegistryFriendlyByteBuf buffer, Boolean value)
            {
                buffer.writeBoolean(value);
            }

            @Override
            public Boolean read(RegistryFriendlyByteBuf buffer)
            {
                return buffer.readBoolean();
            }
        });

        wrappers.put(Long.class, new SyncedWrapper<Long>()
        {
            @Override
            public void write(RegistryFriendlyByteBuf buffer, Long value)
            {
                buffer.writeLong(value);
            }

            @Override
            public Long read(RegistryFriendlyByteBuf buffer)
            {
                return buffer.readLong();
            }
        });

        wrappers.put(String.class, new SyncedWrapper<String>()
        {
            @Override
            public void write(RegistryFriendlyByteBuf buffer, String value)
            {
                buffer.writeUtf(value);
            }

            @Override
            public String read(RegistryFriendlyByteBuf buffer)
            {
                return buffer.readUtf();
            }
        });

        wrappers.put(ItemStack.class, new SyncedWrapper<ItemStack>()
        {
            @Override
            public void write(RegistryFriendlyByteBuf buffer, ItemStack value)
            {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, value);
            }

            @Override
            public ItemStack read(RegistryFriendlyByteBuf buffer)
            {
                return ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            }

            @Override
            public boolean areEquals(ItemStack first, ItemStack second)
            {
                return ItemStack.isSameItemSameComponents(first, second);
            }

            @Override
            public ItemStack copy(ItemStack original)
            {
                return original.copy();
            }
        });

        wrappers.put(Container.class, new SyncedWrapper<Container>()
        {
            @Override
            public void write(RegistryFriendlyByteBuf buffer, Container value)
            {
                buffer.writeInt(value.getContainerSize());

                for (int slot = 0; slot < value.getContainerSize(); slot++)
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, value.getItem(slot));
            }

            @Override
            public Container read(RegistryFriendlyByteBuf buffer)
            {
                InventoryHandler inventory = new InventoryHandler(buffer.readInt());

                for (int slot = 0; slot < inventory.getContainerSize(); slot++)
                    inventory.setItem(slot, ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
                return inventory;
            }

            @Override
            public boolean areEquals(Container first, Container second)
            {
                if (second.getContainerSize() != first.getContainerSize())
                    return false;
                for (int slot = 0; slot < first.getContainerSize(); slot++)
                {
                    if (!ItemStack.isSameItemSameComponents(first.getItem(slot), second.getItem(slot)))
                        return false;
                }
                return true;
            }

            @Override
            public Container copy(Container original)
            {
                InventoryHandler copy = new InventoryHandler(original.getContainerSize());

                for (int slot = 0; slot < original.getContainerSize(); slot++)
                    copy.setItem(slot, original.getItem(slot).copy());
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
