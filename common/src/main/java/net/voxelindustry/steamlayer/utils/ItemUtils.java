package net.voxelindustry.steamlayer.utils;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class ItemUtils
{
    public static boolean deepEquals(ItemStack a, ItemStack b)
    {
        if (a.getItem() != b.getItem() || !ItemStack.areItemStackTagsEqual(a, b))
            return false;
        if (a.getHasSubtypes())
        {
            if (ItemUtils.isWildcard(a.getItemDamage()) || ItemUtils.isWildcard(b.getItemDamage()))
                return true;
            if (a.getItemDamage() != b.getItemDamage())
                return false;
        }
        return true;
    }

    public static boolean deepEqualsWithAmount(ItemStack a, ItemStack b)
    {
        return a.getCount() == b.getCount() && deepEquals(a, b);
    }

    public static boolean isWildcard(int damage)
    {
        return damage == -1 || damage == OreDictionary.WILDCARD_VALUE;
    }

    public static String getPrettyStackName(ItemStack stack)
    {
        return stack.getCount() + " " + stack.getDisplayName();
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
        if ((ItemUtils.deepEquals(dest, from) || OreDictionary.itemMatches(dest, from, true)))
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

    public static boolean hasPlayerEnough(InventoryPlayer player, ItemStack stack, boolean deepEquals)
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

    public static int drainPlayer(InventoryPlayer player, ItemStack stack)
    {
        return player.clearMatchingItems(stack.getItem(), stack.getItemDamage(), stack.getCount(),
                stack.getTagCompound());
    }

    public static NBTTagCompound saveAllItems(NBTTagCompound tag, NonNullList<ItemStack> list)
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < list.size(); ++i)
        {
            ItemStack itemstack = list.get(i);

            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte) i);
            itemstack.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }
        tag.setTag("Items", nbttaglist);
        return tag;
    }

    public static void loadAllItems(NBTTagCompound tag, NonNullList<ItemStack> list)
    {
        NBTTagList nbttaglist = tag.getTagList("Items", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < list.size())
                list.set(j, new ItemStack(nbttagcompound));
        }
    }
}
