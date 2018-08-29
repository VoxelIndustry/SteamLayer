package net.voxelindustry.steamlayer.container.sync;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.voxelindustry.steamlayer.utils.ItemUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultSyncables
{
    public static final class SyncableInteger extends SyncableProperty<Integer>
    {
        public SyncableInteger(Supplier<Integer> supplier, Consumer<Integer> consumer)
        {
            super(supplier, consumer);
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tag)
        {
            tag.setInteger("integer", this.stored);
            return tag;
        }

        @Override
        public void fromNBT(NBTTagCompound tag)
        {
            this.stored = tag.getInteger("integer");
        }
    }

    public static final class SyncableFloat extends SyncableProperty<Float>
    {
        public SyncableFloat(Supplier<Float> supplier, Consumer<Float> consumer)
        {
            super(supplier, consumer);
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tag)
        {
            tag.setFloat("float", this.stored);
            return tag;
        }

        @Override
        public void fromNBT(NBTTagCompound tag)
        {
            this.stored = tag.getFloat("float");
        }
    }

    public static final class SyncableBoolean extends SyncableProperty<Boolean>
    {
        public SyncableBoolean(Supplier<Boolean> supplier, Consumer<Boolean> consumer)
        {
            super(supplier, consumer);
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tag)
        {
            tag.setBoolean("boolean", this.stored);
            return tag;
        }

        @Override
        public void fromNBT(NBTTagCompound tag)
        {
            this.stored = tag.getBoolean("boolean");
        }
    }

    public static final class SyncableString extends SyncableProperty<String>
    {
        public SyncableString(Supplier<String> supplier, Consumer<String> consumer)
        {
            super(supplier, consumer);
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tag)
        {
            tag.setString("string", this.stored);
            return tag;
        }

        @Override
        public void fromNBT(NBTTagCompound tag)
        {
            this.stored = tag.getString("string");
        }
    }

    public static final class SyncableFluid extends SyncableProperty<FluidStack>
    {
        public SyncableFluid(Supplier<FluidStack> supplier, Consumer<FluidStack> consumer)
        {
            super(supplier, consumer);
        }

        @Override
        public boolean areEquals(FluidStack other)
        {
            return this.stored.equals(other) && this.stored.amount == other.amount;
        }

        @Override
        public FluidStack copy(FluidStack original)
        {
            return original != null ? original.copy() : null;
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tag)
        {
            if (this.stored != null)
                this.stored.writeToNBT(tag);
            return tag;
        }

        @Override
        public void fromNBT(NBTTagCompound tag)
        {
            this.stored = FluidStack.loadFluidStackFromNBT(tag);
        }
    }

    public static final class SyncableItem extends SyncableProperty<ItemStack>
    {
        public SyncableItem(Supplier<ItemStack> supplier, Consumer<ItemStack> consumer)
        {
            super(supplier, consumer);
        }

        @Override
        public boolean areEquals(ItemStack other)
        {
            return ItemUtils.deepEqualsWithAmount(this.stored, other);
        }

        @Override
        public ItemStack copy(ItemStack original)
        {
            return original.copy();
        }

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tag)
        {
            this.stored.writeToNBT(tag);
            return tag;
        }

        @Override
        public void fromNBT(NBTTagCompound tag)
        {
            this.stored = new ItemStack(tag);
        }
    }

    public static final class SyncableInventory extends SyncableProperty<IItemHandler>
    {
        public SyncableInventory(Supplier<IItemHandler> supplier, Consumer<IItemHandler> consumer)
        {
            this(supplier, consumer, 0);
        }

        public SyncableInventory(Supplier<IItemHandler> supplier, Consumer<IItemHandler> consumer, int syncRate)
        {
            super(supplier, consumer, syncRate);
        }

        @Override
        public boolean areEquals(IItemHandler other)
        {
            if (other.getSlots() != this.stored.getSlots())
                return false;
            for (int slot = 0; slot < this.stored.getSlots(); slot++)
            {
                if (!ItemUtils.deepEqualsWithAmount(this.stored.getStackInSlot(slot), other.getStackInSlot(slot)))
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

        @Override
        public NBTTagCompound toNBT(NBTTagCompound tag)
        {
            for (int slot = 0; slot < this.stored.getSlots(); slot++)
                tag.setTag("stack" + slot, this.stored.getStackInSlot(slot).writeToNBT(new NBTTagCompound()));

            tag.setInteger("slots", this.stored.getSlots());

            return tag;
        }

        @Override
        public void fromNBT(NBTTagCompound tag)
        {
            this.stored = new ItemStackHandler(tag.getInteger("slots"));
            for (int slot = 0; slot < tag.getInteger("slots"); slot++)
                ((ItemStackHandler) this.stored).setStackInSlot(slot,
                        new ItemStack(tag.getCompoundTag("stack" + slot)));
        }
    }
}
