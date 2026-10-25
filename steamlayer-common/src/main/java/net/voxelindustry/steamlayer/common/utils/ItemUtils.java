package net.voxelindustry.steamlayer.common.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Optional;

public class ItemUtils
{
    // Mojang's codec does not handle empty ItemStack
    public static final Codec<ItemStack> FIXED_ITEMSTACK_CODEC = RecordCodecBuilder
            .create(instance -> instance.group(
                            Registries.ITEM.getCodec().optionalFieldOf("id")
                                    .forGetter(stack -> Optional.ofNullable(stack.getItem())),
                            Codec.INT.fieldOf("Count")
                                    .forGetter(ItemStack::getCount),
                            NbtCompound.CODEC.optionalFieldOf("tag")
                                    .forGetter(stack -> Optional.ofNullable(stack.getNbt())))
                    .apply(instance, (item, count, nbt) ->
                    {
                        var stack = new ItemStack(item.orElse(Items.AIR), count);
                        stack.setNbt(nbt.orElse(null));
                        return stack;
                    }));
    public static final ListCodec<ItemStack> ITEMSTACK_LIST_CODEC = new ListCodec<>(FIXED_ITEMSTACK_CODEC);

    public static boolean containsStack(Collection<ItemStack> stacks, ItemStack candidate)
    {
        for (ItemStack stack : stacks)
        {
            if (ItemStack.canCombine(stack, candidate))
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
        return ItemStack.canCombine(dest, from) && dest.getCount() + from.getCount() <= dest.getMaxCount();
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
        if ((ItemStack.canCombine(dest, from)))
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

            if (deepEquals && ItemStack.canCombine(stack, itemstack)
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
        int removed = player.getInventory().remove(candidate -> ItemStack.canCombine(stack, candidate), Integer.MAX_VALUE, player.playerScreenHandler.getCraftingInput());
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
