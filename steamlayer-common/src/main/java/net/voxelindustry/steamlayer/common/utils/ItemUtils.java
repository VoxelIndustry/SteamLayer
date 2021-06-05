package net.voxelindustry.steamlayer.common.utils;

import com.mojang.serialization.codecs.ListCodec;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.StringUtils;

public class ItemUtils
{
    public static final ListCodec<ItemStack> ITEMSTACK_LIST_CODEC = new ListCodec<>(ItemStack.CODEC);

    public static boolean deepEquals(ItemStack a, ItemStack b)
    {
        return a.isItemEqual(b) && ItemStack.areTagsEqual(a, b);
    }

    public static boolean equalsIgnoringTag(ItemStack a, ItemStack b)
    {
        return a.isItemEqual(b);
    }

    @Deprecated
    public static boolean deepEqualsWithAmount(ItemStack a, ItemStack b)
    {
        return ItemStack.areEqual(a, b);
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

        return stack1.getCount() + stack2.getCount() <= stack1.getMaxCount()
                && ItemUtils.deepEquals(stack1, stack2);
    }

    public static int mergeStacks(ItemStack dest, ItemStack from, boolean doMerge)
    {
        if (dest.isEmpty() || from.isEmpty())
        {
            if (doMerge)
            {
                dest.increment(from.getCount());
                from.setCount(0);
            }
            return 0;
        }
        if ((ItemUtils.deepEquals(dest, from)))
        {
            int merged = Math.min(dest.getMaxCount() - dest.getCount(), from.getCount());
            if (doMerge)
            {
                dest.increment(merged);
                from.decrement(merged);
            }
            return merged;
        }
        return from.getCount();
    }

    public static boolean hasPlayerEnough(PlayerInventory player, ItemStack stack, boolean deepEquals)
    {
        int needed = stack.getCount();
        for (int i = 0; i < player.size(); ++i)
        {
            ItemStack itemstack = player.getStack(i);

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

    public static int drainPlayer(ServerPlayerEntity player, ItemStack stack)
    {
        int removed = player.getInventory().remove(candidate -> deepEquals(stack, candidate), Integer.MAX_VALUE, player.playerScreenHandler.getCraftingInput());
        player.currentScreenHandler.sendContentUpdates();
        player.playerScreenHandler.onContentChanged(player.getInventory());

        return removed;
    }

    public static NbtCompound saveAllItems(NbtCompound tag, DefaultedList<ItemStack> list)
    {
        if (list.size() > Byte.MAX_VALUE)
            throw new RuntimeException("Cannot save more than " + Byte.MAX_VALUE + " entries! size=" + list.size());

        NbtList tagList = new NbtList();

        for (int i = 0; i < list.size(); ++i)
        {
            ItemStack itemstack = list.get(i);

            NbtCompound nbttagcompound = new NbtCompound();
            nbttagcompound.putByte("Slot", (byte) i);
            itemstack.writeNbt(nbttagcompound);
            tagList.add(nbttagcompound);
        }
        tag.put("Items", tagList);
        return tag;
    }

    public static void loadAllItems(NbtCompound tag, DefaultedList<ItemStack> list)
    {
        NbtList tagList = tag.getList("Items", 10);

        for (int i = 0; i < tagList.size(); ++i)
        {
            NbtCompound NbtCompound = tagList.getCompound(i);
            int j = NbtCompound.getByte("Slot") & 255;

            if (j >= 0 && j < list.size())
                list.set(j, ItemStack.fromNbt(NbtCompound));
        }
    }
}
