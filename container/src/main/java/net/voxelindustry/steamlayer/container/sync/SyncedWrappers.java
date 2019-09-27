package net.voxelindustry.steamlayer.container.sync;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.voxelindustry.steamlayer.network.ByteBufHelper;
import net.voxelindustry.steamlayer.utils.ItemUtils;

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
        this.wrappers = new IdentityHashMap<>();

        this.wrappers.put(Integer.class, new SyncedWrapper<Integer>()
        {
            @Override
            public void write(ByteBuf buffer, Integer value)
            {
                buffer.writeInt(value);
            }

            @Override
            public Integer read(ByteBuf buffer)
            {
                return buffer.readInt();
            }
        });

        this.wrappers.put(Float.class, new SyncedWrapper<Float>()
        {
            @Override
            public void write(ByteBuf buffer, Float value)
            {
                buffer.writeFloat(value);
            }

            @Override
            public Float read(ByteBuf buffer)
            {
                return buffer.readFloat();
            }
        });

        this.wrappers.put(Boolean.class, new SyncedWrapper<Boolean>()
        {
            @Override
            public void write(ByteBuf buffer, Boolean value)
            {
                buffer.writeBoolean(value);
            }

            @Override
            public Boolean read(ByteBuf buffer)
            {
                return buffer.readBoolean();
            }
        });

        this.wrappers.put(Long.class, new SyncedWrapper<Long>()
        {
            @Override
            public void write(ByteBuf buffer, Long value)
            {
                buffer.writeLong(value);
            }

            @Override
            public Long read(ByteBuf buffer)
            {
                return buffer.readLong();
            }
        });

        this.wrappers.put(String.class, new SyncedWrapper<String>()
        {
            @Override
            public void write(ByteBuf buffer, String value)
            {
                ByteBufHelper.writeString(buffer, value);
            }

            @Override
            public String read(ByteBuf buffer)
            {
                return ByteBufHelper.readString(buffer);
            }
        });

        this.wrappers.put(FluidStack.class, new SyncedWrapper<FluidStack>()
        {
            @Override
            public void write(ByteBuf buffer, FluidStack value)
            {
                value.writeToPacket(new PacketBuffer(buffer));
            }

            @Override
            public FluidStack read(ByteBuf buffer)
            {
                return FluidStack.readFromPacket(new PacketBuffer(buffer));
            }

            @Override
            public boolean areEquals(FluidStack first, FluidStack second)
            {
                return super.areEquals(first, second) && first.getAmount() == second.getAmount();
            }

            @Override
            public FluidStack copy(FluidStack original)
            {
                return original != null ? original.copy() : null;
            }
        });

        this.wrappers.put(ItemStack.class, new SyncedWrapper<ItemStack>()
        {
            @Override
            public void write(ByteBuf buffer, ItemStack value)
            {
                ByteBufHelper.writeItemStack(buffer, value);
            }

            @Override
            public ItemStack read(ByteBuf buffer)
            {
                return ByteBufHelper.readItemStack(buffer);
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

        this.wrappers.put(IItemHandler.class, new SyncedWrapper<IItemHandler>()
        {
            @Override
            public void write(ByteBuf buffer, IItemHandler value)
            {
                buffer.writeInt(value.getSlots());

                for (int slot = 0; slot < value.getSlots(); slot++)
                    ByteBufHelper.writeItemStack(buffer, value.getStackInSlot(slot));
            }

            @Override
            public IItemHandler read(ByteBuf buffer)
            {
                ItemStackHandler inventory = new ItemStackHandler(buffer.readInt());

                for (int slot = 0; slot < inventory.getSlots(); slot++)
                    inventory.setStackInSlot(slot, ByteBufHelper.readItemStack(buffer));
                return inventory;
            }

            @Override
            public boolean areEquals(IItemHandler first, IItemHandler second)
            {
                if (second.getSlots() != first.getSlots())
                    return false;
                for (int slot = 0; slot < first.getSlots(); slot++)
                {
                    if (!ItemUtils.deepEqualsWithAmount(first.getStackInSlot(slot), second.getStackInSlot(slot)))
                        return false;
                }
                return true;
            }

            @Override
            public IItemHandler copy(IItemHandler original)
            {
                ItemStackHandler copy = new ItemStackHandler(original.getSlots());

                for (int slot = 0; slot < original.getSlots(); slot++)
                    copy.setStackInSlot(slot, original.getStackInSlot(slot).copy());
                return copy;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> SyncedWrapper<T> get(Class<T> typeClass)
    {
        return (SyncedWrapper<T>) this.wrappers.get(typeClass);
    }

    public <T> void registerWrapper(SyncedWrapper<T> wrapper, Class<T> typeClass)
    {
        this.wrappers.put(typeClass, wrapper);
    }
}
