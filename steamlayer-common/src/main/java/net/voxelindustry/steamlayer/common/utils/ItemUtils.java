package net.voxelindustry.steamlayer.common.utils;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

public class ItemUtils
{
    public static final Codec<ItemStack>       FIXED_ITEMSTACK_CODEC = ItemStack.OPTIONAL_CODEC;
    public static final Codec<List<ItemStack>> ITEMSTACK_LIST_CODEC  = ItemStack.OPTIONAL_CODEC.listOf();

    public static Tag saveStack(ItemStack stack, HolderLookup.Provider registries)
    {
        return ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }

    public static ItemStack loadStack(Tag tag, HolderLookup.Provider registries)
    {
        return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
    }

    public static boolean containsStack(Collection<ItemStack> stacks, ItemStack candidate)
    {
        for (ItemStack stack : stacks)
        {
            if (ItemStack.isSameItemSameComponents(stack, candidate))
                return true;
        }
        return false;
    }

    public static String getPrettyStackName(ItemStack stack)
    {
        return stack.getCount() + " " + StringUtils.capitalize(stack.getItem().toString());
    }

    public static boolean canMerge(ItemStack dest, ItemStack from)
    {
        return ItemStack.isSameItemSameComponents(dest, from) && dest.getCount() + from.getCount() <= dest.getMaxStackSize();
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
        if ((ItemStack.isSameItemSameComponents(dest, from)))
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

    public static boolean hasPlayerEnough(Inventory player, ItemStack stack, boolean deepEquals)
    {
        int needed = stack.getCount();
        for (int i = 0; i < player.getContainerSize(); ++i)
        {
            ItemStack itemstack = player.getItem(i);

            if (deepEquals && ItemStack.isSameItemSameComponents(stack, itemstack)
                    || !deepEquals && ItemStack.isSameItem(stack, itemstack))
            {
                needed -= itemstack.getCount();
                if (needed <= 0)
                    return true;
            }
        }
        return false;
    }

    public static int drainPlayer(ServerPlayer player, ItemStack stack)
    {
        int removed = player.getInventory().clearOrCountMatchingItems(candidate -> ItemStack.isSameItemSameComponents(stack, candidate), false, Integer.MAX_VALUE, player.inventoryMenu.getCraftSlots());
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.slotsChanged(player.getInventory());

        return removed;
    }
}
