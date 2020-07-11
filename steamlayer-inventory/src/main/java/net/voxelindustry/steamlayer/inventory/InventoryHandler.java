package net.voxelindustry.steamlayer.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.voxelindustry.steamlayer.common.utils.TagSerializable;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Getter
@Accessors(fluent = true)
public class InventoryHandler implements Inventory, TagSerializable
{
    @Setter
    private IntConsumer onSlotChange;

    private Int2ObjectMap<Predicate<ItemStack>> slotFilters;

    @Setter
    private Consumer<PlayerEntity> onOpen;
    @Setter
    private Consumer<PlayerEntity> onClose;

    @Setter
    private Supplier<BlockPos> posSupplier;

    @Getter
    @Setter
    private boolean canDropContents = true;

    private final DefaultedList<ItemStack> stacks;

    public InventoryHandler()
    {
        this(0);
    }

    public InventoryHandler(int size)
    {
        this(DefaultedList.ofSize(size, ItemStack.EMPTY));
    }

    public InventoryHandler(DefaultedList<ItemStack> stacks)
    {
        this.stacks = stacks;

        slotFilters = new Int2ObjectOpenHashMap<>();
    }

    @Override
    public int size()
    {
        return stacks.size();
    }

    @Override
    public boolean isEmpty()
    {
        for (ItemStack stack : stacks)
            if (!stack.isEmpty())
                return false;
        return true;
    }

    @Override
    public ItemStack getStack(int slot)
    {
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount)
    {
        ItemStack stack = Inventories.splitStack(stacks, slot, amount);
        if (!stack.isEmpty())
            onContentsChanged(slot);

        return stack;
    }

    @Override
    public ItemStack removeStack(int slot)
    {
        ItemStack removed = removeStack(slot, getStack(slot).getCount());
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack)
    {
        stacks.set(slot, stack);

        onContentsChanged(slot);
    }

    @Override
    public int getMaxCountPerStack()
    {
        // FIXME : When an api allowing insert/extract is made. Fall back to the slotLimits map
        return 64;
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player)
    {
        if (posSupplier == null)
            return true;

        BlockPos pos = posSupplier.get();
        return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }

    @Override
    public void onOpen(PlayerEntity player)
    {
        if (onOpen != null)
            onOpen.accept(player);
    }

    @Override
    public void onClose(PlayerEntity player)
    {
        if (onClose != null)
            onClose.accept(player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack)
    {
        if (!slotFilters.isEmpty() && slotFilters.containsKey(slot))
            return slotFilters.get(slot).test(stack);
        return true;
    }

    @Override
    public void clear()
    {
        stacks.forEach(stack -> stack.setCount(0));
        notifySlotChange();
    }

    public void addSlotFilter(int slot, Predicate<ItemStack> filter)
    {
        slotFilters.put(slot, filter);
    }

    public void addSlotFilters(Predicate<ItemStack> filter, int... slots)
    {
        for (int slot : slots)
            addSlotFilter(slot, filter);
    }

    public void notifySlotChange()
    {
        if (onSlotChange != null)
            onSlotChange.accept(-1);
    }

    protected void onContentsChanged(int slot)
    {
        if (onSlotChange != null)
            onSlotChange.accept(slot);

        markDirty();
    }

    @Override
    public Tag toTag()
    {
        return Inventories.toTag(new CompoundTag(), stacks);
    }

    @Override
    public void fromTag(Tag tag)
    {
        Inventories.fromTag((CompoundTag) tag, stacks);
    }
}
