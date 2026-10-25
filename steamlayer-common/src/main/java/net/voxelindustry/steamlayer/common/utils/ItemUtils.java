package net.voxelindustry.steamlayer.common.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemUtils
{
    // Mojang's codec does not handle empty ItemStack
    public static final Codec<ItemStack> FIXED_ITEMSTACK_CODEC = RecordCodecBuilder
            .create(instance -> instance.group(
                            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("id")
                                    .forGetter(stack -> Optional.ofNullable(stack.getItem())),
                            Codec.INT.fieldOf("Count")
                                    .forGetter(ItemStack::getCount),
                            CompoundTag.CODEC.optionalFieldOf("tag")
                                    .forGetter(stack -> Optional.ofNullable(stack.getTag())))
                    .apply(instance, (item, count, nbt) ->
                    {
                        var stack = new ItemStack(item.orElse(Items.AIR), count);
                        stack.setTag(nbt.orElse(null));
                        return stack;
                    }));
    public static final ListCodec<ItemStack> ITEMSTACK_LIST_CODEC = new ListCodec<>(FIXED_ITEMSTACK_CODEC);

    public static boolean containsStack(Collection<ItemStack> stacks, ItemStack candidate)
    {
        for (ItemStack stack : stacks)
        {
            if (ItemStack.isSameItemSameTags(stack, candidate))
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
        return ItemStack.isSameItemSameTags(dest, from) && dest.getCount() + from.getCount() <= dest.getMaxStackSize();
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
        if ((ItemStack.isSameItemSameTags(dest, from)))
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

            if (deepEquals && ItemStack.isSameItemSameTags(stack, itemstack)
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
        int removed = player.getInventory().clearOrCountMatchingItems(candidate -> ItemStack.isSameItemSameTags(stack, candidate), Integer.MAX_VALUE, player.inventoryMenu.getCraftSlots());
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.slotsChanged(player.getInventory());

        return removed;
    }

    public static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ItemStack> list)
    {
        if (list.size() > Byte.MAX_VALUE)
            throw new RuntimeException("Cannot save more than " + Byte.MAX_VALUE + " entries! size=" + list.size());

        ListTag tagList = new ListTag();

        for (int i = 0; i < list.size(); ++i)
        {
            ItemStack itemstack = list.get(i);

            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putByte("Slot", (byte) i);
            itemstack.save(nbttagcompound);
            tagList.add(nbttagcompound);
        }
        tag.put("Items", tagList);
        return tag;
    }

    public static void loadAllItems(CompoundTag tag, NonNullList<ItemStack> list)
    {
        ListTag tagList = tag.getList("Items", 10);

        for (int i = 0; i < tagList.size(); ++i)
        {
            CompoundTag NbtCompound = tagList.getCompound(i);
            int j = NbtCompound.getByte("Slot") & 255;

            if (j >= 0 && j < list.size())
                list.set(j, ItemStack.of(NbtCompound));
        }
    }
}
