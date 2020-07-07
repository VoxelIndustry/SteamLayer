package net.voxelindustry.steamlayer.common.utils;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.StringUtils;

public class ItemUtils
{
    public static boolean deepEquals(ItemStack a, ItemStack b)
    {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    public static boolean deepEqualsWithAmount(ItemStack a, ItemStack b)
    {
        return ItemStack.areItemStacksEqual(a, b);
    }

    public static ItemStack copyWithSize(ItemStack stack, int amount)
    {
        ItemStack result = stack.copy();
        result.setCount(1);
        return result;
    }

    public static String getPrettyStackName(ItemStack stack)
    {
        return stack.getCount() + " " + StringUtils.capitalize(stack.getItem().toString());
    }

    public static boolean canMergeStacks(ItemStack stack1, ItemStack stack2)
    {
        if (stack1.isEmpty() || stack2.isEmpty())
            return true;

        return stack1.getCount() + stack2.getCount() <= stack1.getMaxStackSize()
                && ItemUtils.deepEquals(stack1, stack2);
    }

    public static int mergeStacks(ItemStack dest, ItemStack from, boolean doMerge)
    {
        if (dest.isEmpty() || from.isEmpty())
        {
            if (doMerge)
            {
                dest.grow(from.getCount());
                from.setCount(0);
            }
            return 0;
        }
        if ((ItemUtils.deepEquals(dest, from)))
        {
            int merged = Math.min(dest.getMaxStackSize() - dest.getCount(), from.getCount());
            if (doMerge)
            {
                dest.grow(merged);
                from.shrink(merged);
            }
            return merged;
        }
        return from.getCount();
    }

    public static boolean hasPlayerEnough(PlayerInventory player, ItemStack stack, boolean deepEquals)
    {
        int needed = stack.getCount();
        for (int i = 0; i < player.getSizeInventory(); ++i)
        {
            ItemStack itemstack = player.getStackInSlot(i);

            if (deepEquals && ItemUtils.deepEquals(stack, itemstack)
                    || !deepEquals && ItemStack.areItemsEqual(stack, itemstack))
            {
                needed -= itemstack.getCount();
                if (needed <= 0)
                    return true;
            }
        }
        return false;
    }

    public static int drainPlayer(PlayerInventory player, ItemStack stack)
    {
        return player.clearMatchingItems(candidate -> deepEquals(candidate, stack), stack.getCount());
    }

    public static CompoundNBT saveAllItems(CompoundNBT tag, NonNullList<ItemStack> list)
    {
        if (list.size() > Byte.MAX_VALUE)
            throw new RuntimeException("Cannot save more than " + Byte.MAX_VALUE + " entries! size=" + list.size());

        ListNBT nbttaglist = new ListNBT();

        for (int i = 0; i < list.size(); ++i)
        {
            ItemStack itemstack = list.get(i);

            CompoundNBT nbttagcompound = new CompoundNBT();
            nbttagcompound.putByte("Slot", (byte) i);
            itemstack.write(nbttagcompound);
            nbttaglist.add(nbttagcompound);
        }
        tag.put("Items", nbttaglist);
        return tag;
    }

    public static void loadAllItems(CompoundNBT tag, NonNullList<ItemStack> list)
    {
        ListNBT nbttaglist = tag.getList("Items", 10);

        for (int i = 0; i < nbttaglist.size(); ++i)
        {
            CompoundNBT nbttagcompound = nbttaglist.getCompound(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < list.size())
                list.set(j, ItemStack.read(nbttagcompound));
        }
    }
}
